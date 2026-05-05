# 🍽️ Daily Meal Vote Bot

> **Oshxona ovoz berish Telegram boti** — Spring Boot + PostgreSQL/H2 + Telegram Bot API yordamida qurilgan.

Korporativ oshxona uchun ovoz berish tizimi. Har kuni xodimlar nonushta, obed va poldnik uchun eng yoqtirgan taomga ovoz berishadi. Natijalar real vaqtda yangilanib, reyting ko'rsatiladi.

---

## 🆕 Excel orqali taomlarni boshqarish

**Barcha taom ma'lumotlari `excel/dishes.xlsx` faylida saqlanadi.** Bot ishga tushganda shu fayldan o'qib, bazaga yuklaydi. Sen excelni o'zgartirsang — bot menyusi ham shunga mos keladi.

### Excel formati (`excel/dishes.xlsx`)

| name | category | photoUrl | description |
|---|---|---|---|
| Shirguruch | BREAKFAST | https://example.com/shirguruch.jpg | Sut bilan pishirilgan shirin guruch |
| Lag'mon | LUNCH | https://example.com/lagmon.jpg | Go'sht va sabzavotli sho'rva |
| Sinabon | SNACK | https://example.com/sinabon.jpg | Darchinli bulochka |

**Ustunlar:**
- `name` — taom nomi (majburiy)
- `category` — `BREAKFAST` / `LUNCH` / `SNACK` (yoki `Nonushta` / `Obed` / `Poldnik`) — majburiy
- `photoUrl` — rasm URL (majburiy)
- `description` — taom tavsifi (majburiy)

### Qanday ishlatish

1. Botni birinchi marta ishga tushir — agar `excel/dishes.xlsx` yo'q bo'lsa, **avtomatik namuna fayl yaratiladi**
2. Excelni Excel/LibreOffice/Google Sheets da och va o'z taomlaringni yoz
3. Botni qayta ishga tushir — yangi ma'lumotlar avtomatik yuklanadi

> ⚠️ **Eslatma:** Bot har ishga tushganda eski taomlar tozalanadi va exceldan qaytadan yuklanadi. Ovozlar (votes) ham reset bo'ladi.

---

## 📋 Mundarija

