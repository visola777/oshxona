package com.example.demo.Service;

import com.example.demo.entity.Dish;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DishMessageService {
    private static final Logger log = LoggerFactory.getLogger(DishMessageService.class);
    private final ExcelDishService excelDishService;
    private final MealDishService mealDishService;

    public DishMessageService(ExcelDishService excelDishService, MealDishService mealDishService) {
        this.excelDishService = excelDishService;
        this.mealDishService = mealDishService;
    }

    /**
     * Kategoriya uchun dishlari Excel-dan o'qiydi va formatlaydi
     * Category: 1=Nonushta, 2=Abetmi Poldnik, 3=Poldnik
     */
    public String getCategoryDishesMessage(String categoryCode, String languageCode) {
        try {
            List<Dish> dishes = excelDishService.readDishesFromExcel();
            
            // Filter by category
            List<Dish> categoryDishes = dishes.stream()
                    .filter(d -> d.getCategory().equals(categoryCode))
                    .toList();

            if (categoryDishes.isEmpty()) {
                return "❌ " + getCategoryName(categoryCode) + " uchun ovqat topilmadi.";
            }

            StringBuilder message = new StringBuilder();
            message.append("🍽️ ").append(getCategoryName(categoryCode)).append("\n\n");

            for (Dish dish : categoryDishes) {
                message.append("📌 ").append(dish.getName()).append("\n");
                if (dish.getDescription() != null && !dish.getDescription().isEmpty()) {
                    message.append("   📝 ").append(dish.getDescription()).append("\n");
                }
                if (dish.getPhotoUrl() != null && !dish.getPhotoUrl().isEmpty()) {
                    message.append("   🖼️ ").append(dish.getPhotoUrl()).append("\n");
                }
                message.append("\n");
            }

            return message.toString();
        } catch (Exception e) {
            log.error("Error getting dishes for category {}: {}", categoryCode, e.getMessage());
            return "❌ Xatolik yuz berdi. Keyinroq qayta urinib ko'ring.";
        }
    }

    /**
     * Bitta dish uchun detali habar yaratadi
     */
    public String getDishDetailMessage(String dishName) {
        try {
            List<Dish> allDishes = excelDishService.readDishesFromExcel();
            
            Dish dish = allDishes.stream()
                    .filter(d -> d.getName().equalsIgnoreCase(dishName))
                    .findFirst()
                    .orElse(null);

            if (dish == null) {
                return "❌ Ovqat topilmadi.";
            }

            StringBuilder message = new StringBuilder();
            message.append("🍽️ ").append(dish.getName()).append("\n");
            message.append("📂 Kategoriya: ").append(getCategoryName(dish.getCategory())).append("\n");
            
            if (dish.getDescription() != null && !dish.getDescription().isEmpty()) {
                message.append("📝 Tavsif: ").append(dish.getDescription()).append("\n");
            }
            
            if (dish.getPhotoUrl() != null && !dish.getPhotoUrl().isEmpty()) {
                message.append("🖼️ Rasm: ").append(dish.getPhotoUrl()).append("\n");
            }
            
            message.append("✅ Faol: ").append(dish.isActive() ? "Ha" : "Yo'q").append("\n");

            return message.toString();
        } catch (Exception e) {
            log.error("Error getting dish details for {}: {}", dishName, e.getMessage());
            return "❌ Xatolik yuz berdi.";
        }
    }

    /**
     * Barcha dishlari HTML formatda jadval sifatida qaytaradi
     */
    public String getAllDishesAsHtmlTable() {
        try {
            List<Dish> dishes = excelDishService.readDishesFromExcel();

            StringBuilder html = new StringBuilder();
            html.append("<table border='1' cellpadding='10' cellspacing='0'>\n");
            html.append("<tr><th>Nomi</th><th>Kategoriya</th><th>Tavsif</th><th>Rasm</th></tr>\n");

            for (Dish dish : dishes) {
                html.append("<tr>");
                html.append("<td>").append(dish.getName()).append("</td>");
                html.append("<td>").append(getCategoryName(dish.getCategory())).append("</td>");
                html.append("<td>").append(dish.getDescription() != null ? dish.getDescription() : "-").append("</td>");
                html.append("<td>");
                if (dish.getPhotoUrl() != null && !dish.getPhotoUrl().isEmpty()) {
                    html.append("<a href='").append(dish.getPhotoUrl()).append("'>Rasm</a>");
                } else {
                    html.append("-");
                }
                html.append("</td>");
                html.append("</tr>\n");
            }

            html.append("</table>");
            return html.toString();
        } catch (Exception e) {
            log.error("Error generating HTML table: {}", e.getMessage());
            return "<p>Xatolik yuz berdi.</p>";
        }
    }

    /**
     * Kategoriya uchun emoji va nomni qaytaradi
     */
    public String getCategoryName(String categoryCode) {
        return switch (categoryCode) {
            case "1" -> "🌅 Nonushta";
            case "2" -> "🌤️ Abetmi Poldnik";
            case "3" -> "🌙 Poldnik";
            default -> "📌 Noma'lum";
        };
    }

    /**
     * Excel-dan dishlari o'qiydi va database-ga saqlaydi
     */
    public void syncExcelToDB(MealDishService dishService) {
        try {
            List<Dish> dishes = excelDishService.readDishesFromExcel();
            
            for (Dish dish : dishes) {
                // Check if dish already exists
                if (dishService.getDish(dish.getId()) == null) {
                    dishService.save(dish);
                    log.info("✅ Synced dish from Excel to DB: {}", dish.getName());
                }
            }
            
            log.info("✅ Excel to DB sync completed. {} dishes processed.", dishes.size());
        } catch (Exception e) {
            log.error("Error syncing Excel to DB: {}", e.getMessage());
        }
    }
}
