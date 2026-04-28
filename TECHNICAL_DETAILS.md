# 🔍 Key Code Changes - Technical Details

## 1. MealDishService - Navigation Methods

```java
// Get count of dishes in category
public int getDishCountByCategory(String category) {
    return getActiveDishesByCategory(category).size();
}

// Get dish at specific index
public Dish getDishByIndexInCategory(String category, int index) {
    List<Dish> dishes = getActiveDishesByCategory(category);
    if (index < 0 || index >= dishes.size()) return null;
    return dishes.get(index);
}

// Get index of a dish
public int getDishIndexInCategory(Long dishId, String category) {
    List<Dish> dishes = getActiveDishesByCategory(category);
    for (int i = 0; i < dishes.size(); i++) {
        if (dishes.get(i).getId().equals(dishId)) return i;
    }
    return -1;
}

// Navigation methods
public Dish getNextDishInCategory(Long currentDishId, String category) {
    int currentIndex = getDishIndexInCategory(currentDishId, category);
    if (currentIndex >= 0 && currentIndex < getDishCountByCategory(category) - 1) {
        return getDishByIndexInCategory(category, currentIndex + 1);
    }
    return null;
}

public Dish getPreviousDishInCategory(Long currentDishId, String category) {
    int currentIndex = getDishIndexInCategory(currentDishId, category);
    if (currentIndex > 0) {
        return getDishByIndexInCategory(category, currentIndex - 1);
    }
    return null;
}
```

---

## 2. TelegramMealVoteBot - Enhanced sendDishDetail()

```java
private void sendDishDetail(Long chatId, TelegramUser user, Long dishId) {
    Dish dish = dishService.getDish(dishId);
    if (dish == null) {
        sendText(chatId, "Taom topilmadi.", user.getLanguageCode());
        return;
    }

    // Get navigation information
    int currentIndex = dishService.getDishIndexInCategory(dishId, dish.getCategory());
    int totalCount = dishService.getDishCountByCategory(dish.getCategory());
    boolean hasPrevious = currentIndex > 0;
    boolean hasNext = currentIndex < totalCount - 1;

    // Build caption with dish counter display
    String captionRaw = "🍽️ " + dish.getName() + "\n\n" +
            (dish.getDescription() != null ? dish.getDescription() + "\n\n" : "") +
            "📊 Hozirgi ovozlar: " + dish.getTotalVotes() + "\n" +
            "📍 Taomlar: " + (currentIndex + 1) + "/" + totalCount + "\n\n" +
            "✅ Ushbu taomga ovoz berishni tasdiqlaysizmi?";

    // Create inline buttons with navigation
    InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
    List<List<InlineKeyboardButton>> rows = new ArrayList<>();

    // Navigation row (prev/counter/next)
    if (hasPrevious || hasNext) {
        List<InlineKeyboardButton> navRow = new ArrayList<>();

        if (hasPrevious) {
            InlineKeyboardButton prevBtn = new InlineKeyboardButton();
            prevBtn.setText("⬅️ Oldingi");
            prevBtn.setCallbackData("DISH_NAV_PREV:" + dish.getId());
            navRow.add(prevBtn);
        }

        InlineKeyboardButton counterBtn = new InlineKeyboardButton();
        counterBtn.setText(String.format("%d/%d", currentIndex + 1, totalCount));
        counterBtn.setCallbackData("DISH_NAV_COUNTER");
        navRow.add(counterBtn);

        if (hasNext) {
            InlineKeyboardButton nextBtn = new InlineKeyboardButton();
            nextBtn.setText("Keyingi ➡️");
            nextBtn.setCallbackData("DISH_NAV_NEXT:" + dish.getId());
            navRow.add(nextBtn);
        }

        rows.add(navRow);
    }

    // Action buttons row
    InlineKeyboardButton back = new InlineKeyboardButton();
    back.setText("⬅️ Orqaga");
    back.setCallbackData("BACK_TO_CATEGORY:" + dish.getCategory());

    InlineKeyboardButton confirm = new InlineKeyboardButton();
    confirm.setText("✅ Tasdiqlash");
    confirm.setCallbackData("CONFIRM_VOTE:" + dish.getId());

    rows.add(List.of(back, confirm));
    markup.setKeyboard(rows);

    // Send photo (with fallback to text)
    boolean sent = false;
    if (dish.getPhotoUrl() != null && !dish.getPhotoUrl().isBlank()) {
        SendPhoto photo = new SendPhoto();
        photo.setChatId(chatId.toString());
        photo.setPhoto(new InputFile(dish.getPhotoUrl()));
        photo.setCaption(captionRaw);
        photo.setReplyMarkup(markup);
        try {
            execute(photo);
            sent = true;
        } catch (TelegramApiException e) {
            log.warn("Photo failed: {}", e.getMessage());
        }
    }

    if (!sent) {
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId.toString());
        msg.setText(captionRaw);
        msg.setReplyMarkup(markup);
        msg.setParseMode("Markdown");
        try {
            execute(msg);
        } catch (TelegramApiException e) {
            msg.setParseMode(null);
            try {
                execute(msg);
            } catch (TelegramApiException ex) {
                log.error("Failed to send dish detail", ex);
            }
        }
    }
}
```

