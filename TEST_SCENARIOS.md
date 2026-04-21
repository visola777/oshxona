# Test Scenarios & Usage Examples

## 🧪 Complete Test Scenarios

### Scenario 1: First-Time User Voting Journey

**Step 1: User Opens Bot**
```
User sends: /start

Bot Response:
🍽️ FOOD VOTING BOT
Welcome to School Meal Voting!

Vote for your favorite meals:
- 🥞 Breakfast
- 🍽️ Lunch  
- 🍪 Snack

Voting rules:
⏰ Until 11:00 AM
🔒 Vote in order (Breakfast → Lunch → Snack)
❌ Yesterday's winner is excluded
✅ One vote per category per day

[Breakfast] [Lunch] [Snack] [Statistics]
```

**Backend Processing:**
- `BotUserService.registerOrUpdateUser()` - User created in database
- `SessionStateManager.getUserState()` - Initial state: all locked
- User ID stored in `telegram_users` table

---

**Step 2: User Selects Breakfast (Always Unlocked)**
```
User taps: [Breakfast]

Bot Response:
🥞 SELECT BREAKFAST

1. Pancakes (15 votes)
2. Eggs & Toast (12 votes)
3. Oatmeal (8 votes)
4. Yogurt & Berries (5 votes)

[Back to Menu]
```

**Backend Processing:**
- `CallbackHandler` receives: `button_breakfast`
- `SessionStateManager.isCategoryLocked()` → false
- `DishRepository.findByCategory("breakfast")` - retrieves active dishes
- Dishes with `excluded = false` are shown

---

**Step 3: User Selects a Food Item**
```
User taps: Pancakes

Bot Response:
🥞 PANCAKES

Description: Fluffy pancakes with maple syrup
Nutritional Info:
- Calories: 320
- Protein: 8g
- Carbs: 45g

[Preview Image]

Is this your choice?

[✅ Confirm Vote] [❌ Back to Menu]
```

**Backend Processing:**
- `CallbackHandler` receives: `food_1`
- `FoodPreview` fetched with image URL
- Ready for confirmation

---

**Step 4: Vote Confirmed**
```
User taps: [✅ Confirm Vote]

Backend Validation:
1. VotingValidationService.isVotingTimeValid() → true (9:30 AM)
2. VotingValidationService.isCategoryLocked() → false
3. VotingValidationService.isDishExcluded() → false
4. VotingValidationService.hasUserVotedInCategory() → false

Bot Response:
✔️ VOTE ACCEPTED!

Your breakfast vote: Pancakes ✅

📊 Progress: 1/3 Complete
⭕ Breakfast ✅
⭕ Lunch
⭕ Snack

Next: Vote for Lunch 👇

[Breakfast] [Lunch] [Snack] [Statistics]
```

**Database Update:**
```sql
INSERT INTO votes (user_id, dish_id, category, vote_date, voted_at)
VALUES (123456789, 1, 'breakfast', '2026-04-21', NOW());

UPDATE user_sessions SET breakfast_voted = true WHERE user_id = 123456789;
```

---

**Step 5: User Tries Lunch (Now Unlocked)**
```
User taps: [Lunch]

Bot Response:
🍽️ SELECT LUNCH

1. Pizza (8 votes)
2. Pasta (12 votes)
3. Chicken Rice (10 votes)
4. Vegetable Soup (3 votes)

[Back to Menu]
```

**Backend Processing:**
- `SessionStateManager.isCategoryLocked("lunch")` checks breakfast_voted
- breakfast_voted = true → Lunch unlocked ✅
- `DishRepository.findByCategory("lunch")` and excluded = false

---

### Scenario 2: User Attempts Revote

**Situation: User Wants to Change Breakfast Vote**

```
After voting for Pancakes, user realizes they prefer Eggs & Toast

User taps: [Breakfast] again

Bot Response:
⚠️ You already voted for Pancakes

Do you want to change your vote?

1. Pancakes (your current vote)
2. Eggs & Toast
3. Oatmeal
4. Yogurt & Berries

[Change Vote] [Keep Current]
```

