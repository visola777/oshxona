# 🍽️ Daily Meal Vote Bot

> **Oshxona ovoz berish Telegram boti** — Spring Boot + PostgreSQL/H2 + Telegram Bot API yordamida qurilgan.

Korporativ oshxona uchun ovoz berish tizimi. Har kuni xodimlar nonushta, obed va poldnik uchun eng yoqtirgan taomga ovoz berishadi. Natijalar real vaqtda yangilanib, reyting ko'rsatiladi.

---

## 📋 Mundarija

- [Imkoniyatlar](#-imkoniyatlar)
- [Texnologiyalar](#-texnologiyalar)
- [Arxitektura](#-arxitektura)
- [O'rnatish va ishga tushirish](#-ornatish-va-ishga-tushirish)
- [Muhit o'zgaruvchilari](#-muhit-ozgaruvchilari)
- [Telegram buyruqlari](#-telegram-buyruqlari)
- [API / Callback ma'lumotlari](#-api--callback-malumotlari)
- [Ma'lumotlar bazasi](#-malumotlar-bazasi)
- [Scheduled Tasks](#-scheduled-tasks)
- [Admin panel](#-admin-panel)
- [Loyiha tuzilmasi](#-loyiha-tuzilmasi)

---

## ✨ Imkoniyatlar

- **Kunlik ovoz berish** — Har bir foydalanuvchi kuniga 3 kategoriyada (Nonushta, Obed, Poldnik) bittadan ovoz bera oladi
- **Ovozni o'zgartirish** — Soat 11:00 gacha ovozni qayta o'zgartirish mumkin
- **Ko'p tilli interfeys** — O'zbek 🇺🇿 va Ingliz 🇬🇧 tillari qo'llab-quvvatlanadi
- **Shaxsiy tarix** — Foydalanuvchi o'zining oxirgi 30 kunlik ovozlarini ko'ra oladi
- **Global reyting** — Barcha vaqt ichida eng ko'p ovoz to'plagan taomlar ro'yxati
- **Kunlik xabar-eslatma** — Soat 9:00 da ovoz bermaganlarга eslatma yuboriladi
- **Admin panel** — CSV eksport, ovozlarni reset qilish, broadcast xabar yuborish
- **Rasm ko'rsatish** — Har bir taom uchun rasm URL orqali yuboriladi

---

## 🛠 Texnologiyalar

| Texnologiya | Versiya | Maqsad |
|---|---|---|
| Java | 17 | Asosiy dasturlash tili |
| Spring Boot | 4.0.5 | Asosiy freymvork |
| Spring Data JPA | — | Ma'lumotlar bazasi bilan ishlash |
| Spring Scheduler | — | Kunlik eslatmalar |
| Telegram Bots API | 6.8.0 | Bot integratsiyasi |
| PostgreSQL | — | Production ma'lumotlar bazasi |
| H2 (in-memory) | — | Development/Test uchun |
| Lombok | 1.18.30 | Boilerplate kodni kamaytirish |
| Maven | — | Build tizimi |

---

## 🏗 Arxitektura

```
┌──────────────────────────────────────────────────────┐
│                  Telegram Servers                     │
└──────────────────────┬───────────────────────────────┘
                       │ Long Polling
                       ▼
┌──────────────────────────────────────────────────────┐
│           TelegramMealVoteBot (Bot Layer)             │
│  - onUpdateReceived()                                 │
│  - handleTextMessage() / handleCallbackQuery()        │
└──────┬────────────┬──────────────┬────────────────────┘
       │            │              │
       ▼            ▼              ▼
┌──────────┐ ┌──────────┐ ┌──────────────────┐
│BotUser   │ │Voting    │ │Statistics        │
│Service   │ │Service   │ │Service           │
└──────────┘ └──────────┘ └──────────────────┘
       │            │              │
       └────────────┴──────────────┘
                    │
                    ▼
┌──────────────────────────────────────────────────────┐
│              Repository Layer (Spring Data JPA)      │
│  TelegramUserRepository | VoteRepository             │
│  DishRepository         | FoodRepository             │
└──────────────────────────────────────────────────────┘
                    │
                    ▼
┌──────────────────────────────────────────────────────┐
│         H2 (dev) / PostgreSQL (production)           │
└──────────────────────────────────────────────────────┘
```

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

### 2. Bot tokenini sozlash

`src/main/resources/application.properties` faylida yoki muhit o'zgaruvchisi orqali:

```properties
bot.token=SIZNING_BOT_TOKEN
bot.username=SIZNING_BOT_USERNAME
bot.admin-ids=123456789,987654321
```

### 3. H2 (development) bilan ishga tushirish

```bash
./mvnw spring-boot:run
```

### 4. PostgreSQL (production) bilan ishga tushirish

`application.properties` faylida H2 o'rniga PostgreSQL ulanishini yozing:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/mealvotebot
spring.datasource.username=postgres
spring.datasource.password=sizning_parol
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update
```

Keyin:

```bash
./mvnw spring-boot:run
```

### 5. JAR sifatida build qilish

```bash
./mvnw clean package -DskipTests
java -jar target/demo2-0.0.1-SNAPSHOT.jar
```

---

## ⚙️ Muhit o'zgaruvchilari

| O'zgaruvchi | Majburiy | Tavsif | Misol |
|---|---|---|---|
| `BOT_TOKEN` | ✅ | Telegram bot tokeni | `123456:ABC-DEF...` |
| `BOT_USERNAME` | ✅ | Bot username (`@` siz) | `DailyMealVoteBot` |
| `BOT_ADMIN_IDS` | ❌ | Admin Telegram ID lari (vergul bilan) | `123456789,987654321` |
| `bot.reminder-cron` | ❌ | Kunlik eslatma cron iborasi | `0 0 9 * * *` (soat 9:00) |
| `bot.change-deadline` | ❌ | Ovozni o'zgartirish vaqti | `11:00` |
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

## 📡 API / Callback ma'lumotlari

Bot callback query tizimi orqali ishlaydi. Har bir tugma bosilganda quyidagi ma'lumotlar uzatiladi:

| Callback data | Tavsif |
|---|---|
| `MENU` | Asosiy menyuga qaytish |
| `CATEGORY:<nom>` | Kategoriya taomlarini ko'rsatish (`BREAKFAST`, `LUNCH`, `SNACK`) |
| `DISH:<id>` | Taom tafsilotlarini ko'rsatish |
| `VOTE:<id>` | Taomga ovoz berish |
| `CHANGE:<id>` | Ovozni boshqa taomga o'zgartirish |
| `MY_VOTES` | Shaxsiy ovoz tarixi |
| `GLOBAL_TOP` | Global reyting |
| `ADMIN:*` | Admin paneli |

---

## 🗄 Ma'lumotlar bazasi

### Jadvallar

#### `dishes` — Taomlar
| Ustun | Tur | Tavsif |
|---|---|---|
| `id` | BIGINT PK | Auto-increment |
| `name` | VARCHAR (unique) | Taom nomi |
| `category` | VARCHAR | `BREAKFAST` / `LUNCH` / `SNACK` |
| `photo_url` | VARCHAR | Rasm URL manzili |
| `description` | VARCHAR(1000) | Qo'shimcha tavsif |
| `active` | BOOLEAN | Faol/nofaol holati |
| `total_votes` | INT | Jami ovozlar soni |
| `created_at` | TIMESTAMP | Yaratilgan vaqt |

#### `votes` — Ovozlar
| Ustun | Tur | Tavsif |
|---|---|---|
| `id` | BIGINT PK | Auto-increment |
| `user_id` | BIGINT | Telegram foydalanuvchi ID si |
| `dish_id` | BIGINT FK | Taom bog'lanishi |
| `food_name` | VARCHAR | Taom nomi (qo'shimcha) |
| `category` | VARCHAR | Kategoriya nomi |
| `vote_date` | DATE | Ovoz berilgan sana |
| `voted_at` | TIMESTAMP | Aniq vaqt |

#### `telegram_users` — Foydalanuvchilar
| Ustun | Tur | Tavsif |
|---|---|---|
| `id` | BIGINT PK | Auto-increment |
| `telegram_id` | BIGINT (unique) | Telegram ID |
| `username` | VARCHAR | Telegram username |
| `first_name` | VARCHAR | Ism |
| `last_name` | VARCHAR | Familiya |
| `language_code` | VARCHAR | Til kodi (`uz`, `en`, ...) |
| `joined_at` | TIMESTAMP | Ro'yxatdan o'tgan vaqt |
| `last_seen_at` | TIMESTAMP | Oxirgi faollik vaqti |
| `admin` | BOOLEAN | Admin huquqi |

#### `foods` — Taomlar (eski jadval)
| Ustun | Tur | Tavsif |
|---|---|---|
| `id` | BIGINT PK | Auto-increment |
| `name` | VARCHAR (unique) | Taom nomi |
| `category` | VARCHAR | Kategoriya |
| `image_url` | VARCHAR | Rasm URL |
| `votes` | INT | Ovozlar |

---

## ⏰ Scheduled Tasks

| Task | Cron | Tavsif |
|---|---|---|
| `sendDailyReminder` | `0 0 9 * * *` (sozlanadi) | Har kuni soat 9:00 da ovoz bermaganlarga eslatma |

Cron ifodasi `application.properties` da `bot.reminder-cron` orqali o'zgartiriladi.

---

## 🔧 Admin panel

Admin bo'lish uchun `BOT_ADMIN_IDS` ga Telegram ID qo'shing. Adminlar quyidagilarga ega:

- **CSV export** — `/export` buyrug'i bilan barcha ovozlar CSV fayli sifatida keladi
- **Ovozlarni reset** — `/reset_today` bilan bugungi ovozlar o'chiriladi va taomlarning `totalVotes` maydonlari kamaytiriladi
- **Broadcast** — `/broadcast Xabaringiz` bilan barcha foydalanuvchilarga xabar yuboriladi

---

## 📁 Loyiha tuzilmasi

```
oshxona/
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
│   │   │   │   ├── Food.java                  # Eski taom entity
│   │   │   │   ├── TelegramUser.java          # Foydalanuvchi entity
│   │   │   │   ├── Vote.java                  # Ovoz entity
│   │   │   │   └── VoteCategory.java          # Kategoriya enum
│   │   │   ├── repository/
│   │   │   │   ├── DishRepository.java        # Taomlar uchun JPA
│   │   │   │   ├── FoodRepository.java        # Eski taomlar JPA
│   │   │   │   ├── TelegramUserRepository.java
│   │   │   │   └── VoteRepository.java        # Ovozlar JPA
│   │   │   ├── Service/
│   │   │   │   ├── AdminService.java          # Admin amallari
│   │   │   │   ├── BotUserService.java        # Foydalanuvchi boshqaruvi
│   │   │   │   ├── DishService.java           # Taomlar xizmati
│   │   │   │   ├── FoodService.java           # Eski taomlar xizmati
│   │   │   │   ├── MealDishService.java       # Taom va kategoriya xizmati
│   │   │   │   ├── StatisticsService.java     # Statistika va reyting
│   │   │   │   ├── UserService.java           # Foydalanuvchi
│   │   │   │   ├── VoteService.java           # Ovoz asosiy xizmati
│   │   │   │   └── VotingService.java         # Ovoz mantiqiy qatlami
│   │   │   ├── DATA/
│   │   │   │   └── DataInitializer.java       # Boshlang'ich ma'lumotlar
│   │   │   └── Demo2Application.java          # Dastur kirish nuqtasi
│   │   └── resources/
│   │       └── application.properties         # Konfiguratsiya
│   └── test/
│       └── java/com/example/demo/
│           └── Demo2ApplicationTests.java
├── pom.xml                                    # Maven konfiguratsiyasi
├── mvnw / mvnw.cmd                            # Maven wrapper
└── README.md                                  # Bu fayl
```

---

## 🐛 Muammolarni bartaraf etish

### Bot javob bermayapti
- `bot.token` to'g'ri ekanligini tekshiring
- `bot.username` `@` belgisisiz yozilganligini tekshiring
- Dastur to'liq ishga tushganligini `DEBUG` log dan kuzating

### Ma'lumotlar bazasi xatosi
- H2 da muammo bo'lsa `spring.jpa.hibernate.ddl-auto=create-drop` qiling va qayta ishga tushiring
- PostgreSQL da sxema yaratilganligini tekshiring

### Admin buyruqlari ishlamayapti
- `BOT_ADMIN_IDS` ga o'z Telegram ID ingizni qo'shing
- ID ni bilish uchun `@userinfobot` botiga `/start` yuboring

---

## 📄 Litsenziya

Bu loyiha MIT litsenziyasi ostida tarqatiladi. Batafsil ma'lumot uchun `LICENSE` faylini ko'ring.

---

> Tayyorlandi: **Daily Meal Vote Bot** loyihasi | Spring Boot + Telegram Bot API