---

## 3. Callback Handlers for Navigation

```java
// Dish navigation - next
else if (data.startsWith("DISH_NAV_NEXT:")) {
    Long currentDishId = parseId(data);
    if (currentDishId != null) {
        Dish currentDish = dishService.getDish(currentDishId);
        if (currentDish != null) {
            Dish nextDish = dishService.getNextDishInCategory(currentDishId, currentDish.getCategory());
            if (nextDish != null) {
                sendDishDetail(chatId, user, nextDish.getId());
            }
        }
    }
}

// Dish navigation - previous
else if (data.startsWith("DISH_NAV_PREV:")) {
    Long currentDishId = parseId(data);
    if (currentDishId != null) {
        Dish currentDish = dishService.getDish(currentDishId);
        if (currentDish != null) {
            Dish prevDish = dishService.getPreviousDishInCategory(currentDishId, currentDish.getCategory());
            if (prevDish != null) {
                sendDishDetail(chatId, user, prevDish.getId());
            }
        }
    }
}

// Dish navigation - counter (no action)
else if (data.startsWith("DISH_NAV_COUNTER")) {
    // Just acknowledge, no action needed
}
```

---

## 4. DishExcelExportService - Key Methods

```java
public File exportAllDishesToExcel() {
    List<Dish> dishes = dishRepository.findAll();
    return exportDishesToExcel(dishes, "dishes_export");
}

public File exportActiveDishesToExcel() {
    List<Dish> dishes = dishRepository.findAllByActiveTrueOrderByTotalVotesDesc();
    return exportDishesToExcel(dishes, "dishes_active_export");
}

private File exportDishesToExcel(List<Dish> dishes, String filePrefix) {
    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
    String fileName = filePrefix + "_" + timestamp + ".xlsx";
    File file = new File(exportPath, fileName);
    file.getParentFile().mkdirs();

    try (Workbook workbook = new XSSFWorkbook()) {
        Sheet sheet = workbook.createSheet("Dishes");

        CellStyle headerStyle = createHeaderStyle(workbook);
        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Nomi", "Kategoriya", "Rasm URL", "Tavsif", "Ovozlar", "Faol", "Istisno"};
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

// Batch operations
public int batchDisableDishes(List<Long> dishIds) {
    List<Dish> dishes = dishRepository.findAllById(dishIds);
    int count = 0;
    for (Dish dish : dishes) {
        if (dish.isActive()) {
            dish.setActive(false);
            dishRepository.save(dish);
            count++;
        }
    }
    return count;
}

public int batchEnableDishes(List<Long> dishIds) {
    List<Dish> dishes = dishRepository.findAllById(dishIds);
    int count = 0;
    for (Dish dish : dishes) {
        if (!dish.isActive()) {
            dish.setActive(true);
            dishRepository.save(dish);
            count++;
        }
    }
    return count;
}
```

---

## 5. AdminService Integration

```java
// Add File import
import java.io.File;

// Excel export methods
public File exportAllDishesToExcel(DishExcelExportService exportService) {
    return exportService.exportAllDishesToExcel();
}

public File exportActiveDishesToExcel(DishExcelExportService exportService) {
    return exportService.exportActiveDishesToExcel();
}

// Batch operations
@Transactional
public AdminResult batchDisableDishes(DishExcelExportService exportService, List<Long> dishIds) {
    try {
        int count = exportService.batchDisableDishes(dishIds);
        return new AdminResult(true, String.format("✅ %d ta dish nofaol qilindi", count));
    } catch (Exception e) {
        return new AdminResult(false, "❌ Error: " + e.getMessage());
    }
}

@Transactional
public AdminResult batchEnableDishes(DishExcelExportService exportService, List<Long> dishIds) {
    try {
        int count = exportService.batchEnableDishes(dishIds);
        return new AdminResult(true, String.format("✅ %d ta dish faol qilindi", count));
    } catch (Exception e) {
        return new AdminResult(false, "❌ Error: " + e.getMessage());
    }
}
```

---

## 6. User Interface Changes

### Before Navigation
```
🍽️ Lag'mon
Hand-pulled noodle soup...

📊 Hozirgi ovozlar: 5

✅ Ushbu taomga ovoz berishni tasdiqlaysizmi?

[⬅️ Orqaga] [✅ Tasdiqlash]
```

### After Navigation
```
🍽️ Lag'mon
Hand-pulled noodle soup...

📊 Hozirgi ovozlar: 5
📍 Taomlar: 2/3

✅ Ushbu taomga ovoz berishni tasdiqlaysizmi?

[⬅️ Oldingi] [2/3] [Keyingi ➡️]
[⬅️ Orqaga] [✅ Tasdiqlash]
```

---

## 7. Build Status

```
✅ BUILD SUCCESS
Total time: 4.782 s
Compiled: 37 source files
Warnings: Deprecation warnings only (safe)
Errors: 0
```

---

Developed: 2026-04-28
Technology Stack: Spring Boot 3.2.5, Apache POI, Telegram Bot API 6.8.0
Status: Production Ready ✅