**Backend Processing:**
```java
// VotingValidationService.validateVoting()
Optional<Vote> existingVote = getUserVoteInCategory(userId, "breakfast");

if (existingVote.isPresent() && !existingVote.get().getDish().getId().equals(dish.getId())) {
    // Different dish → Revote allowed
    voteRepository.delete(existingVote.get());
    // Create new vote
    voteRepository.save(newVote);
}
```

**User selects: Eggs & Toast**
```
Bot Response:
🔄 VOTE CHANGED

Previous: Pancakes → New: Eggs & Toast ✅

Your vote has been updated!
```

**Database Update:**
```sql
-- Old vote deleted
DELETE FROM votes WHERE user_id = 123456789 AND category = 'breakfast';

-- New vote created
INSERT INTO votes (user_id, dish_id, category, vote_date, voted_at)
VALUES (123456789, 2, 'breakfast', '2026-04-21', NOW());
```

---

### Scenario 3: Deadline Violation

**Situation: User Tries to Vote After 11:00 AM**

```
Time: 11:05 AM
User opens bot and tries to vote

Bot Response:
❌ VOTING CLOSED

Voting deadline was 11:00 AM

Next voting round: Tomorrow at 00:00

Time until next voting: 12 hours 55 minutes

[View Yesterday's Statistics]
```

**Backend Processing:**
```java
public boolean isVotingTimeValid() {
    return LocalTime.now().isBefore(VOTING_DEADLINE); // 11:00
}

// Returns false
// VotingValidationService blocks the vote
```

---

### Scenario 4: Yesterday's Winner Excluded

**Situation: Pancakes Won Yesterday, Trying to Vote for It Today**

```
Yesterday's Results:
🥇 Pancakes (28 votes)

Today at 00:00 - Daily Reset Runs:
1. DailySchedulerService.performDailyReset()
2. Finds yesterday's top: Pancakes
3. Updates: pancakes.excluded = true
4. Logs: "🥇 Yesterday's winner excluded: Pancakes"

Today Morning - User Tries to Vote:
User selects Breakfast
Food list shows:
1. Eggs & Toast (0 votes) ✅
2. Oatmeal (0 votes) ✅
3. Yogurt & Berries (0 votes) ✅

❌ Pancakes is hidden/grayed out - Excluded!

If user somehow selects Pancakes:
Bot Response:
❌ This dish won yesterday and is excluded today
Choose another option
```

**Database State:**
```sql
UPDATE foods SET excluded = true WHERE id = 1 AND name = 'Pancakes';

-- Next day, re-enable:
UPDATE foods SET excluded = false WHERE id = 1;
```

---

### Scenario 5: Sequential Unlock (Lunch Locked)

**Situation: User Tries to Vote for Lunch Before Breakfast**

```
User selects: [Lunch] button (without voting for Breakfast)

Bot Response:
🔒 LUNCH LOCKED

You must vote for Breakfast first!

Complete the voting sequence:
1. 🥞 Breakfast (pending)
2. 🍽️ Lunch (locked)
3. 🍪 Snack (locked)

➡️ Start with: Breakfast

[Breakfast] [Lunch] [Snack]
```

**Backend Processing:**
```java
public boolean isCategoryLocked(Long userId, String category) {
    if ("lunch".equals(category)) {
        // Check if breakfast voted
        return !voteRepository.existsByUserIdAndVoteDateAndCategory(
            userId, LocalDate.now(), "breakfast"
        );
    }
}
```

---

### Scenario 6: Statistics View

**User Views Today's Voting Statistics**

```
User taps: [Statistics]

Bot Response:
📊 TODAY'S TOP DISHES

🥇 BREAKFAST
   Pancakes: 28 votes (35%)
   Eggs & Toast: 18 votes (22.5%)
   Oatmeal: 15 votes (18.75%)

🥇 LUNCH
   Pizza: 35 votes (40%)
   Pasta: 25 votes (28.5%)
   Chicken Rice: 20 votes (22.8%)

🥇 SNACK
   Cookies: 30 votes (38.5%)
   Apple: 25 votes (32%)
   Banana: 20 votes (25.6%)

Total Votes: 256
Total Voters: 85

[Back to Menu]
```

