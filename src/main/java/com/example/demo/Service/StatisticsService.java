package com.example.demo.Service;

import com.example.demo.entity.Dish;
import com.example.demo.entity.Vote;
import com.example.demo.entity.VoteCategory;
import com.example.demo.repository.DishRepository;
import com.example.demo.repository.TelegramUserRepository;
import com.example.demo.repository.VoteRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StatisticsService {

    private final VoteRepository voteRepository;
    private final DishRepository dishRepository;
    private final TelegramUserRepository userRepository;
    private final VotingService votingService;  // Added

    public StatisticsService(VoteRepository voteRepository,
                             DishRepository dishRepository,
                             TelegramUserRepository userRepository,
                             VotingService votingService) {
        this.voteRepository = voteRepository;
        this.dishRepository = dishRepository;
        this.userRepository = userRepository;
        this.votingService = votingService;
    }

    // ========== New method for percentage-based daily top ==========
    public String renderDailyTopWithPercentage(String languageCode) {
        LocalDate today = LocalDate.now();
        StringBuilder sb = new StringBuilder("🏆 *Bugungi eng ko'p ovoz olgan taomlar*\n\n");

        for (VoteCategory category : VoteCategory.values()) {
            List<Dish> dishes = dishRepository.findActiveByCategory(category.name());
            if (dishes.isEmpty()) continue;

            long totalVotes = voteRepository.countByCategoryAndDate(category.name(), today);
            if (totalVotes == 0) {
                sb.append("🍽️ *").append(category.label(languageCode)).append("*\n");
                sb.append("   Hali hech qanday ovoz yo'q.\n\n");
                continue;
            }

            Map<Long, Integer> voteCounts = voteRepository.findVoteCountsByCategoryAndDate(category.name(), today)
                    .stream()
                    .map(obj -> (Object[]) obj)
                    .collect(Collectors.toMap(
                            row -> (Long) row[0],
                            row -> ((Number) row[1]).intValue()
                    ));

            List<Dish> sortedDishes = dishes.stream()
                    .sorted((a, b) -> Integer.compare(
                            voteCounts.getOrDefault(b.getId(), 0),
                            voteCounts.getOrDefault(a.getId(), 0)))
                    .collect(Collectors.toList());

            sb.append("🍽️ *").append(category.label(languageCode)).append("*\n");
            for (Dish dish : sortedDishes) {
                int votes = voteCounts.getOrDefault(dish.getId(), 0);
                int percent = totalVotes == 0 ? 0 : (int)(votes * 100 / totalVotes);
                sb.append("• ").append(dish.getName())
                        .append(" – ").append(percent).append("% (")
                        .append(votes).append(" ovoz)\n");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    // ========== Fixed methods using votingService ==========
    public String renderGlobalTop(int limit, String languageCode) {
        List<Object[]> rows = votingService.getGlobalTopDishes();
        if (rows.isEmpty()) {
            return getEmptyStats(languageCode);
        }

        StringBuilder sb = new StringBuilder("🔥 *Global Top Dishes:*\n\n");
        for (int i = 0; i < Math.min(limit, rows.size()); i++) {
            Object[] row = rows.get(i);
            Dish dish = (Dish) row[0];
            long count = (Long) row[1];
            sb.append(formatRank(i + 1)).append(" ").append(dish.getName()).append(" — ").append(count)
                    .append(" ta ovoz\n");        }
        return sb.toString();
    }

    public String renderDailyTop(String category, String languageCode) {
        List<Object[]> rows = votingService.getDailyTopDishes();
        if (rows.isEmpty()) {
            return getEmptyStats(languageCode);
        }

        StringBuilder sb = new StringBuilder("📅 *Today's top dishes:*\n\n");
        int displayed = 0;
        for (Object[] row : rows) {
            Dish dish = (Dish) row[0];
            long count = (Long) row[1];
            if (dish.getCategory().equalsIgnoreCase(category)) {
                sb.append(formatRank(displayed + 1)).append(" ").append(dish.getName()).append(" — ").append(count)
                        .append(" votes\n");
                displayed++;
                if (displayed >= 10) break;
            }
        }
        if (displayed == 0) {
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
                ? "📌 *Sizning oxirgi ovozlaringiz:*\n\n"
                : "📌 *Your recent votes:*\n\n");

        history.stream().limit(20).forEach(vote -> sb.append(vote.getVoteDate()).append(" — ")
                .append(vote.getDish().getName()).append(" (").append(vote.getCategory()).append(")\n"));
        return sb.toString();
    }

    public String renderDailySummary(String languageCode) {
        long votesToday = votingService.countTodayVotes();
        return languageCode != null && languageCode.startsWith("uz")
                ? "📊 *Bugungi jami ovozlar:* " + votesToday
                : "📊 *Today's total votes:* " + votesToday;
    }

    // ========== Helper methods ==========
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