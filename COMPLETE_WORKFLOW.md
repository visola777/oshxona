# Complete System Workflow & Implementation Guide

## 📋 Overview
This document describes the complete workflow of the School Food Voting Telegram Bot system, showing how all components work together to implement the voting rules visualization and system architecture.

---

## 🎯 System Components Integration

### 1. USER FLOW - Telegram Mobile Interface

#### Step 1: User Opens Bot (/start)
```
User launches Telegram bot → Receives welcome message with menu
```

**Backend Flow:**
- `TelegramMealVoteBot.onUpdateReceived()` → receives `/start` command
- `BotMessages` generates welcome message
- `BotUserService.registerOrUpdateUser()` stores user in database
- User is assigned to `session` with initial state

#### Step 2: User Selects Category - Sequential Unlock

**Breakfast (Always Available)**
```
User selects "🥞 Breakfast" button
↓
SessionStateManager checks: breakfastVoted = false ✅
↓
FoodList displayed with all active Breakfast foods
```

**Lunch (Locked Until Breakfast Voted)**
```
User selects "🍽️ Lunch" button
↓
SessionStateManager checks: breakfastVoted = false ❌
↓
Message: "🔒 Vote for Breakfast first"
↓
Lunch button remains locked until Breakfast vote confirmed
```

**Snack (Locked Until Breakfast & Lunch Voted)**
```
User selects "🍪 Snack" button
↓
SessionStateManager checks: breakfastVoted && lunchVoted = false ❌
↓
Message: "🔒 Vote for Breakfast and Lunch first"
```

#### Step 3: User Selects Food Item

```
User taps food button
↓
CallbackHandler receives callback query
↓
VotingValidationService.validateVoting() checks:
   ✅ Time before 11:00?
   ✅ Category unlocked?
   ✅ Food not excluded?
   ✅ Different dish than previous vote?
↓
FoodPreview shown (image, name, description, nutrients)
↓
User confirms vote
```

#### Step 4: Vote Processing

```
User taps "✅ Confirm Vote" button
↓
VotingService.voteForDish() executes:
   1. Check if already voted in category
   2. If yes, delete previous vote
   3. Create new Vote record
   4. Update statistics
↓
Vote Accepted Message: "✔️ Vote recorded for [food]"
↓
SessionStateManager updates progress
↓
If all 3 categories voted: Show completion message
Else: Return to main menu, unlock next category
```

#### Step 5: User Views Statistics

```
User selects "📊 Statistics" button
↓
StatisticsService.renderDailyTop() fetches top dishes
↓
Format results with vote counts and percentages
↓
Display: Today's top dishes across all categories
```

---

## ⚖️ Voting Rules Implementation

### Rule 1: Time Deadline (11:00 AM)
```java
// File: VotingValidationService.java
public boolean isVotingTimeValid() {
    return LocalTime.now().isBefore(VOTING_DEADLINE); // 11:00
}

// Usage in validation
if (!isVotingTimeValid()) {
    return new VotingValidation(false, "❌ Voting closed! Deadline is 11:00 AM");
}
```

**Error Message after 11:00:**
```
"❌ Voting closed! Deadline is 11:00 AM"
Time until next voting: Midnight
```

### Rule 2: Revote Mechanism (Replace Previous Vote)
```java
// VotingService.voteForDish()
if (hasVotedToday(telegramUserId, category)) {
    Optional<Vote> existingVote = findTodayVote(telegramUserId, category);
    if (existingVote.get().getDish().getId().equals(dish.getId())) {
        return VoteResult.alreadyVotedSame(dish); // Same dish
    }
    // Different dish → delete old, create new
    voteRepository.delete(existingVote.get());
    return VoteResult.alreadyVotedDifferent(existingVote.orElse(null));
}
```

**User Message:**
```
"🔄 Your vote changed from [Old Food] to [New Food]"
```

### Rule 3: Yesterday's Winner Excluded Today
```java
// File: DailySchedulerService.java @ Midnight (00:00)

@Transactional
public void performDailyReset() {
    LocalDate yesterday = LocalDate.now().minusDays(1);
    Vote yesterdayTopVote = voteRepository.findGlobalTopDishByDate(yesterday);
    
    if (yesterdayTopVote != null) {
        Dish topDish = yesterdayTopVote.getDish();
        topDish.setExcluded(true);        // ❌ Exclude today
        dishRepository.save(topDish);
        
        log.info("🥇 Yesterday's winner excluded: {}", topDish.getName());
    }
}
```

