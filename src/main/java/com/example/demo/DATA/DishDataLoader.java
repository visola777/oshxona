package com.example.demo.DATA;

import com.example.demo.entity.Dish;
import com.example.demo.entity.VoteCategory;
import com.example.demo.repository.DishRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Excel fayldan dishlarni o'qib database ga joylashtirish
 * 
 * Excel format (resources/db/dishes.xlsx):
 * Col A - name (taom nomi - MAJBURIY)
 * Col B - category (1=BREAKFAST, 2=LUNCH, 3=SNACK - MAJBURIY)
 * Col C - photoUrl (rasm URL - MAJBURIY)
 * Col D - description (tavsif - ixtiyoriy)
 */
@Component
public class DishDataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DishDataLoader.class);

    @Value("${dish.excel.default.path:src/main/resources/db/dishes.xlsx}")
    private String defaultExcelPath;

    private final DishRepository dishRepository;

    public DishDataLoader(DishRepository dishRepository) {
        this.dishRepository = dishRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("🔄 DishDataLoader ishga tushdi...");

        // Agar database bo'sh bo'lsa, Excel dan load qilish
        if (dishRepository.count() == 0) {
            log.info("📊 Database bo'sh, Excel fayldan taomlar yüklanmoqda...");
            loadDishesFromExcel();
        } else {
            log.info("✅ Database allaqachon taomlar bilan to'lgan ({} ta)", dishRepository.count());
        }
    }

    /**
     * Excel fayldan taomlarni o'qiydi va database ga joylashtiradi
     */
    private void loadDishesFromExcel() {
        File excelFile = new File(defaultExcelPath);

        if (!excelFile.exists()) {
            log.warn("⚠️ Excel fayli topilmadi: {}", defaultExcelPath);
            log.info("📝 Namuna Excel fayli yaratilmoqda...");
            createDefaultExcel(excelFile);
            loadDishesFromExcel(); // Qayta o'qish
            return;
        }

        try (FileInputStream fis = new FileInputStream(excelFile);
                Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            int loadedCount = 0;

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null)
                    continue;

                try {
                    String name = getCellValue(row.getCell(0));
                    String categoryRaw = getCellValue(row.getCell(1));
                    String photoUrl = getCellValue(row.getCell(2));
                    String description = getCellValue(row.getCell(3));

                    // Validation
                    if (name.isBlank() || categoryRaw.isBlank() || photoUrl.isBlank()) {
                        log.warn("Qator {}: noto'liq ma'lumot — o'tkazib yuborildi", i + 1);
                        continue;
                    }

                    // Category mapping: 1=BREAKFAST, 2=LUNCH, 3=SNACK
                    VoteCategory category = mapCategoryNumber(categoryRaw);
                    if (category == null) {
                        log.warn("Qator {}: noto'g'ri kategoriya '{}' — o'tkazib yuborildi", i + 1, categoryRaw);
                        continue;
                    }

                    // Duplicate check
                    if (dishRepository.findByNameIgnoreCase(name).isPresent()) {
                        log.debug("Qator {}: taom allaqachon mavjud — skip", i + 1);
                        continue;
                    }

                    // Create and save Dish
                    Dish dish = new Dish();
                    dish.setName(name.trim());
                    dish.setCategory(category.name());
                    dish.setPhotoUrl(photoUrl.trim());
                    dish.setDescription(description.trim());
                    dish.setActive(true);
                    dish.setTotalVotes(0);
                    dish.setExcluded(false);

                    dishRepository.save(dish);
                    loadedCount++;
                    log.debug("✅ Qator {}: {} ({}) - saqlandi", i + 1, name, category);

                } catch (Exception e) {
                    log.warn("Qator {} ni o'qishda xato: {}", i + 1, e.getMessage());
                }
            }

            log.info("✅ Excel dan {} ta taom yüklənd", loadedCount);

        } catch (IOException e) {
            log.error("❌ Excel faylni o'qishda xato: {}", e.getMessage());
        }
    }

    /**
     * Kategoriya raqamini VoteCategory ga o'tkazish
     * 1 = BREAKFAST (Nonushta)
     * 2 = LUNCH (Tushlik)
     * 3 = SNACK (Poldnik)
     */
    private VoteCategory mapCategoryNumber(String categoryRaw) {
        if (categoryRaw == null || categoryRaw.isBlank())
            return null;

        String normalized = categoryRaw.trim();

        try {
            int num = Integer.parseInt(normalized);
            return switch (num) {
                case 1 -> VoteCategory.BREAKFAST;
                case 2 -> VoteCategory.LUNCH;
                case 3 -> VoteCategory.SNACK;
                default -> null;
            };
        } catch (NumberFormatException e) {
            // Try by name
            normalized = normalized.toUpperCase();
            return switch (normalized) {
                case "BREAKFAST", "NONUSHTA", "1" -> VoteCategory.BREAKFAST;
                case "LUNCH", "TUSHLIK", "2" -> VoteCategory.LUNCH;
                case "SNACK", "POLDNIK", "3" -> VoteCategory.SNACK;
                default -> null;
            };
        }
    }

    /**
     * Cell qiymatini String ga o'tkazish
     */
    private String getCellValue(Cell cell) {
        if (cell == null)
            return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }

    /**
     * Namuna Excel fayl yaratish
     */
    private void createDefaultExcel(File file) {
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
            String[] headers = { "Nomi", "Kategoriya (1=Nonushta, 2=Tushlik, 3=Poldnik)", "Rasm URL", "Tavsif" };
            for (int i = 0; i < headers.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Default taomlar
            Object[][] defaultDishes = {
                    { "Shirguruch", 1, "https://via.placeholder.com/300?text=Shirguruch",
                            "Sut bilan pishirilgan shirin guruch" },
                    { "Mannaya kasha", 1, "https://via.placeholder.com/300?text=Mannaya", "Sariyog' va murabbo bilan" },
                    { "Qovurilgan tuxum", 1, "https://via.placeholder.com/300?text=Tuxum", "Yangi ko'katlar bilan" },
                    { "Lag'mon", 2, "https://via.placeholder.com/300?text=Lagmon", "Go'sht va sabzavotli sho'rva" },
                    { "Mastava", 2, "https://via.placeholder.com/300?text=Mastava",
                            "Qo'y go'shti bilan guruch sho'rvasi" },
                    { "Chuchvara", 2, "https://via.placeholder.com/300?text=Chuchvara", "Go'shtli qiyma bilan" },
                    { "Somsa kartoshkali", 3, "https://via.placeholder.com/300?text=Somsa",
                            "Kartoshkali krujkali somsa" },
                    { "Pitsa", 3, "https://via.placeholder.com/300?text=Pitsa", "Mini pitsa bo'laklari" },
                    { "Sinabon", 3, "https://via.placeholder.com/300?text=Sinabon", "Darchinli bulochka" },
            };

            for (int i = 0; i < defaultDishes.length; i++) {
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(defaultDishes[i][0].toString());
                row.createCell(1).setCellValue((int) defaultDishes[i][1]);
                row.createCell(2).setCellValue(defaultDishes[i][2].toString());
                row.createCell(3).setCellValue(defaultDishes[i][3].toString());
            }

            // Auto-size
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fos = new FileOutputStream(file)) {
                wb.write(fos);
            }

            log.info("✅ Namuna Excel yaratildi: {}", file.getAbsolutePath());

        } catch (IOException e) {
            log.error("❌ Excel yaratishda xato: {}", e.getMessage());
        }
    }
}