**Backend Processing:**
```java
// ApiStatisticsService.getDashboardData()
public DashboardDataDTO getDashboardData() {
    LocalDate today = LocalDate.now();
    List<Vote> todayVotes = voteRepository.findByVoteDate(today);
    
    // For each category
    for (String category : Arrays.asList("Breakfast", "Lunch", "Snack")) {
        StatisticsDTO stats = getStatsByCategory(category);
        // Calculate percentages
        // Find top dish
    }
    
    return new DashboardDataDTO(...);
}
```

---

### Scenario 7: Admin Panel Access

**Admin User Gets Access to Control Panel**

```
Admin User (ID in bot.admin-ids) sends: /admin

Bot Response:
🔑 ADMIN PANEL

SYSTEM STATISTICS
👥 Total Users: 156
🍱 Total Foods: 12
❌ Excluded Foods: 1
✅ Today's Votes: 256

ADMIN ACTIONS:
1. ➕ Add Food
2. ✏️ Edit Food
3. 🗑️ Delete Food
4. 📈 View Statistics
5. 👥 Manage Users
6. 🥇 View Top Food

[Add] [Edit] [Delete] [Stats] [Users] [Top]
```

---

**Admin Adds New Food**

```
Admin taps: [Add]

Bot: Send food name?
Admin: Biryani

Bot: Send category?
Admin: lunch

Bot: Send photo URL?
Admin: https://example.com/biryani.jpg

Bot: Send description?
Admin: Fragrant rice with meat, spices

Bot Response:
✅ Food added successfully!
- Name: Biryani
- Category: Lunch
- Photo: [URL]
- Description: [Description]

Available immediately for voting!

[Admin Menu] [Add Another]
```

**Backend Processing:**
```java
// AdminService.addFood()
@Transactional
public AdminResult addFood(String name, String category, 
                          String photoUrl, String description) {
    Dish newDish = new Dish();
    newDish.setName(name);
    newDish.setCategory(category);
    newDish.setPhotoUrl(photoUrl);
    newDish.setDescription(description);
    newDish.setActive(true);
    newDish.setExcluded(false);
    
    dishRepository.save(newDish);
    return new AdminResult(true, "✅ Food added: " + name);
}
```

**Database Update:**
```sql
INSERT INTO foods (name, category, photo_url, description, is_active, is_excluded)
VALUES ('Biryani', 'lunch', 'https://...', 'Fragrant rice...', true, false);
```

---

### Scenario 8: Website Dashboard Real-Time Update

**Parent Views Dashboard on Website**

**Initial Load:**
```
Website loads: dashboard-example.html
↓
JavaScript fetches: GET /api/stats/dashboard
↓
API returns:
{
  "date": "2026-04-21",
  "lastUpdated": "2026-04-21 10:45:30",
  "categories": [
    {
      "categoryName": "Breakfast 🥞",
      "topDish": {
        "name": "Pancakes",
        "votes": 28,
        "percentage": 35.0
      },
      "allDishes": [...]
    }
  ],
  "globalTopDishes": [...],
  "totalVotesToday": 256,
  "totalVotersToday": 85
}
↓
Dashboard renders charts and statistics
```

**Auto-Refresh Every 10 Seconds:**
```
10:45:40 → Fetches new data
10:45:50 → Pancakes now 29 votes (35.8%)
↓
Dashboard updates in real-time
Parent sees live voting progress!
```

---

## 🧪 Unit Test Examples

### Test: VotingValidationService

