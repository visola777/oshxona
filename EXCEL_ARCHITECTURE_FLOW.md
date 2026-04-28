# Excel Integration - Architecture & Flow

## 🏗️ System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                      TELEGRAM BOT APPLICATION                   │
└─────────────────────────────────────────────────────────────────┘
                                  │
                    ┌─────────────┼─────────────┐
                    ▼             ▼             ▼
              ┌──────────┐  ┌──────────┐  ┌──────────┐
              │ Nonushta │  │ Poldnik  │  │ Poldnik  │
              │Category 1│  │Category 2│  │Category 3│
              └──────────┘  └──────────┘  └──────────┘
                    │             │             │
                    └─────────────┼─────────────┘
                                  │
                ┌─────────────────▼──────────────────┐
                │   DishMessageService               │
                │  (Message Formatting & Filtering)  │
                └─────────────────┬──────────────────┘
                                  │
                ┌─────────────────▼──────────────────┐
                │   ExcelDishService                 │
                │   (Excel Read/Write Operations)    │
                └─────────────────┬──────────────────┘
                                  │
        ┌─────────────────────────┼─────────────────────────┐
        │                                                     │
        ▼                                                     ▼
┌──────────────┐                               ┌──────────────────┐
│  PostgreSQL  │                               │  Excel File      │
│  Database    │                               │ (dishes.xlsx)    │
└──────────────┘                               └──────────────────┘
        ▲                                             ▲
        │                                             │
        └─────────────────┬──────────────────────────┘
                          │
                    ┌─────▼────────┐
                    │ AdminService │
                    │ (Add Dishes) │
                    └──────────────┘
```

## 🔄 Data Flow - User Perspective

```
┌─────────────────┐
│ User: /start    │
└────────┬────────┘
         │
         ▼
┌──────────────────────────────┐
│ TelegramMealVoteBot receives │
│ and processes command        │
└────────┬─────────────────────┘
         │
         ▼
┌──────────────────────────────┐
│ Bot sends category menu:     │
│ 🌅 Nonushta                  │
│ 🌤️ Poldnik                   │
│ 🌙 Poldnik                   │
└────────┬─────────────────────┘
         │
         ▼
┌────────────────────────┐
│ User clicks Nonushta   │
│ (Category: 1)          │
└────────┬───────────────┘
         │
         ▼
┌────────────────────────────────────────┐
│ ExcelDishService.readDishesFromExcel() │
│ Reads dishes.xlsx file                 │
└────────┬───────────────────────────────┘
         │
         ▼
┌────────────────────────────────────────┐
│ DishMessageService.getCategoryDishesMsg│
│ Filters for Category 1 (Nonushta)      │
│ Formats with name, photo, description  │
└────────┬───────────────────────────────┘
         │
         ▼
┌────────────────────────────────────────┐
│ Bot sends formatted message:           │
│ 🍽️ 🌅 Nonushta                         │
│ 📌 Osh - Plov with meat                │
│    🖼️ https://...                      │
│ 📌 Chuchvara - Dumplings               │
│    🖼️ https://...                      │
│ 📌 Manti - Large dumplings             │
│    🖼️ https://...                      │
└────────────────────────────────────────┘
```

## 🔄 Data Flow - Admin Perspective

```
┌─────────────────────────┐
│ Admin: /admin           │
└────────┬────────────────┘
         │
         ▼
┌──────────────────────────────┐
│ Admin panel shows options    │
│ - Add Food                   │
│ - Edit Food                  │
│ - Delete Food                │
│ - Exclude Food               │
└────────┬─────────────────────┘
         │
         ▼
┌──────────────────────────────┐
│ Admin selects: Add Food      │
└────────┬─────────────────────┘
         │
         ▼
┌──────────────────────────────┐
│ Admin enters:                │
│ - Name: "Plov"              │
│ - Category: 1 (Nonushta)    │
│ - Photo: https://...        │
│ - Description: "Plov dish"  │
└────────┬─────────────────────┘
         │
         ▼
┌──────────────────────────────┐
│ AdminService.addFood()       │
└────────┬─────────────────────┘
         │
    ┌────┴────┐
    │          │
    ▼          ▼
┌────────┐ ┌────────────────────┐
│Database│ │ExcelDishService    │
│save()  │ │.addDishToExcel()   │
└────────┘ └────────────────────┘
    │            │
    └────┬───────┘
         │
         ▼
┌──────────────────────────────┐
│ Admin sees confirmation:     │
│ ✅ Food added: Plov         │
└──────────────────────────────┘

