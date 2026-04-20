package com.example.demo.Service;

import com.example.demo.entity.Dish;
import com.example.demo.entity.Vote;
import com.example.demo.entity.VoteCategory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class StatisticsService {
    private final VotingService votingService;
    private final DishService dishService;

    public StatisticsService(VotingService votingService, DishService dishService) {
        this.votingService = votingService;
        this.dishService = dishService;
    }

    public String renderGlobalTop(int limit, String languageCode) {
        List<Object[]> rows = votingService.getGlobalTopDishes();
        if (rows.isEmpty()) {
            return getEmptyStats(languageCode);
        }

        StringBuilder sb = new StringBuilder("🔥 Global Top Dishes:\n\n");
        for (int i = 0; i < Math.min(limit, rows.size()); i++) {
            Object[] row = rows.get(i);
            Dish dish = (Dish) row[0];
            long count = (Long) row[1];
            sb.append(formatRank(i + 1)).append(" ").append(dish.getName()).append(" — ").append(count).append(" votes\n");
        }
        return sb.toString();
    }

    public String renderDailyTop(String category, String languageCode) {
        List<Object[]> rows = votingService.getDailyTopDishes();
        if (rows.isEmpty()) {
            return getEmptyStats(languageCode);
        }

        StringBuilder sb = new StringBuilder("📅 Today's top dishes:\n\n");
        for (int i = 0; i < Math.min(10, rows.size()); i++) {
            Object[] row = rows.get(i);
            Dish dish = (Dish) row[0];
            long count = (Long) row[1];
            if (!dish.getCategory().equalsIgnoreCase(category)) {
                continue;
            }
            sb.append(formatRank(i + 1)).append(" ").append(dish.getName()).append(" — ").append(count).append(" votes\n");
        }
        if (sb.toString().equals("📅 Today's top dishes:\n\n")) {
            return getEmptyStats(languageCode);
        }
        return sb.toString();
    }

    public String renderPersonalHistory(Long telegramUserId, int daysBack, String languageCode) {
        List<Vote> history = votingService.getUserHistory(telegramUserId, daysBack);
        if (history.isEmpty()) {
            return languageCode != null && languageCode.startsWith("uz")
                    ? "Siz oxirgi bir necha kunda hech qanday ovoz bermagansiz."
                    : "You have not voted in the last few days.";
        }

        StringBuilder sb = new StringBuilder(languageCode != null && languageCode.startsWith("uz")
                ? "📌 Sizning oxirgi ovozlaringiz:\n\n"
                : "📌 Your recent votes:\n\n");

        history.stream().limit(20).forEach(vote -> sb.append(vote.getVoteDate()).append(" — ").append(vote.getDish().getName()).append(" (" + vote.getCategory() + ")\n"));
        return sb.toString();
    }

    public String renderDailySummary(String languageCode) {
        long votesToday = votingService.countTodayVotes();
        return languageCode != null && languageCode.startsWith("uz")
                ? "📊 Bugungi jami ovozlar: " + votesToday
                : "📊 Today's total votes: " + votesToday;
    }

    private String formatRank(int rank) {
        return switch (rank) {
            case 1 -> "🥇";
            case 2 -> "🥈";
            case 3 -> "🥉";
            default -> rank + ".";
        };
    }

    private String getEmptyStats(String languageCode) {
        return languageCode != null && languageCode.startsWith("uz")
                ? "Hali hech kim ovoz bermagan."
                : "No votes have been recorded yet.";
    }
}
