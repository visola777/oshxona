package com.example.demo.Service;

import com.example.demo.entity.Dish;
import com.example.demo.repository.DishRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Dishlarni Excel faylga eksport qilish servisi.
 *
 * Excel kolonkalar tartibi (export):
 * 0 — id (Dish ID)
 * 1 — name (taom nomi)
 * 2 — category (BREAKFAST / LUNCH / SNACK)
 * 3 — photoUrl (rasm URL)
 * 4 — description (tavsif)
 * 5 — totalVotes (jami ovozlar)
 * 6 — active (faol/nofaol)
 * 7 — excluded (istisno qilingan)
 */
@Service
public class DishExcelExportService {

    private static final Logger log = LoggerFactory.getLogger(DishExcelExportService.class);

    @Value("${dish.excel.export.path:./excel}")
    private String exportPath;

    private final DishRepository dishRepository;

    public DishExcelExportService(DishRepository dishRepository) {
        this.dishRepository = dishRepository;
    }

    // -----------------------------------------------------------------------
    // Barcha dishlarni Excel ga eksport qilish
    // -----------------------------------------------------------------------
    public File exportAllDishesToExcel() {
        List<Dish> dishes = dishRepository.findAll();
        return exportDishesToExcel(dishes, "dishes_export");
    }

    /**
     * Faol dishlarni eksport qilish
     */
    public File exportActiveDishesToExcel() {
        List<Dish> dishes = dishRepository.findAllByActiveTrueOrderByTotalVotesDesc();
        return exportDishesToExcel(dishes, "dishes_active_export");
    }

    /**
     * Kategoriya bo'yicha dishlarni eksport qilish
     */
    public File exportDishesByCategory(String category) {
        List<Dish> dishes = dishRepository.findAllByCategoryIgnoreCaseAndActiveTrueOrderByTotalVotesDesc(category);
        return exportDishesToExcel(dishes, "dishes_" + category.toLowerCase() + "_export");
    }

    /**
     * Asosiy eksport metodi
     */
    private File exportDishesToExcel(List<Dish> dishes, String filePrefix) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        String fileName = filePrefix + "_" + timestamp + ".xlsx";
        File file = new File(exportPath, fileName);
        file.getParentFile().mkdirs();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Dishes");

            // Header uslubi — ko'k rangda, qalin shrift
            CellStyle headerStyle = createHeaderStyle(workbook);

            // Header qatori
            Row headerRow = sheet.createRow(0);
            String[] headers = { "ID", "Nomi", "Kategoriya", "Rasm URL", "Tavsif", "Ovozlar", "Faol", "Istisno" };
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Ma'lumotlar qatorlari
            CellStyle dataStyle = createDataStyle(workbook);
            int rowNum = 1;
            for (Dish dish : dishes) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(dish.getId() != null ? dish.getId().doubleValue() : 0);
                row.createCell(1).setCellValue(dish.getName() != null ? dish.getName() : "");
                row.createCell(2).setCellValue(dish.getCategory() != null ? dish.getCategory() : "");
                row.createCell(3).setCellValue(dish.getPhotoUrl() != null ? dish.getPhotoUrl() : "");
                row.createCell(4).setCellValue(dish.getDescription() != null ? dish.getDescription() : "");
                row.createCell(5).setCellValue(dish.getTotalVotes());
                row.createCell(6).setCellValue(dish.isActive() ? "✅ Ha" : "❌ Yo'q");
                row.createCell(7).setCellValue(dish.isExcluded() ? "❌ Ha" : "✅ Yo'q");

