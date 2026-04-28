# Excel Services API Documentation

## 📚 ExcelDishService API

### Class Location
```
com.example.demo.Service.ExcelDishService
```

### Constructor
```java
public ExcelDishService(/* auto-injected */)
```

### Public Methods

#### 1. Initialize Excel File
```java
public void initializeExcelFile()
```
**Purpose**: Creates Excel file with default data if it doesn't exist
**Called**: On application startup (DataInitializer)
**Parameters**: None
**Returns**: void

**Example**:
```java
excelDishService.initializeExcelFile();
```

---

#### 2. Read Dishes from Excel
```java
public List<Dish> readDishesFromExcel()
```
**Purpose**: Read all dishes from the Excel file
**Parameters**: None
**Returns**: `List<Dish>` - List of all dishes from Excel

**Example**:
```java
List<Dish> dishes = excelDishService.readDishesFromExcel();
dishes.forEach(dish -> {
    System.out.println(dish.getName() + " - " + dish.getCategory());
});
```

---

#### 3. Add Dish to Excel
```java
public void addDishToExcel(Dish dish)
```
**Purpose**: Add a new dish to Excel file
**Parameters**: 
- `dish` (Dish) - The dish object to add

**Returns**: void

**Example**:
```java
Dish newDish = new Dish();
newDish.setId(1L);
newDish.setName("Palov");
newDish.setCategory("1"); // Breakfast
newDish.setPhotoUrl("https://example.com/palov.jpg");
newDish.setDescription("Traditional Uzbek plov");
newDish.setActive(true);

excelDishService.addDishToExcel(newDish);
```

---

#### 4. Reset Excel File
```java
public void resetExcelFile()
```
**Purpose**: Delete and recreate Excel file with default data
**Parameters**: None
**Returns**: void

**Example**:
```java
excelDishService.resetExcelFile(); // Clears all data and reloads defaults
```

---

#### 5. Get Category Name
```java
public String getCategoryName(String categoryCode)
```
**Purpose**: Get human-readable category name with emoji
**Parameters**: 
- `categoryCode` (String) - Category code: "1", "2", or "3"

**Returns**: String - Category name with emoji

**Example**:
```java
String categoryName = excelDishService.getCategoryName("1");
// Returns: "🌅 Nonushta"

String categoryName = excelDishService.getCategoryName("2");
// Returns: "🌤️ Abetmi Poldnik"

String categoryName = excelDishService.getCategoryName("3");
// Returns: "🌙 Poldnik"
```

---

## 📚 DishMessageService API

### Class Location
```
com.example.demo.Service.DishMessageService
```

### Constructor
```java
public DishMessageService(ExcelDishService excelDishService, MealDishService mealDishService)
```

### Public Methods

#### 1. Get Category Dishes Message
```java
public String getCategoryDishesMessage(String categoryCode, String languageCode)
```
**Purpose**: Get formatted message with all dishes in a category
**Parameters**: 
- `categoryCode` (String) - "1", "2", or "3"
- `languageCode` (String) - Language code (e.g., "uz", "en")

**Returns**: String - Formatted message ready to send to Telegram

**Example**:
```java
String message = dishMessageService.getCategoryDishesMessage("1", "uz");
sendText(chatId, message, "uz");

// Output:
// 🍽️ 🌅 Nonushta
//
// 📌 Osh
//    📝 Plov with meat
//    🖼️ https://example.com/osh.jpg
//
// 📌 Chuchvara
//    📝 Traditional dumplings
//    🖼️ https://example.com/chuchvara.jpg
```

---

#### 2. Get Dish Detail Message
```java
public String getDishDetailMessage(String dishName)
```
**Purpose**: Get detailed message for a single dish
**Parameters**: 
- `dishName` (String) - Name of the dish

**Returns**: String - Detailed dish information

**Example**:
```java
String detail = dishMessageService.getDishDetailMessage("Osh");
// Output:
// 🍽️ Osh
// 📂 Kategoriya: 🌅 Nonushta
// 📝 Tavsif: Plov with meat
// 🖼️ Rasm: https://example.com/osh.jpg
// ✅ Faol: Ha
```

---

#### 3. Get All Dishes as HTML Table
```java
public String getAllDishesAsHtmlTable()
```
**Purpose**: Generate HTML table with all dishes from Excel
**Parameters**: None
**Returns**: String - HTML table

**Example**:
```java
String htmlTable = dishMessageService.getAllDishesAsHtmlTable();
// Returns HTML like:
// <table border='1' cellpadding='10' cellspacing='0'>
// <tr><th>Nomi</th><th>Kategoriya</th><th>Tavsif</th><th>Rasm</th></tr>
// <tr><td>Osh</td><td>🌅 Nonushta</td><td>Plov with meat</td><td><a href='...'>Rasm</a></td></tr>
// ...
// </table>
```

---

#### 4. Get Category Name
```java
public String getCategoryName(String categoryCode)
```
**Purpose**: Get category name with emoji
**Parameters**: 
- `categoryCode` (String) - "1", "2", or "3"

**Returns**: String - Category name with emoji

**Example**:
```java
String name = dishMessageService.getCategoryName("1");
// Returns: "🌅 Nonushta"
```

---

#### 5. Sync Excel to Database
```java
public void syncExcelToDB(MealDishService dishService)
```
**Purpose**: Read dishes from Excel and save to database
**Parameters**: 
- `dishService` (MealDishService) - Service to save dishes