**User Attempts Excluded Food:**
```
"❌ This dish won yesterday and is excluded today"
Food button is hidden or disabled
```

### Rule 4: One Vote Per Category Per Day
```java
// VotingValidationService.java
public boolean hasUserVotedInCategory(Long telegramUserId, String category) {
    return voteRepository.existsByUserIdAndVoteDateAndCategory(
        telegramUserId, 
        LocalDate.now(), 
        category
    );
}
```

### Rule 5: Sequential Unlock Logic
```java
// VotingValidationService.java
public boolean isCategoryLocked(Long telegramUserId, String category) {
    LocalDate today = LocalDate.now();
    
    return switch (category.toLowerCase()) {
        case "breakfast" -> false; // Always available
        case "lunch" -> 
            // Lunch locked until Breakfast voted
            !voteRepository.existsByUserIdAndVoteDateAndCategory(
                telegramUserId, today, "breakfast"
            );
        case "snack" -> 
            // Snack locked until Breakfast AND Lunch voted
            !voteRepository.existsByUserIdAndVoteDateAndCategory(
                telegramUserId, today, "breakfast"
            ) || !voteRepository.existsByUserIdAndVoteDateAndCategory(
                telegramUserId, today, "lunch"
            );
        default -> false;
    };
}
```

---

## 🤖 Telegram Bot Logic Layer

### Update Listener (Long Polling)
```
TelegramMealVoteBot extends TelegramLongPollingBot
↓
onUpdateReceived(Update update) continuously polls
↓
Receives: messages, callback queries, commands
↓
Routes to appropriate handler
```

### Message Handler
```java
if (message.getText().startsWith("/")) {
    switch (command) {
        case "/start" → startHandler();
        case "/stats" → statsHandler();
        case "/admin" → adminHandler();
    }
}
```

### Callback Handler
```java
if (update.hasCallbackQuery()) {
    String callbackData = update.getCallbackQuery().getData();
    // button_breakfast, button_lunch, button_snack, food_123, etc.
    
    if (callbackData.startsWith("food_")) {
        Long foodId = Long.parseLong(callbackData.substring(5));
        votingService.voteForDish(userId, foodId);
    }
}
```

### Session State Manager
```java
// Track which categories user has voted in
UserVotingState state = sessionStateManager.getUserState(userId);

// state.breakfastVoted = true
// state.lunchVoted = false
// state.snackVoted = false

// Get next available category
String nextCategory = sessionStateManager.getNextCategory(userId);
// Returns: "lunch"
```

### Validation Layer
```java
VotingValidation validation = votingValidationService.validateVoting(userId, dish);

if (!validation.valid) {
    sendMessage(userId, validation.message); // Error message
    return;
}
// Valid → process vote
```

---

## ⚙️ Backend (Spring Boot) - Service Layer

### 1. User Service
**File:** `BotUserService.java`
```java
public TelegramUser registerOrUpdateUser(Long telegramId, User user) {
    return userRepository.findByTelegramId(telegramId)
        .orElseGet(() -> {
            TelegramUser newUser = new TelegramUser();
            newUser.setTelegramId(telegramId);
            newUser.setUsername(user.getUserName());
            newUser.setFirstName(user.getFirstName());
            return userRepository.save(newUser);
        });
}
```

### 2. Voting Service
**File:** `VotingService.java`
```java
@Transactional
public VoteResult voteForDish(Long telegramUserId, Dish dish) {
    VoteCategory category = VoteCategory.fromName(dish.getCategory());
    
    // Check existing vote
    if (hasVotedToday(telegramUserId, category)) {
        // Handle revote
    }
    
    // Create new vote
    Vote vote = new Vote();
    vote.setUserId(telegramUserId);
    vote.setDish(dish);
    vote.setCategory(category.name());
    vote.setVoteDate(LocalDate.now());
    vote.setVotedAt(LocalDateTime.now());
    
    return voteRepository.save(vote);
}
```

