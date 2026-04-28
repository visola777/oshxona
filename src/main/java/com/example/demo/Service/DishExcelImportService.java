package com.example.demo.Service;

import com.example.demo.entity.Dish;
import com.example.demo.entity.VoteCategory;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Excel fayldan Dish larni import qilish servisi.
 *
 * Excel kolonkalar tartibi (dishes.xlsx):
 *   0 — name        (taom nomi)      MAJBURIY
 *   1 — category    (BREAKFAST / LUNCH / SNACK)  MAJBURIY
 *   2 — photoUrl    (rasm URL)       MAJBURIY
 *   3 — description (tavsif)         ixtiyoriy
 *
 * Birinchi qator — header (o'tkazib yuboriladi).
 */
@Service
public class DishExcelImportService {

    private static final Logger log = LoggerFactory.getLogger(DishExcelImportService.class);

    @Value("${dish.excel.path:./excel/dishes.xlsx}")
    private String excelFilePath;

    // -----------------------------------------------------------------------
    // Asosiy import metodi — Excel fayldan Dish ro'yxatini o'qiydi
    // -----------------------------------------------------------------------
    public List<Dish> importFromExcel() {
        File file = new File(excelFilePath);
        if (!file.exists()) {
            log.warn("dishes.xlsx topilmadi: {}. Namuna fayl yaratilmoqda...", file.getAbsolutePath());
            createSampleExcel(file);
            return importFromExcel(); // qayta o'qish
        }

        List<Dish> dishes = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            int lastRow = sheet.getLastRowNum();
            log.info("Excel fayldan {} qator o'qilmoqda...", lastRow);

            for (int i = 1; i <= lastRow; i++) { // i=0 — header, o'tkazib yuboramiz
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Dish dish = mapRowToDish(row, i + 1);
                if (dish != null) {
                    dishes.add(dish);
                    log.debug("  ✅ Qator {}: {} ({})", i + 1, dish.getName(), dish.getCategory());
                } else {
                    log.debug("  ⚠️ Qator {} o'tkazib yuborildi (noto'g'ri ma'lumot)", i + 1);
                }
            }

            log.info("✅ Exceldan {} ta taom muvaffaqiyatli o'qildi", dishes.size());

        } catch (IOException e) {
            log.error("Excel faylni o'qishda xato: {}", e.getMessage());
        }

        return dishes;
    }

    // -----------------------------------------------------------------------
    // Stream orqali import (admin /import_excel buyrug'i uchun)
    // -----------------------------------------------------------------------
    public List<Dish> importFromStream(InputStream inputStream) {
        List<Dish> dishes = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            int lastRow = sheet.getLastRowNum();

            for (int i = 1; i <= lastRow; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                Dish dish = mapRowToDish(row, i + 1);
                if (dish != null) dishes.add(dish);
            }

            log.info("Stream dan {} ta taom o'qildi", dishes.size());

        } catch (IOException e) {
            log.error("Stream dan Excel o'qishda xato: {}", e.getMessage());
        }

        return dishes;
    }

    // -----------------------------------------------------------------------
    // Row → Dish mapping
    // -----------------------------------------------------------------------
    private Dish mapRowToDish(Row row, int rowNumber) {
        try {
            String name = getCellValue(row.getCell(0));
            String categoryRaw = getCellValue(row.getCell(1));
            String photoUrl = getCellValue(row.getCell(2));
            String description = row.getCell(3) != null ? getCellValue(row.getCell(3)) : "";

            // Majburiy maydonlarni tekshirish
            if (name.isBlank()) {
                log.warn("Qator {}: name bo'sh — o'tkazib yuborildi", rowNumber);
                return null;
            }
            if (name.startsWith("[") || name.startsWith("#")) {
                // Template yoki izoh qatori
                return null;
            }

            // Kategoriyani tekshirish va normallashtirish
            VoteCategory category = resolveCategory(categoryRaw);
            if (category == null) {
                log.warn("Qator {}: noto'g'ri kategoriya '{}' — o'tkazib yuborildi", rowNumber, categoryRaw);
                return null;
            }

            if (photoUrl.isBlank()) {
                log.warn("Qator {}: photoUrl bo'sh — taom '{}' qo'shilmaydi", rowNumber, name);
                return null;
            }

            Dish dish = new Dish();
            dish.setName(name.trim());
            dish.setCategory(category.name());
            dish.setPhotoUrl(photoUrl.trim());
            dish.setDescription(description.trim());
            dish.setActive(true);
            dish.setTotalVotes(0);
            return dish;

        } catch (Exception e) {
            log.warn("Qator {} ni o'qishda xato: {}", rowNumber, e.getMessage());
            return null;
        }
    }

    // -----------------------------------------------------------------------
    // Kategoriyani aniqlash: BREAKFAST / Nonushta / breakfast → VoteCategory
    // -----------------------------------------------------------------------
    private VoteCategory resolveCategory(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String s = raw.trim().toUpperCase();
        return switch (s) {
            case "BREAKFAST", "NONUSHTA", "TONG", "ERTALAB" -> VoteCategory.BREAKFAST;
            case "LUNCH", "OBED", "TUSHLIK" -> VoteCategory.LUNCH;
            case "SNACK", "POLDNIK", "KECHKI" -> VoteCategory.SNACK;
            default -> null;
        };
    }

    // -----------------------------------------------------------------------
    // Cell qiymatini xavfsiz o'qish
    // -----------------------------------------------------------------------
    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try { yield cell.getStringCellValue().trim(); }
                catch (Exception e) { yield String.valueOf(cell.getNumericCellValue()); }
            }
            default -> "";
        };
    }

    // -----------------------------------------------------------------------
    // Namuna Excel fayl yaratish (faylni birinchi marta ishga tushirganda)
    // -----------------------------------------------------------------------
    public void createSampleExcel(File file) {
        file.getParentFile().mkdirs();

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Dishes");

            // Header uslubi
            CellStyle headerStyle = wb.createCellStyle();
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Header qatori
            Row header = sheet.createRow(0);
            String[] headers = {"name", "category", "photoUrl", "description"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Namuna ma'lumotlar
            Object[][] samples = {
                {"Shirguruch",       "BREAKFAST", "https://example.com/shirguruch.jpg",  "Sut bilan pishirilgan shirin guruch"},
                {"Mannaya kasha",    "BREAKFAST", "https://example.com/kasha.jpg",       "Sariyog' va murabbo bilan"},
                {"Qovurilgan tuxum", "BREAKFAST", "https://example.com/tuxum.jpg",       "Yangi ko'katlar bilan"},
                {"Lag'mon",          "LUNCH",     "https://example.com/lagmon.jpg",       "Go'sht va sabzavotli sho'rva"},
                {"Mastava",          "LUNCH",     "https://example.com/mastava.jpg",      "Qo'y go'shti bilan guruch sho'rvasi"},
                {"Chuchvara",        "LUNCH",     "https://example.com/chuchvara.jpg",    "Go'shtli qiyma bilan"},
                {"Somsa",            "SNACK",     "https://example.com/somsa.jpg",        "Kartoshkali krujkali somsa"},
                {"Pitsa",            "SNACK",     "https://example.com/pitsa.jpg",        "Mini pitsa bo'laklari"},
                {"Sinabon",          "SNACK",     "https://example.com/sinabon.jpg",      "Darchinli bulochka"},
            };

            for (int i = 0; i < samples.length; i++) {
                Row row = sheet.createRow(i + 1);
                for (int j = 0; j < samples[i].length; j++) {
                    row.createCell(j).setCellValue(samples[i][j].toString());
                }
            }

            // Ustun kengliklarini avtomatik moslash
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fos = new FileOutputStream(file)) {
                wb.write(fos);
            }

            log.info("✅ Namuna dishes.xlsx yaratildi: {}", file.getAbsolutePath());

        } catch (IOException e) {
            log.error("Namuna Excel yaratishda xato: {}", e.getMessage());
        }
    }

    public String getExcelFilePath() {
        return excelFilePath;
    }
}
