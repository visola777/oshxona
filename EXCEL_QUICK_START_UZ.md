# Excel Integration - Quick Start (Tez Boshlash)

## 🎯 Nima Qilindi?

3 ta yangi Java classı yaratildi:

### 1. **ExcelDishService** 
- Excel fayli bilan ishlaydi
- Dishlari o'qiydi va yozadi
- Default ma'lumotlar bilan initsializatsiya qiladi

### 2. **DishMessageService**
- Excel-dan dishlari o'qiydi
- Telegram habarini formatlaydi
- HTML jadval yaratadi

### 3. **Database Folder** (`src/main/resources/db/`)
- `dishes.xlsx` fayli shu yerda sohralanadi

## 📦 Yo'riqlamalar Qadam bo Qadam

### **Qadam 1: Loyihani Build Qiling**
```bash
cd c:\Users\Hewlett Packard\IdeaProjects\demo2
mvn clean install
```

### **Qadam 2: Bot Ishga Tushiring**
```bash
java -jar target/meal-vote-bot-1.0.0.jar
```

Bot ishga tushganda:
- `db` papkasi avtomatik yaratiladi
- `dishes.xlsx` fayli yaratiladi
- Default 9 ta ovqat bilan to'ltiriladi

### **Qadam 3: Excel Faylini Ochib Ko'ring** ✅
Fayl yo'li:
```
src/main/resources/db/dishes.xlsx
```

Shu faylni Excel yoki Google Sheets-da ocha olasiz.

## 🤖 Bot Bilan Ishlash

### Foydalanuvchi Perspektivasi:

**User**: `/start`
```
Bot shows menu with categories:
- 🌅 Nonushta (Breakfast)
- 🌤️ Abetmi Poldnik (Lunch)
- 🌙 Poldnik (Dinner)
```

**User**: Clicks "🌅 Nonushta"
```
Bot reads from Excel → finds breakfast dishes
Bot sends:

🍽️ 🌅 Nonushta

📌 Osh
   📝 Plov with meat
   🖼️ https://example.com/osh.jpg

📌 Chuchvara
   📝 Traditional dumplings
   🖼️ https://example.com/chuchvara.jpg
```

### Admin Perspektivasi:

**Admin**: `/admin` → Click "Add Food"
```
Enters: Name, Category (1/2/3), Photo URL, Description
↓
Dish saved to Database ✅
Dish saved to Excel ✅
```

**Next User Request**:
```
Bot reads Excel → finds new dish → sends to user
```

## 📊 Excel Fayli Tuzilishi

```
Ustun 1: ID          (1, 2, 3...)
Ustun 2: Name        (Osh, Lag'man, Kebab...)
Ustun 3: Category    (1, 2, 3)
Ustun 4: Photo URL   (https://...)
Ustun 5: Description (Plov with meat...)
Ustun 6: Active      (TRUE/FALSE)
```

### Misollar:

| ID | Name | Category | Photo URL | Description | Active |
|----|------|----------|-----------|-------------|--------|
| 1 | Osh | 1 | https://example.com/osh.jpg | Plov | TRUE |
| 2 | Lag'man | 2 | https://example.com/lagman.jpg | Noodles | TRUE |
| 3 | Kebab | 3 | https://example.com/kebab.jpg | Grilled meat | TRUE |

## 🔧 Excel-ni Qo'lda O'zgartirish

1. Excel faylni oching: `src/main/resources/db/dishes.xlsx`
2. Dishlari tahrir qiling:
   - Nomi o'zgartiring
   - Kategoriyani o'zgartiring (1/2/3)
   - Rasm linkini o'zgartiring
   - Tavsifni o'zgartiring
3. Faylni saqlang
4. Bot-ni restart qiling
5. Yangi dishlari ko'rsatiladi

## 🎨 Kategoriyalar

```
1 = 🌅 Nonushta (Breakfast)
2 = 🌤️ Abetmi Poldnik (Lunch)
3 = 🌙 Poldnik (Dinner)
```

## 📝 Misolni Sinab Ko'rish

### Excel-dan Excelni O'qish:
```java
List<Dish> dishes = excelDishService.readDishesFromExcel();
System.out.println(dishes.size() + " dishlari o'qildi");
```

### Yangi Dish Qo'shish:
```java
Dish newDish = new Dish();
newDish.setName("Plov");
newDish.setCategory("1");
newDish.setPhotoUrl("https://example.com/plov.jpg");
newDish.setDescription("Traditional plov");
excelDishService.addDishToExcel(newDish);
```

### Kategoriya Uchun Habar:
```java
String message = dishMessageService.getCategoryDishesMessage("1", "uz");
System.out.println(message);
```

Output:
```
🍽️ 🌅 Nonushta

📌 Osh
   📝 Plov with meat
   🖼️ https://example.com/osh.jpg

📌 Chuchvara
   📝 Traditional dumplings
   🖼️ https://example.com/chuchvara.jpg
```

## ✨ Asosiy Xususiyatlar

✅ Avtomatik initsializatsiya (bot ishga tushganda)
✅ Database va Excel sinxronizatsiyasi
✅ Admin panelidan yangi dishlari qo'shish
✅ Kategoriya bo'ylab filtrlash
✅ Rasm va tavsif qo'llab-quvvatlash
✅ Manual Excel tahrirlash

## 🚀 Production Ready

Bu tizim production uchun tayyor. Admin mahsulot qo'shganda:
1. Database-ga sohralanadi
2. Excel-ga ham sohralanadi
3. User tanlasa, Excel-dan oqiladi
4. Formatlangan habar sifatida jo'natiladi

## 📞 Fayllar

Qo'shilgan fayllar:
- `ExcelDishService.java` - Excel operatsiyalari
- `DishMessageService.java` - Habar formatlash
- `EXCEL_INTEGRATION_GUIDE_EN.md` - Ingliz tilida yo'riqnama
- `EXCEL_INTEGRATION_GUIDE_UZ.md` - O'zbek tilida yo'riqnama
- `db/` papka - Excel fayli uchun

## 🎯 Tugallash

Barcha kerakli classlar yaratildi va integratsiya qilindi.
Excel faylini qo'l bilan tahrir qila olasiz.
Bot avtomatik ravishda Excel-dan ma'lumotlarni o'qiydi.

**Tayyor bo'ltingiz!** 🎉