### 3. Food Management Service
**File:** `MealDishService.java` or `AdminService.java`
```java
public AdminResult addFood(String name, String category, String photoUrl, String description) {
    Dish dish = new Dish();
    dish.setName(name);
    dish.setCategory(category);
    dish.setPhotoUrl(photoUrl);
    dish.setDescription(description);
    dish.setActive(true);
    return dishRepository.save(dish);
}
```

### 4. Statistics Engine
**File:** `StatisticsService.java` / `ApiStatisticsService.java`
```java
public List<DishVoteDTO> getTodayTopDishes(int limit) {
    LocalDate today = LocalDate.now();
    List<Vote> todayVotes = voteRepository.findByVoteDate(today);
    
    Map<Dish, Long> votesByDish = todayVotes.stream()
        .collect(Collectors.groupingBy(Vote::getDish, Collectors.counting()));
    
    // Convert to DTOs with percentages
    return votesByDish.entrySet().stream()
        .map(entry -> new DishVoteDTO(
            entry.getKey().getId(),
            entry.getKey().getName(),
            entry.getValue(),
            (entry.getValue() * 100.0) / todayVotes.size()
        ))
        .sorted(Comparator.comparingLong(DishVoteDTO::getVoteCount).reversed())
        .limit(limit)
        .collect(Collectors.toList());
}
```

### 5. Scheduler Service
**File:** `ScheduledTasks.java` / `DailySchedulerService.java`
```java
@Scheduled(cron = "0 0 0 * * *") // Midnight every day
public void performDailyReset() {
    // 1. Exclude yesterday's winner
    // 2. Generate daily report
    // 3. Reset for new day
}
```

### 6. Voting Validation Service
**File:** `VotingValidationService.java`
```java
public VotingValidation validateVoting(Long telegramUserId, Dish dish) {
    // Rule 1: Time check
    if (!isVotingTimeValid()) return VOTING_CLOSED;
    
    // Rule 2: Sequential unlock
    if (isCategoryLocked(telegramUserId, dish.getCategory())) 
        return CATEGORY_LOCKED;
    
    // Rule 3: Exclusion check
    if (isDishExcluded(dish.getId())) 
        return DISH_EXCLUDED;
    
    return VALID;
}
```

### 7. Session State Manager
**File:** `SessionStateManager.java`
```java
public UserVotingState getUserState(Long telegramUserId) {
    VotingProgress progress = votingValidationService.getUserVotingProgress(telegramUserId);
    return new UserVotingState(
        telegramUserId,
        progress.breakfastVoted,
        progress.lunchVoted,
        progress.snackVoted,
        progress.getProgressEmoji()
    );
}
```

### 8. Admin Service
**File:** `AdminService.java`
```java
public AdminResult excludeFood(Long dishId) {
    Dish dish = dishRepository.findById(dishId).orElse(null);
    if (dish != null) {
        dish.setExcluded(true);
        dishRepository.save(dish);
        return new AdminResult(true, "✅ Food excluded");
    }
    return new AdminResult(false, "❌ Dish not found");
}

public AdminResult blockUser(Long telegramUserId) {
    TelegramUser user = userRepository.findByTelegramId(telegramUserId).orElse(null);
    if (user != null) {
        user.setBlocked(true);
        userRepository.save(user);
        return new AdminResult(true, "✅ User blocked");
    }
    return new AdminResult(false, "❌ User not found");
}
```

---

## 🗄️ Database (PostgreSQL)

### Tables & Schema

