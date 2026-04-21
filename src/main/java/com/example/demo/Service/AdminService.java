package com.example.demo.Service;

import com.example.demo.entity.Dish;
import com.example.demo.entity.TelegramUser;
import com.example.demo.entity.Vote;
import com.example.demo.repository.DishRepository;
import com.example.demo.repository.TelegramUserRepository;
import com.example.demo.repository.VoteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AdminService {
    private static final Logger log = LoggerFactory.getLogger(AdminService.class);
    private final BotUserService userService;
    private final VoteRepository voteRepository;
    private final DishRepository dishRepository;
    private final TelegramUserRepository userRepository;

    public AdminService(BotUserService userService, 
                       VoteRepository voteRepository,
                       DishRepository dishRepository,
                       TelegramUserRepository userRepository) {
        this.userService = userService;
        this.voteRepository = voteRepository;
        this.dishRepository = dishRepository;
        this.userRepository = userRepository;
    }

    public long countVotesToday() {
        return voteRepository.findAllByVoteDate(LocalDate.now()).size();
    }

    public List<TelegramUser> allUsers() {
        return userService.getAllUsers();
    }

    public byte[] exportVotesCsv() {
        List<Vote> votes = voteRepository.findAll();
        String header = "userId,username,dish,category,voteDate\n";
        String body = votes.stream()
                .map(v -> String.format("%d,%s,%s,%s,%s",
                        v.getUserId(),
                        safe(v.getDish().getName()),
                        safe(v.getDish().getCategory()),
                        safe(v.getCategory()),
                        v.getVoteDate()))
                .collect(Collectors.joining("\n"));
        return (header + body).getBytes(StandardCharsets.UTF_8);
    }

    public void resetTodayVotes() {
        List<Vote> currentVotes = voteRepository.findAllByVoteDate(LocalDate.now());
        currentVotes.forEach(vote -> {
            Dish dish = vote.getDish();
            dish.setTotalVotes(Math.max(0, dish.getTotalVotes() - 1));
        });
        voteRepository.deleteAll(currentVotes);
    }

    private String safe(String value) {
        return value == null ? "" : value.replace(",", " ").replace("\n", " ");
    }

    // ===================== FOOD MANAGEMENT =====================

    /**
     * Add a new food dish
     */
    @Transactional
    public AdminResult addFood(String name, String category, String photoUrl, String description) {
        try {
            List<Dish> existing = dishRepository.findByName(name);
            if (!existing.isEmpty()) {
                return new AdminResult(false, "❌ Dish already exists: " + name);
            }

            Dish newDish = new Dish();
            newDish.setName(name);
            newDish.setCategory(category);
            newDish.setPhotoUrl(photoUrl);
            newDish.setDescription(description);
            newDish.setActive(true);
            newDish.setExcluded(false);
            newDish.setCreatedAt(LocalDateTime.now());

            dishRepository.save(newDish);
            log.info("✅ Food added: {} ({})", name, category);
            return new AdminResult(true, "✅ Food added: " + name);
        } catch (Exception e) {
            log.error("❌ Error adding food: {}", e.getMessage());
            return new AdminResult(false, "❌ Error: " + e.getMessage());
        }
    }

    /**
     * Edit an existing food
     */
    @Transactional
    public AdminResult editFood(Long dishId, String name, String description, String photoUrl) {
        try {
            Optional<Dish> dishOpt = dishRepository.findById(dishId);
            if (dishOpt.isEmpty()) {
                return new AdminResult(false, "❌ Dish not found");
            }

            Dish dish = dishOpt.get();
            if (name != null && !name.isEmpty()) dish.setName(name);
            if (description != null && !description.isEmpty()) dish.setDescription(description);
            if (photoUrl != null && !photoUrl.isEmpty()) dish.setPhotoUrl(photoUrl);

            dishRepository.save(dish);
            log.info("✏️ Food updated: {}", dish.getName());
            return new AdminResult(true, "✅ Food updated: " + dish.getName());
        } catch (Exception e) {
            log.error("❌ Error editing food: {}", e.getMessage());
            return new AdminResult(false, "❌ Error: " + e.getMessage());
        }
    }

    /**
     * Delete a food
     */
    @Transactional
    public AdminResult deleteFood(Long dishId) {
        try {
            Optional<Dish> dishOpt = dishRepository.findById(dishId);
            if (dishOpt.isEmpty()) {
                return new AdminResult(false, "❌ Dish not found");
            }

            Dish dish = dishOpt.get();
            String name = dish.getName();
            dishRepository.delete(dish);
            log.info("🗑️ Food deleted: {}", name);
            return new AdminResult(true, "✅ Food deleted: " + name);
        } catch (Exception e) {
            log.error("❌ Error deleting food: {}", e.getMessage());
            return new AdminResult(false, "❌ Error: " + e.getMessage());
        }
    }

    /**
     * Get all foods in a category
     */
    public List<Dish> getFoodsByCategory(String category) {
        return dishRepository.findByCategory(category);
    }

    /**
     * Get all excluded foods
     */
    public List<Dish> getExcludedFoods() {
        return dishRepository.findAll().stream()
                .filter(Dish::isExcluded)
                .toList();
    }

    /**
     * Exclude a food (e.g., yesterday's winner)
     */
    @Transactional
    public AdminResult excludeFood(Long dishId) {
        try {
            Optional<Dish> dishOpt = dishRepository.findById(dishId);
            if (dishOpt.isEmpty()) {
                return new AdminResult(false, "❌ Dish not found");
            }

            Dish dish = dishOpt.get();
            dish.setExcluded(true);
            dishRepository.save(dish);
            log.info("❌ Food excluded: {}", dish.getName());
            return new AdminResult(true, "✅ Food excluded: " + dish.getName());
        } catch (Exception e) {
            log.error("❌ Error excluding food: {}", e.getMessage());
            return new AdminResult(false, "❌ Error: " + e.getMessage());
        }
    }

    /**
     * Remove exclusion from a food
     */
    @Transactional
    public AdminResult removeExclusion(Long dishId) {
        try {
            Optional<Dish> dishOpt = dishRepository.findById(dishId);
            if (dishOpt.isEmpty()) {
                return new AdminResult(false, "❌ Dish not found");
            }

            Dish dish = dishOpt.get();
            dish.setExcluded(false);
            dishRepository.save(dish);
            log.info("♻️ Exclusion removed: {}", dish.getName());
            return new AdminResult(true, "✅ Exclusion removed");
        } catch (Exception e) {
            log.error("❌ Error removing exclusion: {}", e.getMessage());
            return new AdminResult(false, "❌ Error: " + e.getMessage());
        }
    }

    // ===================== USER MANAGEMENT =====================

    /**
     * Block a user
     */
    @Transactional
    public AdminResult blockUser(Long telegramUserId) {
        try {
            Optional<TelegramUser> userOpt = userRepository.findByTelegramId(telegramUserId);
            if (userOpt.isEmpty()) {
                return new AdminResult(false, "❌ User not found");
            }

            TelegramUser user = userOpt.get();
            user.setBlocked(true);
            userRepository.save(user);
            log.info("🚫 User blocked: {}", user.getUsername());
            return new AdminResult(true, "✅ User blocked");
        } catch (Exception e) {
            log.error("❌ Error blocking user: {}", e.getMessage());
            return new AdminResult(false, "❌ Error: " + e.getMessage());
        }
    }

    /**
     * Unblock a user
     */
    @Transactional
    public AdminResult unblockUser(Long telegramUserId) {
        try {
            Optional<TelegramUser> userOpt = userRepository.findByTelegramId(telegramUserId);
            if (userOpt.isEmpty()) {
                return new AdminResult(false, "❌ User not found");
            }

            TelegramUser user = userOpt.get();
            user.setBlocked(false);
            userRepository.save(user);
            log.info("✅ User unblocked: {}", user.getUsername());
            return new AdminResult(true, "✅ User unblocked");
        } catch (Exception e) {
            log.error("❌ Error unblocking user: {}", e.getMessage());
            return new AdminResult(false, "❌ Error: " + e.getMessage());
        }
    }

    // ===================== SYSTEM STATISTICS =====================

    /**
     * Get system statistics for admin dashboard
     */
    public AdminStatistics getSystemStatistics() {
        return new AdminStatistics(
            userRepository.count(),
            dishRepository.count(),
            getExcludedFoods().size(),
            voteRepository.findAllByVoteDate(LocalDate.now()).size(),
            LocalDateTime.now()
        );
    }

    /**
     * Get formatted system report
     */
    public String getSystemReport() {
        AdminStatistics stats = getSystemStatistics();
        return String.format(
            "📊 SYSTEM STATISTICS\n\n" +
            "👥 Total Users: %d\n" +
            "🍱 Total Foods: %d\n" +
            "❌ Excluded Foods: %d\n" +
            "✅ Today's Votes: %d\n" +
            "⏰ Generated: %s",
            stats.totalUsers, stats.totalFoods, stats.excludedFoods, 
            stats.votesToday, stats.generatedAt.toString()
        );
    }

    // ===================== ADMIN RESULT & STATISTICS CLASSES =====================

    public static class AdminResult {
        public final boolean success;
        public final String message;

        public AdminResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }

    public static class AdminStatistics {
        public final long totalUsers;
        public final long totalFoods;
        public final long excludedFoods;
        public final long votesToday;
        public final LocalDateTime generatedAt;

        public AdminStatistics(long totalUsers, long totalFoods, long excludedFoods, 
                              long votesToday, LocalDateTime generatedAt) {
            this.totalUsers = totalUsers;
            this.totalFoods = totalFoods;
            this.excludedFoods = excludedFoods;
            this.votesToday = votesToday;
            this.generatedAt = generatedAt;
        }
    }
