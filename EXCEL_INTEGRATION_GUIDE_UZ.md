# Excel Integration Guide - Ovqat Bot uchun

## 📋 O'zgarishlar Tavsifi

Siz so'ragan Excel integratsiyasi quyidagilarni o'z ichiga oladi:

### 1. **Kategoriya Tizimi**
- **1** = 🌅 **Nonushta** (Breakfast)
- **2** = 🌤️ **Abetmi Poldnik** (Lunch)  
- **3** = 🌙 **Poldnik** (Dinner)

### 2. **Excel Fayli Strukturasi**

Excel fayli `src/main/resources/db/dishes.xlsx` ga saqllanadi va quyidagi ustunlarga ega:

| ID | Name | Category | Photo URL | Description | Active |
|-----|------|----------|-----------|-------------|--------|
| 1 | Osh | 1 | https://... | Plov with meat | TRUE |
| 2 | Norin | 2 | https://... | Noodle with meat | TRUE |
| 3 | Kebab | 3 | https://... | Grilled meat | TRUE |

## 🚀 Ishlatish Yo'li

### **Qadam 1: Bot Ishga Tushganda**
- Bot ishga tushganda `ExcelDishService` avtomatik ravishda `dishes.xlsx` faylini yaratadi
- Default ma'lumotlar bilan to'ltiriladi (9 ta ovqat namuna)

### **Qadam 2: User Nonushta/Poldnik Tanlasa**
- User kassa tugmasini bosganda, bot Excel-dan ma'lumotlarni o'qiydi
- O'sha kategoriya uchun barcha dishlari formatlab jo'natadi
- Har bir dishin nomi, tavsifi va rasm linki ko'rsatiladi

### **Qadam 3: Admin Yangi Ovqat Qo'shsa**
```
/admin → "Add Food" → Nomi, Kategoriya, Rasm linki, Tavsif
```
- Yangi ovqat **database-ga** va **Excel-ga** birga sohralanadi
- Keyingi marta bot ishga tushganda Excel-dan o'qiladi

## 📝 Excel Fayli Qo'lda Tahrirlash

Agar siz Excel-ni tekis ilovada ochib tahrirlamoqchi bo'lsangiz:

1. **Fayl yo'li**: `src/main/resources/db/dishes.xlsx`
2. **Ustunlar**:
   - **ID**: Raqam (o'zgartirilmaydi)
   - **Name**: Ovqat nomi (masalan: "Osh", "Lag'man")
   - **Category**: 1, 2 yoki 3
   - **Photo URL**: Rasm havolasi (masalan: `https://example.com/photo.jpg`)
   - **Description**: Tavsif (masalan: "Plov with meat")
   - **Active**: TRUE yoki FALSE

### Misol:
```
ID | Name      | Category | Photo URL                      | Description           | Active
1  | Osh       | 1        | https://example.com/osh.jpg   | Plov with meat        | TRUE
2  | Lag'man   | 2        | https://example.com/lagman.jpg| Hand-pulled noodles   | TRUE
3  | Samsa     | 3        | https://example.com/samsa.jpg | Fried pastry          | TRUE
```

## 🔧 Kod Integratsiyasi

### **ExcelDishService** - Excel bilan ishlash
```java
// Excel-dan dishlari o'qish
List<Dish> dishes = excelDishService.readDishesFromExcel();

// Yangi dish qo'shish
excelDishService.addDishToExcel(newDish);

// Excel-ni reset qilish
excelDishService.resetExcelFile();
```

### **DishMessageService** - Habar formatlagich
```java
// Kategoriya uchun dishlari o'qib habar yaratish
String message = dishMessageService.getCategoryDishesMessage("1", "uz");

// Bitta dish uchun detali habar
String detail = dishMessageService.getDishDetailMessage("Osh");

// Barcha dishlari HTML jadval sifatida
String htmlTable = dishMessageService.getAllDishesAsHtmlTable();
```

## 🤖 Bot Xabarlari

### Nonushta Tanlasa:
```
🍽️ 🌅 Nonushta

📌 Osh
   📝 Plov with meat
   🖼️ https://example.com/osh.jpg

📌 Chuchvara
   📝 Traditional dumplings
   🖼️ https://example.com/chuchvara.jpg
```

### Poldnik Tanlasa:
```
🍽️ 🌙 Poldnik

📌 Kebab
   📝 Grilled meat
   🖼️ https://example.com/kebab.jpg

📌 Samsa
   📝 Fried pastry
   🖼️ https://example.com/samsa.jpg
```

## 📊 Xususiyatlar

✅ **Avtomatik Initsializatsiya**
- Bot ishga tushganda Excel avtomatik yaratiladi

✅ **Database va Excel Sinxronizatsiyasi**
- Admin yangi ovqat qo'shganda, ikkala joyga ham sohralanadi

✅ **Kategoriya Bilan Filtrlash**
- Her bir kategoriya uchun ovqatlar alohida ko'rsatiladi

✅ **Rasm va Tavsif Qo'llab-Quvvatlash**
- Har bir ovqatning rasm linki va tavsifi bor

✅ **HTML Jadval Export**
- Admin panelida Excel ma'lumotlarini HTML jadval ko'rinishida ko'rish mumkin

## 🛠️ Konfiguratsiya

Excel fayli yo'lini o'zgartirish uchun `ExcelDishService.java` faylida:
```java
private static final String EXCEL_PATH = "src/main/resources/db/dishes.xlsx";
```

## 📞 Qo'shimcha

Excel faylni o'zingiz yaratishni xohlamasangiz, bot avtomatik ravishda yaratib default ma'lumotlar bilan to'ltirib beradi.

### Default Dishlari:
**Nonushta (1):**
- Osh
- Chuchvara
- Manti

**Abetmi Poldnik (2):**
- Norin
- Lag'man
- Shurvak

**Poldnik (3):**
- Kebab
- Samsa
- Tandir Bread

---

**Tayyorlik xizmati**: Bu tizim ishg tayyorlik topshiriq. Admin yangi mahsulot qo'shganda va user tanlaganda, Excel-dan ma'lumotlar olinadi va formatlangan habar sifatida jo'natiladi.