- [Imkoniyatlar](#-imkoniyatlar)
- [Texnologiyalar](#-texnologiyalar)
- [O'rnatish va ishga tushirish](#-ornatish-va-ishga-tushirish)
- [Muhit o'zgaruvchilari](#-muhit-ozgaruvchilari)
- [Telegram buyruqlari](#-telegram-buyruqlari)
- [Ma'lumotlar bazasi](#-malumotlar-bazasi)
- [Loyiha tuzilmasi](#-loyiha-tuzilmasi)

---

## ✨ Imkoniyatlar

- **Excel orqali taomlarni boshqarish** — Bot kodiga tegmasdan taomlarni qo'shish/o'chirish
- **Kunlik ovoz berish** — 3 kategoriyada (Nonushta, Obed, Poldnik) bittadan ovoz
- **Sequential unlock** — Avval nonushta, keyin obed, keyin poldnik
- **Ko'p tilli interfeys** — O'zbek 🇺🇿 va Ingliz 🇬🇧
- **Shaxsiy tarix** — Oxirgi 30 kunlik shaxsiy ovozlar
- **Global reyting** — Eng ko'p ovoz to'plagan taomlar
- **Kunlik eslatma** — Soat 9:00 da ovoz bermaganlarga
- **Admin panel** — CSV eksport, reset, broadcast
- **Rasm ko'rsatish** — Har bir taom uchun rasm URL orqali

---

## 🛠 Texnologiyalar

| Texnologiya | Versiya | Maqsad |
|---|---|---|
| Java | 17 | Asosiy dasturlash tili |
| Spring Boot | 3.2.5 | Asosiy freymvork |
| Spring Data JPA | — | Ma'lumotlar bazasi bilan ishlash |
| Spring Scheduler | — | Kunlik eslatmalar |
| Telegram Bots API | 6.8.0 | Bot integratsiyasi |
| **Apache POI** | **5.2.5** | **Excel import** |
| PostgreSQL | — | Production ma'lumotlar bazasi |
| H2 (in-memory) | — | Development/Test uchun |
| Maven | — | Build tizimi |

---

## 🚀 O'rnatish va ishga tushirish

### Talablar

- Java 17+
- Maven 3.8+
- Telegram Bot tokeni (`@BotFather` orqali olingan)

### 1. Reponi klonlash

```bash
git clone https://github.com/sizning-username/oshxona.git
cd oshxona
```

### 2. Bot tokenini sozlash (Environment Variables)

Windows (CMD):
```cmd
set BOT_TOKEN=SIZNING_BOT_TOKEN
set BOT_USERNAME=SIZNING_BOT_USERNAME
set BOT_ADMIN_IDS=123456789
```

Linux/Mac:
```bash
export BOT_TOKEN=SIZNING_BOT_TOKEN
export BOT_USERNAME=SIZNING_BOT_USERNAME
export BOT_ADMIN_IDS=123456789
```

### 3. Birinchi marta ishga tushirish

```bash
./mvnw spring-boot:run
```

> 💡 Birinchi ishga tushirishda `excel/dishes.xlsx` namuna fayli avtomatik yaratiladi.
> Faylni o'zgartiring va botni qayta ishga tushiring.

### 4. Excel faylni tahrirlash

1. `excel/dishes.xlsx` faylni Excel'da oching
2. Ustunlarni to'ldiring (yuqoridagi formatga qarang)
3. Saqlang
4. Botni qayta ishga tushiring — yangi taomlar yuklanadi ✅

### 5. JAR sifatida build qilish

```bash
./mvnw clean package -DskipTests
java -jar target/meal-vote-bot-1.0.0.jar
```

---

## ⚙️ Muhit o'zgaruvchilari

| O'zgaruvchi | Majburiy | Tavsif | Misol |
|---|---|---|---|
| `BOT_TOKEN` | ✅ | Telegram bot tokeni | `123456:ABC-DEF...` |
| `BOT_USERNAME` | ✅ | Bot username (`@` siz) | `DailyMealVoteBot` |
| `BOT_ADMIN_IDS` | ❌ | Admin Telegram ID lari (vergul bilan) | `123456789,987654321` |
| `DISH_EXCEL_PATH` | ❌ | Excel fayl yo'li | `./excel/dishes.xlsx` |
| `BOT_PROFILE` | ❌ | Profil: `dev` yoki `prod` | `dev` |
| `bot.reminder-cron` | ❌ | Kunlik eslatma cron | `0 0 9 * * *` |
| `bot.change-deadline` | ❌ | Ovozni o'zgartirish vaqti | `13:00` |
| `bot.default-language` | ❌ | Standart til | `uz` yoki `en` |

---

## 💬 Telegram buyruqlari

### Foydalanuvchi buyruqlari

| Buyruq | Tavsif |
|---|---|
| `/start` | Asosiy menyuni ochish va botni boshlash |
| `/help` | Qoidalar va yordam |
| `/myvotes` | Oxirgi 30 kunlik shaxsiy ovozlar tarixi |
| `/top` | Global top 10 taomlar reytingi |

### Admin buyruqlari

| Buyruq | Tavsif |
|---|---|
| `/admin` | Admin panelini ko'rish |
| `/export` | Barcha ovozlarni CSV formatida yuklash |
| `/reset_today` | Bugungi barcha ovozlarni o'chirish |
| `/broadcast <xabar>` | Barcha foydalanuvchilarga xabar yuborish |

---

## 🗄 Ma'lumotlar bazasi

### `dishes` — Taomlar (exceldan import qilingan)
| Ustun | Tur | Tavsif |
|---|---|---|
| `id` | BIGINT PK | Auto-increment |
| `name` | VARCHAR (unique) | Taom nomi (exceldan) |
| `category` | VARCHAR | `BREAKFAST` / `LUNCH` / `SNACK` (exceldan) |
| `photo_url` | VARCHAR | Rasm URL (exceldan) |
| `description` | VARCHAR(1000) | Tavsif (exceldan) |
| `active` | BOOLEAN | Faol holati |
| `total_votes` | INT | Jami ovozlar soni |
| `created_at` | TIMESTAMP | Yaratilgan vaqt |

### `votes` — Ovozlar
| Ustun | Tur | Tavsif |
|---|---|---|
| `id` | BIGINT PK | Auto-increment |
| `user_id` | BIGINT | Telegram foydalanuvchi ID si |
| `dish_id` | BIGINT FK | Taom bog'lanishi |
| `category` | VARCHAR | Kategoriya nomi |
| `vote_date` | DATE | Ovoz berilgan sana |
| `voted_at` | TIMESTAMP | Aniq vaqt |

### `telegram_users` — Foydalanuvchilar
| Ustun | Tur | Tavsif |
|---|---|---|
| `id` | BIGINT PK | Auto-increment |
| `telegram_id` | BIGINT (unique) | Telegram ID |
| `username` | VARCHAR | Telegram username |
| `first_name` | VARCHAR | Ism |
| `language_code` | VARCHAR | Til kodi |
| `joined_at` | TIMESTAMP | Ro'yxatdan o'tgan vaqt |
| `admin` | BOOLEAN | Admin huquqi |

---

## 📁 Loyiha tuzilmasi

```
oshxona/
├── excel/
│   └── dishes.xlsx                            # 🆕 Taomlar Excel fayli
├── src/
│   ├── main/
│   │   ├── java/com/example/demo/
│   │   │   ├── bot/
│   │   │   │   ├── TelegramMealVoteBot.java   # Asosiy bot klassi
│   │   │   │   ├── BotMessages.java           # Ko'p tilli xabarlar
│   │   │   │   └── ScheduledTasks.java        # Kunlik eslatma
│   │   │   ├── config/
│   │   │   │   └── BotConfig.java             # Bot konfiguratsiyasi
│   │   │   ├── entity/
│   │   │   │   ├── Dish.java                  # Taom entity
│   │   │   │   ├── TelegramUser.java          # Foydalanuvchi entity
│   │   │   │   ├── Vote.java                  # Ovoz entity
│   │   │   │   └── VoteCategory.java          # Kategoriya enum
│   │   │   ├── repository/
│   │   │   │   ├── DishRepository.java
│   │   │   │   ├── TelegramUserRepository.java
│   │   │   │   └── VoteRepository.java
│   │   │   ├── Service/
│   │   │   │   ├── AdminService.java
│   │   │   │   ├── BotUserService.java
│   │   │   │   ├── DishService.java
│   │   │   │   ├── DishExcelImportService.java # 🆕 Excel import
│   │   │   │   ├── MealDishService.java
│   │   │   │   ├── StatisticsService.java
│   │   │   │   └── VotingService.java
│   │   │   ├── DATA/
│   │   │   │   └── DataInitializer.java       # 🆕 Excel orqali yuklash
│   │   │   └── Demo2Application.java
│   │   └── resources/
│   │       ├── application.properties         # Umumiy
│   │       ├── application-dev.properties     # Development
│   │       ├── application-prod.properties    # Production
│   │       └── application-test.properties    # Test
│   └── test/
│       └── java/com/example/demo/             # 6 ta test sinf
├── pom.xml
├── mvnw / mvnw.cmd
├── README.md
└── TEST_RESULTS.md
```

---

## 🐛 Muammolarni bartaraf etish

### Bot javob bermayapti
- `BOT_TOKEN` to'g'ri ekanligini tekshir
- `BOT_USERNAME` `@` belgisisiz yozilganligini tekshir
- Dastur to'liq ishga tushganligini DEBUG log dan kuzat

### Excel fayl o'qilmayapti
- `excel/dishes.xlsx` mavjud ekanligiga ishonch hosil qil
- Excelda 1-qator header (`name`, `category`, `photoUrl`, `description`) bo'lishi kerak
- Kategoriya: `BREAKFAST` / `LUNCH` / `SNACK` yoki o'zbekcha (`Nonushta` / `Obed` / `Poldnik`)
- Bo'sh maydonlar (`name`, `category`, `photoUrl`, `description` — to'rttasi ham) bo'lmasligi kerak

### Rasm ko'rinmayapti
- `photoUrl` haqiqiy va ochiladigan URL bo'lishi kerak (browserdan ochib tekshir)
- URL `https://` bilan boshlanishi kerak
- Telegram API faqat ba'zi formatlarni qabul qiladi: `.jpg`, `.png`

### Admin buyruqlari ishlamayapti
- `BOT_ADMIN_IDS` ga o'z Telegram ID ingizni qo'sh
- ID ni bilish uchun `@userinfobot` ga `/start` yuborish

---

## 📄 Litsenziya

MIT — batafsil `LICENSE` faylini ko'ring.

---

> **Daily Meal Vote Bot** | Spring Boot + Telegram Bot API + Apache POI