#### Users Table
```sql
CREATE TABLE telegram_users (
    id BIGSERIAL PRIMARY KEY,
    telegram_id BIGINT UNIQUE NOT NULL,
    username VARCHAR(255),
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    language_code VARCHAR(10),
    is_admin BOOLEAN DEFAULT false,
    is_blocked BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### Foods Table
```sql
CREATE TABLE foods (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL,
    description TEXT,
    category VARCHAR(50),
    photo_url TEXT,
    is_active BOOLEAN DEFAULT true,
    is_excluded BOOLEAN DEFAULT false,
    total_votes INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### Dishes Table
```sql
CREATE TABLE dishes (
    id BIGSERIAL PRIMARY KEY,
    food_id BIGINT REFERENCES foods(id),
    category_id BIGINT REFERENCES vote_categories(id),
    vote_date DATE,
    created_at TIMESTAMP
);
```

#### Votes Table
```sql
CREATE TABLE votes (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES telegram_users(id),
    dish_id BIGINT REFERENCES dishes(id),
    food_name VARCHAR(255),
    category VARCHAR(50),
    vote_date DATE NOT NULL,
    voted_at TIMESTAMP,
    INDEX idx_vote_date (vote_date),
    INDEX idx_user_id (user_id),
    INDEX idx_category (category)
);
```

#### Vote Categories Table
```sql
CREATE TABLE vote_categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    created_at TIMESTAMP
);
-- Insert: breakfast, lunch, snack
```

#### Excluded Foods Table
```sql
CREATE TABLE excluded_foods (
    id BIGSERIAL PRIMARY KEY,
    food_id BIGINT REFERENCES foods(id),
    excluded_date DATE,
    reason VARCHAR(255),
    created_at TIMESTAMP
);
```

#### Admins Table
```sql
CREATE TABLE admins (
    id BIGSERIAL PRIMARY KEY,
    telegram_id BIGINT UNIQUE,
    permissions VARCHAR(255),
    created_at TIMESTAMP
);
```

#### User Sessions Table
```sql
CREATE TABLE user_sessions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES telegram_users(id),
    last_active TIMESTAMP,
    breakfast_voted BOOLEAN DEFAULT false,
    lunch_voted BOOLEAN DEFAULT false,
    snack_voted BOOLEAN DEFAULT false
);
```

---

## 🔌 REST API Layer

### Endpoints for Website Dashboard

```
GET /api/stats/dashboard
├─ Returns: DashboardDataDTO
├─ Contains: date, lastUpdated, categories[], globalTopDishes[], totalVotesToday, totalVotersToday
└─ Auto-refreshed every 10 seconds on dashboard

GET /api/stats/top-dishes?limit=5
├─ Returns: List<DishVoteDTO>
└─ Shows: top dishes with vote counts and percentages

GET /api/stats/category/{category}
├─ Returns: StatisticsDTO
└─ Shows: all dishes in category with breakdown

GET /api/stats/dish/{dishId}
├─ Returns: DishVoteDTO
└─ Shows: individual dish statistics
```

---

## 🌐 Public Website Dashboard

### Features
- Real-time vote counter
- Pie charts for vote distribution
- Top 3 dishes per category
- Live updates every 10 seconds
- Responsive mobile design
- Parent-friendly interface

### Data Flow
```
Website loads
↓
JavaScript fetches GET /api/stats/dashboard
↓
Dashboard renders data
↓
Auto-refresh every 10 seconds
↓
Users see live voting statistics
```

---

## 🔑 Admin Panel (Inside Telegram Bot)

### Admin Commands
```
/admin → Shows admin menu

Admin Actions:
1. ➕ Add Food
   /admin_add_food [name] [category] [photo_url]
   
2. ✏️ Edit Food
   /admin_edit_food [food_id] [new_name]
   
3. 🗑️ Delete Food
   /admin_delete_food [food_id]
   
4. 📈 View Statistics
   /admin_stats → Shows system statistics
   
5. 👥 Manage Users
   /admin_block_user [user_id]
   /admin_unblock_user [user_id]
   
6. 🥇 View Top Food
   /admin_top_food → Shows today's leader
```

---

## 📊 Data Flow Diagram

```
USERS (Parents)
    ↓
    └─→ TELEGRAM BOT (TelegramMealVoteBot)
            ↓
            ├─→ Update Listener (receives updates)
            ├─→ Message Handler (commands)
            ├─→ Callback Handler (buttons)
            └─→ Session State Manager (tracks progress)
                    ↓
                    └─→ Validation Layer
                            ↓
        ┌───────────────────┼───────────────────┐
        ↓                   ↓                   ↓
    BACKEND SERVICES:
    ├─ User Service
    ├─ Voting Service
    ├─ Food Management
    ├─ Statistics Engine
    ├─ Scheduler
    └─ Admin Control
        ↓
        └─→ DATABASE (PostgreSQL)
                ├─ Users
                ├─ Foods
                ├─ Categories
                ├─ Votes
                ├─ Excluded Foods
                └─ Sessions
                    ↓
                    └─→ API LAYER
                            ↓
                            └─→ WEBSITE DASHBOARD
                                    ├─ Today's Top Food
                                    ├─ Vote Percentages
                                    └─ Live Statistics
```

---

## 🚀 Execution Flow Example

### Scenario: User Votes for Lunch After Breakfast

1. **User selects Lunch button**
   - CallbackHandler receives: `button_lunch`
   - SessionStateManager.isCategoryLocked("lunch") checks
   - VotingValidationService checks: breakfastVoted = true ✅
   - Lunch unlocks, shows food list

2. **User selects Food (e.g., "Pizza")**
   - CallbackHandler receives: `food_5`
   - FoodPreview displayed with image and description
   - User taps "Confirm Vote"

3. **Vote Processing**
   - VotingValidationService.validateVoting() runs all checks ✅
   - VotingService.voteForDish() creates Vote record
   - Vote saved to database
   - Message: "✔️ Vote accepted for Pizza!"
   - SessionStateManager updates: lunchVoted = true

4. **Next Step**
   - SessionStateManager shows: "2/3 Complete"
   - Suggests: "Next: Vote for Snack"
   - Snack button now unlocked

5. **Statistics Updated**
   - ApiStatisticsService calculates new percentages
   - Website dashboard fetches new data
   - Dashboard updates in real-time

---

## ⏲️ Daily Scheduler (Midnight)

```
Midnight (00:00) Trigger
↓
DailySchedulerService.performDailyReset()
├─ Find yesterday's top dish
├─ Set excluded = true
├─ Generate daily report
├─ Log statistics
└─ New voting round ready
↓
Next day voting starts fresh
```

---

## 🎯 Complete Component Checklist

✅ User Telegram Interface (Mobile)
✅ Telegram Bot Logic (Long Polling)
✅ Command Handler (messages)
✅ Callback Handler (buttons)
✅ Session State Manager (voting progress)
✅ Voting Validation Service (all rules)
✅ User Service (registration)
✅ Voting Service (vote processing)
✅ Food Management (CRUD)
✅ Statistics Engine (calculations)
✅ Scheduler Service (daily reset)
✅ Admin Service (management)
✅ REST API Layer (endpoints)
✅ Database (PostgreSQL)
✅ Website Dashboard (frontend)
✅ CORS Configuration (cross-origin)
✅ Error Handling (validation)

---

## 📁 File Locations

```
src/main/java/com/example/demo/
├── bot/
│   ├── TelegramMealVoteBot.java       [Long Polling + Handlers]
│   ├── BotMessages.java                [Message formatting]
│   └── ScheduledTasks.java             [Task scheduling]
│
├── config/
│   ├── BotConfig.java                  [Bot configuration]
│   ├── CorsConfig.java                 [CORS setup]
│   └── [other configs]
│
├── controller/
│   ├── StatisticsController.java       [API endpoints]
│   └── ApiController.java              [General API]
│
├── dto/
│   ├── DashboardDataDTO.java
│   ├── StatisticsDTO.java
│   └── DishVoteDTO.java
│
├── entity/
│   ├── Dish.java                       [Food entity]
│   ├── Vote.java                       [Vote entity]
│   ├── TelegramUser.java               [User entity]
│   └── [other entities]
│
├── repository/
│   ├── VoteRepository.java             [Vote queries]
│   ├── DishRepository.java             [Food queries]
│   └── [other repos]
│
└── Service/
    ├── VotingService.java              [Vote processing]
    ├── BotUserService.java             [User management]
    ├── MealDishService.java            [Food management]
    ├── StatisticsService.java          [Statistics]
    ├── VotingValidationService.java    [Rule validation]
    ├── SessionStateManager.java        [Session tracking]
    ├── DailySchedulerService.java      [Daily tasks]
    ├── ApiStatisticsService.java       [API stats]
    ├── AdminService.java               [Admin operations]
    └── [other services]
```

---

This completes the entire workflow documentation for the School Food Voting Telegram Bot system!
