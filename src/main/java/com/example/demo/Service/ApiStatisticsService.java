package com.example.demo.Service;

import com.example.demo.dto.DashboardDataDTO;
import com.example.demo.dto.DishVoteDTO;
import com.example.demo.dto.StatisticsDTO;
import com.example.demo.entity.Dish;
import com.example.demo.entity.Vote;
import com.example.demo.repository.DishRepository;
import com.example.demo.repository.VoteRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for generating API statistics and dashboard data
 */
@Service
public class ApiStatisticsService {
    private final VoteRepository voteRepository;
    private final DishRepository dishRepository;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ApiStatisticsService(VoteRepository voteRepository, DishRepository dishRepository) {
        this.voteRepository = voteRepository;
        this.dishRepository = dishRepository;
    }

    /**
     * Get comprehensive statistics for a specific category
     */
    public StatisticsDTO getStatsByCategory(String category) {
        LocalDate today = LocalDate.now();

        // Get all votes for this category today
        List<Vote> todayVotes = voteRepository.findByVoteDateAndCategory(today, category);

        // Get all active dishes for this category
        List<Dish> dishes = dishRepository.findByCategory(category);

        // Count total voters (unique users)
        Long totalVoters = todayVotes.stream()
                .map(Vote::getUserId)
                .distinct()
                .count();

        // Group votes by dish
        Map<Dish, Long> votesByDish = todayVotes.stream()
                .collect(Collectors.groupingBy(Vote::getDish, Collectors.counting()));

        long totalVotes = todayVotes.size();

        // Convert to DTOs with percentages
        List<DishVoteDTO> dishDTOs = dishes.stream()
                .map(dish -> {
                    Long votes = votesByDish.getOrDefault(dish, 0L);
                    Double percentage = totalVotes > 0 ? (votes * 100.0) / totalVotes : 0.0;
                    return new DishVoteDTO(
                            dish.getId(),
                            dish.getName(),
                            dish.getDescription(),
                            dish.getCategory(),
                            dish.getPhotoUrl(),
                            votes,
                            Math.round(percentage * 100.0) / 100.0);
                })
                .sorted((a, b) -> Long.compare(b.getVoteCount(), a.getVoteCount()))
                .collect(Collectors.toList());

        // Get top dish
        DishVoteDTO topDish = dishDTOs.isEmpty() ? null : dishDTOs.get(0);

        return new StatisticsDTO(
                today,
                category,
                formatCategoryName(category),
                totalVotes,
                totalVoters,
                topDish,
                dishDTOs);
    }

    /**
     * Get today's top dishes across all categories
     */
    public List<DishVoteDTO> getTodayTopDishes(int limit) {
        LocalDate today = LocalDate.now();
        List<Vote> todayVotes = voteRepository.findByVoteDate(today);

        if (todayVotes.isEmpty()) {
            return new ArrayList<>();
        }

        Map<Dish, Long> votesByDish = todayVotes.stream()
                .collect(Collectors.groupingBy(Vote::getDish, Collectors.counting()));

        long totalVotes = todayVotes.size();

        return votesByDish.entrySet().stream()
                .map(entry -> {
                    Dish dish = entry.getKey();
                    Long votes = entry.getValue();
                    Double percentage = (votes * 100.0) / totalVotes;
                    return new DishVoteDTO(
                            dish.getId(),
                            dish.getName(),
                            dish.getDescription(),
                            dish.getCategory(),
                            dish.getPhotoUrl(),
                            votes,
                            Math.round(percentage * 100.0) / 100.0);
                })
                .sorted((a, b) -> Long.compare(b.getVoteCount(), a.getVoteCount()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Get dashboard data with all statistics
     */
    public DashboardDataDTO getDashboardData() {
        LocalDate today = LocalDate.now();
        List<Vote> todayVotes = voteRepository.findByVoteDate(today);

        // Get unique categories
        List<String> categories = Arrays.asList("Breakfast", "Lunch", "Snack");

        // Generate stats for each category
        List<StatisticsDTO> categoryStats = categories.stream()
                .map(this::getStatsByCategory)
                .collect(Collectors.toList());

        // Get global top dishes
        List<DishVoteDTO> globalTop = getTodayTopDishes(5);

        // Count total voters (unique users today)
        Long totalVoters = todayVotes.stream()
                .map(Vote::getUserId)
                .distinct()
                .count();

        return new DashboardDataDTO(
                today,
                LocalDateTime.now().format(formatter),
                categoryStats,
                globalTop,
                (long) todayVotes.size(),
                totalVoters);
    }

    /**
     * Get statistics for a specific dish
     */
    public DishVoteDTO getDishStatistics(Long dishId) {
        Optional<Dish> dishOpt = dishRepository.findById(dishId);
        if (dishOpt.isEmpty()) {
            return null;
        }

        Dish dish = dishOpt.get();
        LocalDate today = LocalDate.now();

        // Count votes for this dish today
        Long voteCount = voteRepository.countByDishIdAndVoteDate(dishId, today);

        // Count total votes for the category today
        List<Vote> categoryVotes = voteRepository.findByVoteDateAndCategory(today, dish.getCategory());
        long totalVotes = categoryVotes.size();

        Double percentage = totalVotes > 0 ? (voteCount * 100.0) / totalVotes : 0.0;

        return new DishVoteDTO(
                dish.getId(),
                dish.getName(),
                dish.getDescription(),
                dish.getCategory(),
                dish.getPhotoUrl(),
                voteCount,
                Math.round(percentage * 100.0) / 100.0);
    }

    /**
     * Format category name for display
     */
    private String formatCategoryName(String category) {
        return switch (category) {
            case "breakfast" -> "Breakfast 🥞";
            case "lunch" -> "Lunch 🍽️";
            case "snack" -> "Snack 🍪";
            default -> category;
        };
    }
}