                // Uslub qo'llash
                for (int i = 0; i < 8; i++) {
                    row.getCell(i).setCellStyle(dataStyle);
                }
            }

            // Ustun kengliklarini avtomatik moslash
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Fayl saqlash
            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }

            log.info("✅ Dishlar eksport qilindi: {} ({} ta dish)", file.getAbsolutePath(), dishes.size());
            return file;

        } catch (IOException e) {
            log.error("Excel eksportda xato: {}", e.getMessage());
            return null;
        }
    }

    // -----------------------------------------------------------------------
    // Stream orqali eksport (download uchun)
    // -----------------------------------------------------------------------
    public InputStream exportAllDishesToStream() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        List<Dish> dishes = dishRepository.findAll();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Dishes");

            CellStyle headerStyle = createHeaderStyle(workbook);
            Row headerRow = sheet.createRow(0);
            String[] headers = { "ID", "Nomi", "Kategoriya", "Rasm URL", "Tavsif", "Ovozlar", "Faol", "Istisno" };
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            CellStyle dataStyle = createDataStyle(workbook);
            int rowNum = 1;
            for (Dish dish : dishes) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(dish.getId() != null ? dish.getId().doubleValue() : 0);
                row.createCell(1).setCellValue(dish.getName() != null ? dish.getName() : "");
                row.createCell(2).setCellValue(dish.getCategory() != null ? dish.getCategory() : "");
                row.createCell(3).setCellValue(dish.getPhotoUrl() != null ? dish.getPhotoUrl() : "");
                row.createCell(4).setCellValue(dish.getDescription() != null ? dish.getDescription() : "");
                row.createCell(5).setCellValue(dish.getTotalVotes());
                row.createCell(6).setCellValue(dish.isActive() ? "✅ Ha" : "❌ Yo'q");
                row.createCell(7).setCellValue(dish.isExcluded() ? "❌ Ha" : "✅ Yo'q");

                for (int i = 0; i < 8; i++) {
                    row.getCell(i).setCellStyle(dataStyle);
                }
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(baos);
        }

        return new ByteArrayInputStream(baos.toByteArray());
    }

    // -----------------------------------------------------------------------
    // Batch disable - bir nechta dishlarni nofaol qilish
    // -----------------------------------------------------------------------
    public int batchDisableDishes(List<Long> dishIds) {
        List<Dish> dishes = dishRepository.findAllById(dishIds);
        int count = 0;
        for (Dish dish : dishes) {
            if (dish.isActive()) {
                dish.setActive(false);
                dishRepository.save(dish);
                count++;
                log.debug("Nofaol qilindi: {}", dish.getName());
            }
        }
        log.info("✅ {} ta dish nofaol qilindi", count);
        return count;
    }

    /**
     * Batch enable - bir nechta dishlarni faol qilish
     */
    public int batchEnableDishes(List<Long> dishIds) {
        List<Dish> dishes = dishRepository.findAllById(dishIds);
        int count = 0;
        for (Dish dish : dishes) {
            if (!dish.isActive()) {
                dish.setActive(true);
                dishRepository.save(dish);
                count++;
                log.debug("Faol qilindi: {}", dish.getName());
            }
        }
        log.info("✅ {} ta dish faol qilindi", count);
        return count;
    }

    /**
     * Batch exclude - bir nechta dishlarni istisno qilish
     */
    public int batchExcludeDishes(List<Long> dishIds) {
        List<Dish> dishes = dishRepository.findAllById(dishIds);
        int count = 0;
        for (Dish dish : dishes) {
            if (!dish.isExcluded()) {
                dish.setExcluded(true);
                dishRepository.save(dish);
                count++;
                log.debug("Istisno qilindi: {}", dish.getName());
            }
        }
        log.info("✅ {} ta dish istisno qilindi", count);
        return count;
    }

    /**
     * Batch reset votes - ovozlarni nollash
     */
    public int batchResetVotes(List<Long> dishIds) {
        List<Dish> dishes = dishRepository.findAllById(dishIds);
        int count = 0;
        for (Dish dish : dishes) {
            if (dish.getTotalVotes() > 0) {
                dish.setTotalVotes(0);
                dishRepository.save(dish);
                count++;
                log.debug("Ovozlar nollandi: {}", dish.getName());
            }
        }
        log.info("✅ {} ta dish ovozlari nollandi", count);
        return count;
    }

    // -----------------------------------------------------------------------
    // Uslub yordamchilar
    // -----------------------------------------------------------------------
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setWrapText(true);
        return style;
    }

    public String getExportPath() {
        return exportPath;
    }
}