```java
@Test
public void testVotingAfterDeadline() {
    // Mock time to 11:05 AM
    LocalTime afterDeadline = LocalTime.of(11, 5);
    
    VotingValidationService service = new VotingValidationService(...);
    
    assertFalse(service.isVotingTimeValid());
}

@Test
public void testSequentialUnlock() {
    Long userId = 123456789L;
    
    // Initially all locked except breakfast
    assertTrue(service.isCategoryLocked(userId, "lunch"));
    assertTrue(service.isCategoryLocked(userId, "snack"));
    assertFalse(service.isCategoryLocked(userId, "breakfast"));
    
    // After breakfast vote, lunch unlocks
    // (vote recorded in database)
    
    assertFalse(service.isCategoryLocked(userId, "lunch"));
    assertTrue(service.isCategoryLocked(userId, "snack"));
}

@Test
public void testExcludedFood() {
    Dish dish = new Dish();
    dish.setName("Pancakes");
    dish.setExcluded(true);
    
    assertTrue(service.isDishExcluded(dish.getId()));
}
```

---

## 📋 Integration Test Scenario

```java
@SpringBootTest
public class VotingIntegrationTest {
    
    @Test
    public void testCompleteVotingJourney() {
        // 1. Register user
        TelegramUser user = botUserService.registerOrUpdateUser(123456789L, telegramUser);
        assertNotNull(user.getId());
        
        // 2. Get breakfast foods
        List<Dish> breakfastFoods = dishService.getFoodsByCategory("breakfast");
        assertFalse(breakfastFoods.isEmpty());
        
        // 3. Vote for breakfast
        Dish pancakes = breakfastFoods.get(0);
        VotingService.VoteResult result = votingService.voteForDish(user.getId(), pancakes);
        assertTrue(result.success);
        
        // 4. Check voting progress
        SessionStateManager.UserVotingState state = 
            sessionStateManager.getUserState(user.getId());
        assertTrue(state.breakfastVoted);
        assertFalse(state.lunchVoted);
        
        // 5. Try to vote for snack (should be locked)
        assertTrue(service.isCategoryLocked(user.getId(), "snack"));
        
        // 6. Vote for lunch
        List<Dish> lunchFoods = dishService.getFoodsByCategory("lunch");
        result = votingService.voteForDish(user.getId(), lunchFoods.get(0));
        assertTrue(result.success);
        
        // 7. Now snack should be unlocked
        assertFalse(service.isCategoryLocked(user.getId(), "snack"));
        
        // 8. Get statistics
        DashboardDataDTO dashboard = apiStatisticsService.getDashboardData();
        assertNotNull(dashboard.getGlobalTopDishes());
        assertEquals(2L, dashboard.getTotalVotesToday());
    }
}
```

---

## 🔍 Manual Testing Checklist

### Voting Functionality
- [ ] User can vote for breakfast (unlocked by default)
- [ ] Lunch remains locked until breakfast voted
- [ ] Snack remains locked until breakfast & lunch voted
- [ ] Voting blocked after 11:00 AM
- [ ] Revote works (changes previous vote)
- [ ] Same dish revote rejected
- [ ] Excluded dishes not shown

### Statistics
- [ ] Vote counts increase when user votes
- [ ] Percentages calculated correctly
- [ ] Top dishes displayed in order
- [ ] Statistics updated in real-time
- [ ] Website dashboard updates every 10 seconds

### Admin Panel
- [ ] Admin can add new food
- [ ] Admin can edit food details
- [ ] Admin can delete food
- [ ] Admin can view statistics
- [ ] Admin can block users
- [ ] System statistics display correctly

### Scheduler
- [ ] Daily reset at midnight
- [ ] Yesterday's winner excluded
- [ ] New voting round starts fresh
- [ ] Excluded dishes re-enabled after exclusion

### API Endpoints
- [ ] GET /api/health returns 200
- [ ] GET /api/stats/dashboard returns valid JSON
- [ ] GET /api/stats/top-dishes?limit=5 returns correct data
- [ ] GET /api/stats/category/lunch returns category stats
- [ ] GET /api/stats/dish/1 returns dish stats
- [ ] CORS headers present in responses

---

This covers all major scenarios for testing the complete system!