Next time user selects category:
→ Excel is read
→ New "Plov" dish appears!
```

## 📊 Excel File Integration

```
┌──────────────────────────────────────────────────────┐
│         dishes.xlsx (Excel File Structure)           │
├──────────────────────────────────────────────────────┤
│ ID | Name | Category | Photo URL | Description | Active│
├────┼──────┼──────────┼───────────┼─────────────┼──────┤
│ 1  │ Osh  │    1     │ https://..│ Plov...     │ TRUE │
│ 2  │ Chuc │    1     │ https://..│ Dumplings...│ TRUE │
│ 3  │ Manti│    1     │ https://..│ Large...    │ TRUE │
│ 4  │ Norin│    2     │ https://..│ Noodle...   │ TRUE │
│... │  ... │   ...    │ ...       │ ...         │ ...  │
└──────────────────────────────────────────────────────┘
     ▲                                    ▲
     │                                    │
  Read by                            Updated by
  ExcelDishService              AdminService
```

## 🔌 Component Integration

```
┌─────────────────────────────────────────────────────────┐
│                 Application Startup                      │
└────────┬────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────┐
│ DataInitializer (CommandLineRunner)                     │
│ - Calls: excelDishService.initializeExcelFile()        │
│ - Loads: Default 9 dishes into database                │
└────────┬────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────┐
│ ExcelDishService                                        │
│ + readDishesFromExcel()                                │
│ + addDishToExcel(Dish)                                 │
│ + initializeExcelFile()                                │
│ + resetExcelFile()                                     │
│ + getCategoryName(String)                              │
└─────────────────────────────────────────────────────────┘
         ▲
         │
         Injected into
         │
         ├─ DishMessageService
         ├─ AdminService
         └─ Other Services
```

## 🎯 Request-Response Flow

### User Selects Breakfast:
```
Request Flow:
1. User: Click "🌅 Nonushta"
         ↓
2. Bot: handleCategorySelection(chatId, user, "1")
         ↓
3. ExcelDishService: readDishesFromExcel() → List<Dish>
         ↓
4. DishMessageService: getCategoryDishesMessage("1", "uz")
         ↓
5. Filter: Dishes where category == "1"
         ↓
6. Format: Build message with name, photo, description
         ↓
7. Bot: sendText(chatId, formattedMessage)
         ↓
Response: 🍽️ 🌅 Nonushta
          📌 Osh - Plov with meat - 🖼️ https://...
          📌 Chuchvara - Dumplings - 🖼️ https://...
          ...
```

## 🗂️ File Structure After Setup

```
demo2/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/example/demo/
│       │       ├── Service/
│       │       │   ├── ExcelDishService.java ✨ NEW
│       │       │   ├── DishMessageService.java ✨ NEW
│       │       │   ├── AdminService.java (UPDATED)
│       │       │   └── ... other services
│       │       └── DATA/
│       │           └── DataInitializer.java (UPDATED)
│       └── resources/
│           ├── application.properties
│           └── db/ ✨ NEW FOLDER
│               └── dishes.xlsx (Created at startup)
│
├── EXCEL_INTEGRATION_GUIDE_UZ.md ✨ NEW
├── EXCEL_INTEGRATION_GUIDE_EN.md ✨ NEW
├── EXCEL_QUICK_START_UZ.md ✨ NEW
├── EXCEL_API_DOCUMENTATION.md ✨ NEW
├── EXCEL_IMPLEMENTATION_REPORT_FINAL.md ✨ NEW
├── EXCEL_SETUP_SUMMARY.md ✨ NEW
└── ... other files
```

## 📈 Scaling & Performance

```
Concurrent Users → TelegramBot
        ↓
    Bot Handler (Multiple threads)
        ↓
    ExcelDishService.readDishesFromExcel()
        ↓
    ✅ Reads cached Excel file (Fast)
    ✅ Multiple concurrent reads allowed
    ✅ Minimal memory usage
        ↓
    DishMessageService (Formats in memory)
        ↓
    ✅ No database queries for display
    ✅ Fast response time
    ✅ Can handle many users
```

## 🔐 Data Consistency

```
When Admin Adds Food:

Step 1: AdminService.addFood()
   ├─ Validate input
   └─ Check if dish exists
   
Step 2: Save to Database
   └─ dishRepository.save(newDish)
   
Step 3: Save to Excel
   └─ excelDishService.addDishToExcel(newDish)
   
Result: ✅ Data in both places
        ✅ Synchronized immediately
        ✅ Next user request sees new dish
```

## 🎯 Category Distribution

```
User Requests Category 1 (Nonushta):
├─ Bot receives request
├─ ExcelDishService reads file
├─ Filter: .filter(d -> d.getCategory().equals("1"))
├─ Result: [Osh, Chuchvara, Manti]
└─ Format & Send ✅

User Requests Category 2 (Poldnik):
├─ Bot receives request
├─ ExcelDishService reads file
├─ Filter: .filter(d -> d.getCategory().equals("2"))
├─ Result: [Norin, Lag'man, Shurvak]
└─ Format & Send ✅

User Requests Category 3 (Poldnik):
├─ Bot receives request
├─ ExcelDishService reads file
├─ Filter: .filter(d -> d.getCategory().equals("3"))
├─ Result: [Kebab, Samsa, Tandir Bread]
└─ Format & Send ✅
```

---

**System is fully integrated and ready for production!** ✅
