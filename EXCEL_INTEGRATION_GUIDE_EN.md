# Excel Integration Implementation Guide

## 📋 Overview

This integration adds Excel-based data management for meal/dish information with automatic synchronization between the database and Excel file.

### Category System
- **1** = 🌅 Breakfast (Nonushta)
- **2** = 🌤️ Lunch (Abetmi Poldnik)
- **3** = 🌙 Dinner (Poldnik)

## 📁 File Structure

```
src/main/resources/
└── db/
    └── dishes.xlsx
```

The Excel file is automatically created at startup with default data.

## 🔧 New Services

### 1. **ExcelDishService** - Core Excel Operations

**Methods:**
```java
// Initialize/Create Excel file
void initializeExcelFile()

// Read dishes from Excel
List<Dish> readDishesFromExcel()

// Add new dish to Excel
void addDishToExcel(Dish dish)

// Reset Excel file
void resetExcelFile()

// Get category name with emoji
String getCategoryName(String categoryCode)
```

**Usage Example:**
```java
@Autowired
private ExcelDishService excelDishService;

// Read all dishes from Excel
List<Dish> dishes = excelDishService.readDishesFromExcel();

// Add a new dish
Dish newDish = new Dish();
newDish.setName("New Dish");
newDish.setCategory("1");
newDish.setPhotoUrl("https://example.com/photo.jpg");
excelDishService.addDishToExcel(newDish);
```

### 2. **DishMessageService** - Message Formatting

**Methods:**
```java
// Get formatted message for a category
String getCategoryDishesMessage(String categoryCode, String languageCode)

// Get detailed message for a single dish
String getDishDetailMessage(String dishName)

// Get all dishes as HTML table
String getAllDishesAsHtmlTable()

// Get category name with emoji
String getCategoryName(String categoryCode)

// Sync Excel data to database
void syncExcelToDB(MealDishService dishService)
```

**Usage Example:**
```java
@Autowired
private DishMessageService dishMessageService;

// Get breakfast dishes as formatted message
String breakfastMessage = dishMessageService.getCategoryDishesMessage("1", "uz");
sendText(chatId, breakfastMessage, "uz");

// Get specific dish details
String dishDetail = dishMessageService.getDishDetailMessage("Osh");

// Get HTML table
String htmlTable = dishMessageService.getAllDishesAsHtmlTable();
```

## 📊 Excel File Format

| ID | Name | Category | Photo URL | Description | Active |
|-----|------|----------|-----------|-------------|--------|
| 1 | Osh | 1 | https://example.com/osh.jpg | Plov with meat | TRUE |
| 2 | Lag'man | 2 | https://example.com/lagman.jpg | Hand-pulled noodles | TRUE |
| 3 | Kebab | 3 | https://example.com/kebab.jpg | Grilled meat | TRUE |

## 🚀 Integration Points

### 1. **Bot Startup (DataInitializer)**
```java
// Automatically called on application startup
excelDishService.initializeExcelFile();
```

### 2. **Admin Adding Dish**
```java
// In AdminService.addFood()
Dish saved = dishRepository.save(newDish);
excelDishService.addDishToExcel(saved);  // Also saves to Excel
```

### 3. **Sending Dishes to User**
```java
// In Bot message handler
String categoryDishes = dishMessageService.getCategoryDishesMessage("1", "uz");
sendText(chatId, categoryDishes, "uz");
```

## 📝 Manual Excel Editing

Users can manually edit the Excel file at:
```
src/main/resources/db/dishes.xlsx
```

### Steps:
1. Open the Excel file in any spreadsheet application (Excel, Google Sheets, LibreOffice)
2. Edit rows with dish information
3. Keep the header row unchanged
4. Save the file
5. Restart the bot to reload changes

## 🔄 Workflow

### User Flow:
```
User selects category (e.g., Breakfast)
    ↓
Bot reads from Excel file
    ↓
Bot fetches dishes from category
    ↓
Bot formats message with name, photo URL, description
    ↓
Bot sends message to user
```

### Admin Flow:
```
Admin adds new dish via /admin command
    ↓
Dish saved to database
    ↓
Dish automatically saved to Excel
    ↓
On next user request, new dish is available
```

## 💾 Default Data

**Breakfast (1):**
- Osh
- Chuchvara
- Manti

**Lunch (2):**
- Norin
- Lag'man
- Shurvak

**Dinner (3):**
- Kebab
- Samsa
- Tandir Bread

## ⚙️ Configuration

### Modify Excel Path:
In `ExcelDishService.java`:
```java
private static final String EXCEL_PATH = "src/main/resources/db/dishes.xlsx";
```

### Modify Category Names:
In `ExcelDishService.java`:
```java
public static final String CATEGORY_BREAKFAST = "1";
public static final String CATEGORY_LUNCH = "2";
public static final String CATEGORY_DINNER = "3";
```

## 🛠️ Dependencies

The project uses Apache POI (already included in pom.xml):
```xml
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>5.2.5</version>
</dependency>
```

## 📊 Example Bot Message Output

### Breakfast Selection:
```
🍽️ 🌅 Nonushta

📌 Osh
   📝 Plov with meat
   🖼️ https://example.com/osh.jpg

📌 Chuchvara
   📝 Traditional dumplings
   🖼️ https://example.com/chuchvara.jpg

📌 Manti
   📝 Large dumplings
   🖼️ https://example.com/manti.jpg
```

### Dinner Selection:
```
🍽️ 🌙 Poldnik

📌 Kebab
   📝 Grilled meat
   🖼️ https://example.com/kebab.jpg

📌 Samsa
   📝 Fried pastry
   🖼️ https://example.com/samsa.jpg

📌 Tandir Bread
   📝 Traditional bread
   🖼️ https://example.com/bread.jpg
```

## ✨ Features

✅ **Automatic Initialization** - Excel created with default data on startup
✅ **Dual Storage** - Data synced to both database and Excel
✅ **Category Filtering** - Separate meals by breakfast/lunch/dinner
✅ **Rich Content** - Support for name, photo URL, and description
✅ **Easy Editing** - Manual Excel editing capability
✅ **Admin Integration** - New dishes automatically added to Excel
✅ **Message Formatting** - Pre-formatted messages ready to send

## 🔗 Related Classes

- `ExcelDishService` - Excel operations
- `DishMessageService` - Message formatting
- `AdminService` - Admin operations (updated to use Excel)
- `DataInitializer` - Startup initialization (updated)
- `MealDishService` - Dish management

---

**Status**: Production Ready ✅
