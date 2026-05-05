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
 * BARCHA taom ma'lumoti excelda bo'ladi:
 *   0 — name        (taom nomi)                      MAJBURIY
 *   1 — category    (BREAKFAST / LUNCH / SNACK)      MAJBURIY
 *   2 — photoUrl    (rasm URL)                       MAJBURIY
 *   3 — description (opisaniya / tavsif)             MAJBURIY
 *
 * Birinchi qator — header (o'tkazib yuboriladi).
 * Sen excelni o'zgartirsang — bot menyusi ham o'zgaradi.
 */
@Service
public class DishExcelImportService {

    private static final Logger log = LoggerFactory.getLogger(DishExcelImportService.class);

    @Value("${dish.excel.path:./excel/dishes.xlsx}")
    private String excelFilePath;

    // -----------------------------------------------------------------------
    // Asosiy import: Excel fayldan Dish ro'yxatini o'qiydi
    // -----------------------------------------------------------------------
    public List<Dish> importFromExcel() {
        File file = new File(excelFilePath);
        if (!file.exists()) {
            log.warn("📂 dishes.xlsx topilmadi: {}", file.getAbsolutePath());
            log.warn("   Namuna fayl yaratilmoqda — keyin uni o'zingiz to'ldirib qayta ishga tushiring.");
            createSampleExcel(file);
        }

        List<Dish> dishes = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            int lastRow = sheet.getLastRowNum();
            log.info("📖 Excel fayl o'qilmoqda: {} qator topildi", lastRow);

            for (int i = 1; i <= lastRow; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Dish dish = mapRowToDish(row, i + 1);
                if (dish != null) {
                    dishes.add(dish);
                    log.debug("  ✅ {}: {} ({})", i + 1, dish.getName(), dish.getCategory());
                }
            }

            log.info("✅ Exceldan {} ta taom muvaffaqiyatli o'qildi", dishes.size());

        } catch (IOException e) {
            log.error("❌ Excel faylni o'qishda xato: {}", e.getMessage());
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
            String description = getCellValue(row.getCell(3));

            // Bo'sh yoki izoh qatorni o'tkazib yuborish
            if (name.isBlank() || name.startsWith("#") || name.startsWith("[")) {
                return null;
            }

            // Majburiy maydonlar
            if (categoryRaw.isBlank()) {
                log.warn("⚠️ Qator {}: category bo'sh — '{}' o'tkazib yuborildi", rowNumber, name);
                return null;
            }
            if (photoUrl.isBlank()) {
                log.warn("⚠️ Qator {}: photoUrl bo'sh — '{}' o'tkazib yuborildi", rowNumber, name);
                return null;
            }
            if (description.isBlank()) {
                log.warn("⚠️ Qator {}: description bo'sh — '{}' o'tkazib yuborildi", rowNumber, name);
                return null;
            }

            // Kategoriyani aniqlash
            VoteCategory category = resolveCategory(categoryRaw);
            if (category == null) {
                log.warn("⚠️ Qator {}: noto'g'ri kategoriya '{}' — '{}' o'tkazib yuborildi",
                        rowNumber, categoryRaw, name);
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

    // BREAKFAST / Nonushta / breakfast → VoteCategory
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
    // Namuna Excel fayl yaratish (bo'sh template)
    // -----------------------------------------------------------------------
    public void createSampleExcel(File file) {
        if (file.getParentFile() != null) file.getParentFile().mkdirs();

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Dishes");

            CellStyle headerStyle = wb.createCellStyle();
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Row header = sheet.createRow(0);
            String[] headers = {"name", "category", "photoUrl", "description"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Boshlang'ich namuna ma'lumotlar — sen o'zing o'zgartirasen
            Object[][] samples = {
                {"Shirguruch",       "BREAKFAST", "https://example.com/shirguruch.jpg", "Sut bilan pishirilgan shirin guruch"},
                {"Mannaya kasha",    "BREAKFAST", "https://example.com/kasha.jpg",      "Sariyog' va murabbo bilan"},
                {"Qovurilgan tuxum", "BREAKFAST", "https://example.com/tuxum.jpg",      "Yangi ko'katlar bilan"},
                {"Lag'mon",          "LUNCH",     "https://example.com/lagmon.jpg",     "Go'sht va sabzavotli sho'rva"},
                {"Mastava",          "LUNCH",     "https://example.com/mastava.jpg",    "Qo'y go'shti bilan guruch sho'rvasi"},
                {"Chuchvara",        "LUNCH",     "https://example.com/chuchvara.jpg",  "Go'shtli qiyma bilan"},
                {"Somsa",            "SNACK",     "https://example.com/somsa.jpg",      "Kartoshkali krujkali somsa"},
                {"Pitsa",            "SNACK",     "https://example.com/pitsa.jpg",      "Mini pitsa bo'laklari"},
                {"Sinabon",          "SNACK",     "https://example.com/sinabon.jpg",    "Darchinli bulochka"},
            };

            for (int i = 0; i < samples.length; i++) {
                Row row = sheet.createRow(i + 1);
                for (int j = 0; j < samples[i].length; j++) {
                    row.createCell(j).setCellValue(samples[i][j].toString());
                }
            }

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
