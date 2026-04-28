package com.example.demo.Service;

import com.example.demo.entity.Dish;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ExcelDishService {
    private static final Logger log = LoggerFactory.getLogger(ExcelDishService.class);
    private static final String EXCEL_PATH = "src/main/resources/db/dishes.xlsx";
    private static final String SHEET_NAME = "Dishes";
    
    // Category Constants
    public static final String CATEGORY_BREAKFAST = "1"; // Nonushta
    public static final String CATEGORY_LUNCH = "2";     // Abetmi Poldnik
    public static final String CATEGORY_DINNER = "3";    // Poldnik

    /**
     * Excel faylini yaratish (ilk bor)
     */
    public void initializeExcelFile() {
        File excelFile = new File(EXCEL_PATH);
        
        if (excelFile.exists()) {
            log.info("Excel file already exists: {}", EXCEL_PATH);
            return;
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(SHEET_NAME);
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Name", "Category", "Photo URL", "Description", "Active"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                CellStyle style = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setBold(true);
                style.setFont(font);
                cell.setCellStyle(style);
            }

            // Add sample data
            int rowNum = 1;
            List<Map<String, Object>> sampleData = getSampleData();
            for (Map<String, Object> data : sampleData) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue((Integer) data.get("id"));
                row.createCell(1).setCellValue((String) data.get("name"));
                row.createCell(2).setCellValue((String) data.get("category"));
                row.createCell(3).setCellValue((String) data.get("photoUrl"));
                row.createCell(4).setCellValue((String) data.get("description"));
                row.createCell(5).setCellValue((Boolean) data.get("active"));
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Save workbook
            try (FileOutputStream fos = new FileOutputStream(excelFile)) {
                workbook.write(fos);
                log.info("Excel file created successfully at: {}", EXCEL_PATH);
            }
        } catch (IOException e) {
            log.error("Error creating Excel file", e);
        }
    }

    /**
     * Default ma'lumotlarni qaytaradi
     */
    private List<Map<String, Object>> getSampleData() {
        List<Map<String, Object>> data = new ArrayList<>();

        // Nonushta (Breakfast - Category 1)
        data.add(createDishMap(1, "Osh", CATEGORY_BREAKFAST, "https://via.placeholder.com/200?text=Osh", "Plov with meat", true));
        data.add(createDishMap(2, "Chuchvara", CATEGORY_BREAKFAST, "https://via.placeholder.com/200?text=Chuchvara", "Traditional dumplings", true));
        data.add(createDishMap(3, "Manti", CATEGORY_BREAKFAST, "https://via.placeholder.com/200?text=Manti", "Large dumplings", true));

        // Abetmi Poldnik (Lunch - Category 2)
        data.add(createDishMap(4, "Norin", CATEGORY_LUNCH, "https://via.placeholder.com/200?text=Norin", "Noodle with meat", true));
        data.add(createDishMap(5, "Lag'man", CATEGORY_LUNCH, "https://via.placeholder.com/200?text=Lagman", "Hand-pulled noodles", true));
        data.add(createDishMap(6, "Shurvak", CATEGORY_LUNCH, "https://via.placeholder.com/200?text=Shurvak", "Meat stew", true));

        // Poldnik (Dinner - Category 3)
        data.add(createDishMap(7, "Kebab", CATEGORY_DINNER, "https://via.placeholder.com/200?text=Kebab", "Grilled meat", true));
        data.add(createDishMap(8, "Samsa", CATEGORY_DINNER, "https://via.placeholder.com/200?text=Samsa", "Fried pastry", true));
        data.add(createDishMap(9, "Tandir Bread", CATEGORY_DINNER, "https://via.placeholder.com/200?text=Bread", "Traditional bread", true));

        return data;
    }

    /**
     * Dish ma'lumotlar xaritasini yaratadi
     */
    private Map<String, Object> createDishMap(int id, String name, String category, String photoUrl, String description, boolean active) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("name", name);
        map.put("category", category);
        map.put("photoUrl", photoUrl);
        map.put("description", description);
        map.put("active", active);
        return map;
    }

    /**
     * Exceldan dishlari o'qish
     */
    public List<Dish> readDishesFromExcel() {
        List<Dish> dishes = new ArrayList<>();
        File excelFile = new File(EXCEL_PATH);

        if (!excelFile.exists()) {
            log.warn("Excel file not found, initializing...");
            initializeExcelFile();
            return dishes;
        }

        try (FileInputStream fis = new FileInputStream(excelFile);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet(SHEET_NAME);
            if (sheet == null) {
                log.warn("Sheet '{}' not found", SHEET_NAME);
                return dishes;
            }

            Iterator<Row> rowIterator = sheet.iterator();
            rowIterator.next(); // Skip header

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                if (row.getFirstCellNum() < 0) continue; // Skip empty rows

                try {
                    Dish dish = new Dish();
                    dish.setId(getLongCellValue(row.getCell(0)));
                    dish.setName(getStringCellValue(row.getCell(1)));
                    dish.setCategory(getStringCellValue(row.getCell(2)));
                    dish.setPhotoUrl(getStringCellValue(row.getCell(3)));
                    dish.setDescription(getStringCellValue(row.getCell(4)));
                    dish.setActive(getBooleanCellValue(row.getCell(5)));
                    dish.setCreatedAt(LocalDateTime.now());

                    dishes.add(dish);
                } catch (Exception e) {
                    log.error("Error parsing row", e);
                }
            }

            log.info("Read {} dishes from Excel", dishes.size());
        } catch (IOException e) {
            log.error("Error reading Excel file", e);
        }

        return dishes;
    }

    /**
     * Excelga yangi dish qo'shish
     */
    public void addDishToExcel(Dish dish) {
        File excelFile = new File(EXCEL_PATH);

        if (!excelFile.exists()) {
            initializeExcelFile();
        }

        try (FileInputStream fis = new FileInputStream(excelFile);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet(SHEET_NAME);
            int lastRowNum = sheet.getLastRowNum();
            Row newRow = sheet.createRow(lastRowNum + 1);

            newRow.createCell(0).setCellValue(dish.getId() != null ? dish.getId() : 0);
            newRow.createCell(1).setCellValue(dish.getName());
            newRow.createCell(2).setCellValue(dish.getCategory());
            newRow.createCell(3).setCellValue(dish.getPhotoUrl());
            newRow.createCell(4).setCellValue(dish.getDescription());
            newRow.createCell(5).setCellValue(dish.isActive());

            try (FileOutputStream fos = new FileOutputStream(excelFile)) {
                workbook.write(fos);
                log.info("Dish '{}' added to Excel", dish.getName());
            }
        } catch (IOException e) {
            log.error("Error writing to Excel file", e);
        }
    }

    /**
     * Excelni bo'sh shaklda qayta yaratish (reset)
     */
    public void resetExcelFile() {
        File excelFile = new File(EXCEL_PATH);
        if (excelFile.exists()) {
            excelFile.delete();
            log.info("Excel file deleted");
        }
        initializeExcelFile();
    }

    // ============ HELPER METHODS ============

    private String getStringCellValue(Cell cell) {
        if (cell == null) return "";
        return cell.getCellType() == CellType.STRING ? cell.getStringCellValue() : cell.toString();
    }

    private Long getLongCellValue(Cell cell) {
        if (cell == null) return 0L;
        return (long) cell.getNumericCellValue();
    }

    private Boolean getBooleanCellValue(Cell cell) {
        if (cell == null) return true;
        return cell.getCellType() == CellType.BOOLEAN ? cell.getBooleanCellValue() : true;
    }

    /**
     * Kategoriya nomini qayturadi
     */
    public String getCategoryName(String categoryCode) {
        return switch (categoryCode) {
            case CATEGORY_BREAKFAST -> "🌅 Nonushta";
            case CATEGORY_LUNCH -> "🌤️ Abetmi Poldnik";
            case CATEGORY_DINNER -> "🌙 Poldnik";
            default -> "Unknown";
        };
    }
}
