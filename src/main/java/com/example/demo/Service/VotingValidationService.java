package com.example.demo.Service;

import com.example.demo.entity.Dish;
import com.example.demo.entity.TelegramUser;
import com.example.demo.entity.Vote;
import com.example.demo.entity.VoteCategory;
import com.example.demo.repository.DishRepository;
import com.example.demo.repository.TelegramUserRepository;
import com.example.demo.repository.VoteRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

/**
 * Service for validating voting rules and constraints
 * Implements the voting rules visualization:
 * - Deadline: 11:00 AM
 * - Revote: Replaces previous vote
 * - Exclusion: Yesterday's winner excluded
 * - One vote per category per day
 * - Sequential unlock logic
 */
@Service
public class VotingValidationService {
    private final VoteRepository voteRepository;
    private final DishRepository dishRepository;
    private final TelegramUserRepository userRepository;
    private static final LocalTime VOTING_DEADLINE = LocalTime.of(11, 0);

    public VotingValidationService(VoteRepository voteRepository,
            DishRepository dishRepository,
            TelegramUserRepository userRepository) {
        this.voteRepository = voteRepository;
        this.dishRepository = dishRepository;
        this.userRepository = userRepository;
    }

    /**
     * Check if voting is allowed at current time (before 11:00)
     */
    public boolean isVotingTimeValid() {
        return LocalTime.now().isBefore(VOTING_DEADLINE);
    }

    /**
     * Get time remaining until voting deadline
     */
    public String getTimeUntilDeadline() {
        LocalTime now = LocalTime.now();
        if (now.isAfter(VOTING_DEADLINE)) {
            return "❌ Voting closed for today";
        }
        LocalTime deadline = VOTING_DEADLINE;
        int minutes = (int) java.time.temporal.ChronoUnit.MINUTES.between(now, deadline);
        int hours = minutes / 60;
        minutes = minutes % 60;

        if (hours > 0) {
            return String.format("⏰ %d hours %d minutes remaining", hours, minutes);
        } else {
            return String.format("⏰ %d minutes remaining", minutes);
        }
    }

    /**
     * Check if user has already voted in this category today
     */
    public boolean hasUserVotedInCategory(Long telegramUserId, String category) {
        return voteRepository.existsByUserIdAndVoteDateAndCategory(
                telegramUserId,
                LocalDate.now(),
                category);
    }

    /**
     * Get user's vote in a category today
     */
    public Optional<Vote> getUserVoteInCategory(Long telegramUserId, String category) {
        return voteRepository.findByUserIdAndVoteDateAndCategory(
                telegramUserId,
                LocalDate.now(),
                category);
    }

    /**
     * Check sequential voting progress
     * Returns which categories are unlocked for user
     */
    public VotingProgress getUserVotingProgress(Long telegramUserId) {
        LocalDate today = LocalDate.now();

        boolean breakfastVoted = voteRepository.existsByUserIdAndVoteDateAndCategory(
                telegramUserId, today, "breakfast");
        boolean lunchVoted = voteRepository.existsByUserIdAndVoteDateAndCategory(
                telegramUserId, today, "lunch");
        boolean snackVoted = voteRepository.existsByUserIdAndVoteDateAndCategory(
                telegramUserId, today, "snack");

        return new VotingProgress(breakfastVoted, lunchVoted, snackVoted);
    }

    /**
     * Check if a dish is excluded (yesterday's winner)
     */
    public boolean isDishExcluded(Long dishId) {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        return dishRepository.findById(dishId)
                .map(dish -> {
                    // Check if this dish won yesterday
                    Vote yesterdayWinner = voteRepository.findGlobalTopDishByDate(yesterday);
                    return yesterdayWinner != null && yesterdayWinner.getDish().getId().equals(dishId);
                })
                .orElse(false);
    }

    /**
     * Check if category is locked (requires prior category to be voted first)
     */
    public boolean isCategoryLocked(Long telegramUserId, String category) {
        LocalDate today = LocalDate.now();

        return switch (category.toLowerCase()) {
            case "breakfast" -> false; // Breakfast is always available
            case "lunch" ->
                // Lunch is locked until Breakfast is voted
                !voteRepository.existsByUserIdAndVoteDateAndCategory(
                        telegramUserId, today, "breakfast");
            case "snack" ->
                // Snack is locked until both Breakfast and Lunch are voted
                !voteRepository.existsByUserIdAndVoteDateAndCategory(
                        telegramUserId, today, "breakfast")
                        || !voteRepository.existsByUserIdAndVoteDateAndCategory(
                                telegramUserId, today, "lunch");
            default -> false;
        };
    }

    /**
     * Get lock reason if category is locked
     */
    public String getLockReason(String category) {
        return switch (category.toLowerCase()) {
            case "breakfast" -> ""; // Not locked
            case "lunch" -> "🔒 Vote for Breakfast first";
            case "snack" -> "🔒 Vote for Breakfast and Lunch first";
            default -> "🔒 Category locked";
        };
    }

    /**
     * Validate complete voting rule compliance
     */
    public VotingValidation validateVoting(Long telegramUserId, Dish dish) {
        // Rule 1: Check time deadline
        if (!isVotingTimeValid()) {
            return new VotingValidation(false, "❌ Voting closed! Deadline is 11:00 AM");
        }

        // Rule 2: Check sequential unlock
        if (isCategoryLocked(telegramUserId, dish.getCategory())) {
            String reason = getLockReason(dish.getCategory());
            return new VotingValidation(false, reason);
        }

        // Rule 3: Check if dish is excluded (yesterday's winner)
        if (isDishExcluded(dish.getId())) {
            return new VotingValidation(false, "❌ This dish won yesterday and is excluded today");
        }

        // Rule 4: Check one vote per category per day
        if (hasUserVotedInCategory(telegramUserId, dish.getCategory())) {
            Optional<Vote> existingVote = getUserVoteInCategory(telegramUserId, dish.getCategory());
            if (existingVote.isPresent()) {
                Dish prevDish = existingVote.get().getDish();
                if (prevDish.getId().equals(dish.getId())) {
                    return new VotingValidation(false,
                            "⚠️ You already voted for this dish in " + dish.getCategory());
                }
                // Different dish in same category = revote (allowed)
                return new VotingValidation(true,
                        "✅ Your vote will replace previous vote for " + prevDish.getName());
            }
        }

        return new VotingValidation(true, "✅ Vote accepted");
    }

    /**
     * Inner class representing voting progress
     */
    public static class VotingProgress {
        public final boolean breakfastVoted;
        public final boolean lunchVoted;
        public final boolean snackVoted;

        public VotingProgress(boolean breakfast, boolean lunch, boolean snack) {
            this.breakfastVoted = breakfast;
            this.lunchVoted = lunch;
            this.snackVoted = snack;
        }

        public boolean isComplete() {
            return breakfastVoted && lunchVoted && snackVoted;
        }

        public String getProgressEmoji() {
            int completed = (breakfastVoted ? 1 : 0) + (lunchVoted ? 1 : 0) + (snackVoted ? 1 : 0);
            return switch (completed) {
                case 0 -> "⭕ Start voting";
                case 1 -> "🔵 1/3 Complete";
                case 2 -> "🟢 2/3 Complete";
                case 3 -> "✅ All votes done!";
                default -> "";
            };
        }
    }

    /**
     * Inner class representing validation result
     */
    public static class VotingValidation {
        public final boolean valid;
        public final String message;

        public VotingValidation(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }
    }
}