**Returns**: void

**Example**:
```java
dishMessageService.syncExcelToDB(mealDishService);
// Reads all dishes from Excel and saves to database
```

---

## 🔄 Integration Examples

### In Bot Message Handler
```java
@Autowired
private DishMessageService dishMessageService;

// When user clicks breakfast button
private void handleBreakfastSelection(Long chatId, String languageCode) {
    String message = dishMessageService.getCategoryDishesMessage("1", languageCode);
    sendText(chatId, message, languageCode);
}

// When user clicks lunch button
private void handleLunchSelection(Long chatId, String languageCode) {
    String message = dishMessageService.getCategoryDishesMessage("2", languageCode);
    sendText(chatId, message, languageCode);
}

// When user clicks dinner button
private void handleDinnerSelection(Long chatId, String languageCode) {
    String message = dishMessageService.getCategoryDishesMessage("3", languageCode);
    sendText(chatId, message, languageCode);
}
```

### In Admin Service
```java
@Autowired
private ExcelDishService excelDishService;

public AdminResult addFood(String name, String category, String photoUrl, String description) {
    Dish newDish = new Dish();
    newDish.setName(name);
    newDish.setCategory(category);
    newDish.setPhotoUrl(photoUrl);
    newDish.setDescription(description);
    newDish.setActive(true);
    
    // Save to database
    Dish saved = dishRepository.save(newDish);
    
    // Also save to Excel
    excelDishService.addDishToExcel(saved);
    
    return new AdminResult(true, "✅ Dish added: " + name);
}
```

### In REST Controller
```java
@Autowired
private DishMessageService dishMessageService;

@GetMapping("/api/dishes/breakfast")
public ResponseEntity<String> getBreakfastDishes() {
    String message = dishMessageService.getCategoryDishesMessage("1", "uz");
    return ResponseEntity.ok(message);
}

@GetMapping("/api/dishes/all/table")
public ResponseEntity<String> getAllDishesTable() {
    String htmlTable = dishMessageService.getAllDishesAsHtmlTable();
    return ResponseEntity.ok(htmlTable);
}

@GetMapping("/api/dishes/{name}")
public ResponseEntity<String> getDishDetail(@PathVariable String name) {
    String detail = dishMessageService.getDishDetailMessage(name);
    return ResponseEntity.ok(detail);
}
```

---

## 📋 Constants

### Category Codes
```java
public static final String CATEGORY_BREAKFAST = "1";  // 🌅 Nonushta
public static final String CATEGORY_LUNCH = "2";      // 🌤️ Abetmi Poldnik
public static final String CATEGORY_DINNER = "3";     // 🌙 Poldnik
```

### Excel Path
```java
private static final String EXCEL_PATH = "src/main/resources/db/dishes.xlsx";
private static final String SHEET_NAME = "Dishes";
```

---

## 🛠️ Error Handling

All methods include try-catch error handling:
- **IOException**: When file operations fail
- **CellTypeMismatch**: When Excel cell types don't match expected types
- **NullPointerException**: When data is missing

**Examples**:
```java
try {
    List<Dish> dishes = excelDishService.readDishesFromExcel();
} catch (IOException e) {
    log.error("Error reading Excel: {}", e.getMessage());
}
```

---

## 💾 File Locations

- **Excel File**: `src/main/resources/db/dishes.xlsx`
- **Service Classes**: 
  - `com.example.demo.Service.ExcelDishService`
  - `com.example.demo.Service.DishMessageService`
- **Configuration**: `src/main/resources/application.properties`

---

## 📊 Default Sample Data

**Breakfast (Category 1):**
```
| ID | Name | Category | Photo URL | Description |
| 1 | Osh | 1 | https://via.placeholder.com/200?text=Osh | Plov with meat |
| 2 | Chuchvara | 1 | https://via.placeholder.com/200?text=Chuchvara | Traditional dumplings |
| 3 | Manti | 1 | https://via.placeholder.com/200?text=Manti | Large dumplings |
```

**Lunch (Category 2):**
```
| ID | Name | Category | Photo URL | Description |
| 4 | Norin | 2 | https://via.placeholder.com/200?text=Norin | Noodle with meat |
| 5 | Lag'man | 2 | https://via.placeholder.com/200?text=Lagman | Hand-pulled noodles |
| 6 | Shurvak | 2 | https://via.placeholder.com/200?text=Shurvak | Meat stew |
```

**Dinner (Category 3):**
```
| ID | Name | Category | Photo URL | Description |
| 7 | Kebab | 3 | https://via.placeholder.com/200?text=Kebab | Grilled meat |
| 8 | Samsa | 3 | https://via.placeholder.com/200?text=Samsa | Fried pastry |
| 9 | Tandir Bread | 3 | https://via.placeholder.com/200?text=Bread | Traditional bread |
```

---

## ✅ Checklist

- ✅ ExcelDishService created and functional
- ✅ DishMessageService created and functional
- ✅ DataInitializer updated to use Excel
- ✅ AdminService updated to save to Excel
- ✅ db folder created in resources
- ✅ Default Excel file structure defined
- ✅ API documentation created
- ✅ Ready for production use

---

**Last Updated**: April 28, 2026
**Status**: Production Ready ✅
