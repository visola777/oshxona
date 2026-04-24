//package com.example.demo.Service;
//
//import com.example.demo.entity.Dish;
//import com.example.demo.entity.Vote;
//import com.example.demo.repository.DishRepository;
//import com.example.demo.repository.VoteRepository;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDate;
//
///**
// * Service for daily scheduler tasks
// * - Daily reset of votes (new day, new voting round)
// * - Exclude yesterday's winning dish
// * - Generate statistics for previous day
// */
//@Service
//public class DailySchedulerService {
//    private static final Logger log = LoggerFactory.getLogger(DailySchedulerService.class);
//    private final VoteRepository voteRepository;
//    private final DishRepository dishRepository;
//    private final ApiStatisticsService statisticsService;
//
//    public DailySchedulerService(VoteRepository voteRepository,
//            DishRepository dishRepository,
//            ApiStatisticsService statisticsService) {
//        this.voteRepository = voteRepository;
//        this.dishRepository = dishRepository;
//        this.statisticsService = statisticsService;
//    }
//
//    /**
//     * Daily task executed at midnight (00:00)
//     * 1. Exclude yesterday's top dish
//     * 2. Generate final statistics for the day
//     * 3. Log daily report
//     */
//    @Transactional
//    public void performDailyReset() {
//        try {
//            log.info("🌙 Starting daily scheduler task at midnight...");
//
//            LocalDate yesterday = LocalDate.now().minusDays(1);
//
//            // Step 1: Find yesterday's top dish
//            Vote yesterdayTopVote = voteRepository.findGlobalTopDishByDate(yesterday);
//
//            if (yesterdayTopVote != null) {
//                Dish topDish = yesterdayTopVote.getDish();
//                log.info("🥇 Yesterday's winner: {} ({})", topDish.getName(), topDish.getCategory());
//
//                // Exclude today (set excluded flag or mark in exclusion table)
//                topDish.setExcluded(true);
//                dishRepository.save(topDish);
//                log.info("❌ Excluded today: {}", topDish.getName());
//
//                // Generate report for yesterday
//                generateDailyReport(yesterday);
//            } else {
//                log.info("ℹ️ No votes recorded yesterday");
//            }
//
//            log.info("✅ Daily scheduler task completed successfully");
//
//        } catch (Exception e) {
//            log.error("❌ Error in daily scheduler task: {}", e.getMessage(), e);
//        }
//    }
//
//    /**
//     * Generate daily voting report
//     */
//    private void generateDailyReport(LocalDate date) {
//        log.info("\n========== DAILY VOTING REPORT: {} ==========", date);
//
//        long totalVotes = voteRepository.countByVoteDate(date);
//        long totalVoters = voteRepository.countUniqueVotersByDate(date);
//
//        log.info("📊 Total Votes: {}", totalVotes);
//        log.info("👥 Total Voters: {}", totalVoters);
//        log.info("📈 Average votes per voter: {}",
//                totalVotes > 0 ? String.format("%.2f", (double) totalVotes / totalVoters) : 0);
//
//        // Category reports
//        logCategoryReport(date, "breakfast");
//        logCategoryReport(date, "lunch");
//        logCategoryReport(date, "snack");
//
//        log.info("==========================================\n");
//    }
//
//    /**
//     * Log statistics for a specific category
//     */
//    private void logCategoryReport(LocalDate date, String category) {
//        long categoryVotes = voteRepository.countByVoteDateAndCategory(date, category);
//        log.info("  {} {} - {} votes", getCategoryEmoji(category), category.toUpperCase(), categoryVotes);
//    }
//
//    /**
//     * Re-enable previously excluded dishes at new day
//     */
//    @Transactional
//    public void reenableExcludedDishes() {
//        try {
//            log.info("♻️ Re-enabling excluded dishes for new day...");
//
//            Iterable<Dish> excludedDishes = dishRepository.findAll();
//            int count = 0;
//
//            for (Dish dish : excludedDishes) {
//                if (dish.isExcluded()) {
//                    dish.setExcluded(false);
//                    dishRepository.save(dish);
//                    count++;
//                }
//            }
//
//            log.info("✅ Re-enabled {} dishes", count);
//        } catch (Exception e) {
//            log.error("❌ Error re-enabling dishes: {}", e.getMessage(), e);
//        }
//    }
//
//    private String getCategoryEmoji(String category) {
//        return switch (category.toLowerCase()) {
//            case "breakfast" -> "🥞";
//            case "lunch" -> "🍽️";
//            case "snack" -> "🍪";
//            default -> "🍱";
//        };
//    }
//
//    /**
//     * Get additional statistics from the repository
//     */
//    private long countByVoteDateAndCategory(LocalDate date, String category) {
//        return voteRepository.findByVoteDateAndCategory(date, category).size();
//    }
//}
package com.example.demo.Service;

import com.example.demo.bot.TelegramMealVoteBot;
import com.example.demo.repository.VoteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class DailySchedulerService {

    private static final Logger log = LoggerFactory.getLogger(DailySchedulerService.class);
    private final VoteRepository voteRepository;
    private final TelegramMealVoteBot bot;

    public DailySchedulerService(VoteRepository voteRepository, TelegramMealVoteBot bot) {
        this.voteRepository = voteRepository;
        this.bot = bot;
    }

    // Send reminder at 10:30 every day
    @Scheduled(cron = "0 30 10 * * ?")
    public void sendReminder() {
        log.info("Sending daily reminder");
        bot.sendDailyReminder();
    }

    // Optional: log statistics at 11:05
    @Scheduled(cron = "0 5 12 * * ?")
    public void logMorningStats() {
        LocalDate today = LocalDate.now();
        long total = voteRepository.countByVoteDate(today);
        log.info("Votes before deadline: total={}", total);
    }
}

