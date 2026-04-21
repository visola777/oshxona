package com.example.demo.Service;

import com.example.demo.entity.TelegramUser;
import com.example.demo.repository.TelegramUserRepository;
import org.springframework.stereotype.Service;

/**
 * Service for managing user session state and voting progress
 * Tracks which categories user has voted in and their voting sequence
 */
@Service
public class SessionStateManager {
    private final TelegramUserRepository userRepository;
    private final VotingValidationService votingValidationService;

    public SessionStateManager(TelegramUserRepository userRepository,
                              VotingValidationService votingValidationService) {
        this.userRepository = userRepository;
        this.votingValidationService = votingValidationService;
    }

    /**
     * Get user's current voting state
     */
    public UserVotingState getUserState(Long telegramUserId) {
        VotingValidationService.VotingProgress progress = 
            votingValidationService.getUserVotingProgress(telegramUserId);
        
        return new UserVotingState(
            telegramUserId,
            progress.breakfastVoted,
            progress.lunchVoted,
            progress.snackVoted,
            progress.getProgressEmoji()
        );
    }

    /**
     * Get next available category for user
     */
    public String getNextCategory(Long telegramUserId) {
        UserVotingState state = getUserState(telegramUserId);
        
        if (!state.breakfastVoted) {
            return "breakfast";
        } else if (!state.lunchVoted) {
            return "lunch";
        } else if (!state.snackVoted) {
            return "snack";
        }
        return null; // All voted
    }

    /**
     * Check if user has completed all voting for today
     */
    public boolean isUserVotingComplete(Long telegramUserId) {
        UserVotingState state = getUserState(telegramUserId);
        return state.isComplete();
    }

    /**
     * Get readable voting progress message
     */
    public String getProgressMessage(Long telegramUserId) {
        UserVotingState state = getUserState(telegramUserId);
        
        StringBuilder sb = new StringBuilder();
        sb.append("📊 Today's Voting Progress\n\n");
        
        sb.append(state.breakfastVoted ? "✅ Breakfast voted\n" : "⭕ Breakfast pending\n");
        sb.append(state.lunchVoted ? "✅ Lunch voted\n" : "⭕ Lunch pending\n");
        sb.append(state.snackVoted ? "✅ Snack voted\n" : "⭕ Snack pending\n");
        
        sb.append("\n").append(state.progressEmoji);
        
        if (!state.isComplete()) {
            String next = getNextCategory(telegramUserId);
            if (next != null) {
                sb.append("\n\n➡️ Next: Vote for ").append(next.toUpperCase());
            }
        } else {
            sb.append("\n🎉 Thank you for voting!");
        }
        
        return sb.toString();
    }

    /**
     * Inner class representing user's voting state
     */
    public static class UserVotingState {
        public final Long userId;
        public final boolean breakfastVoted;
        public final boolean lunchVoted;
        public final boolean snackVoted;
        public final String progressEmoji;
        
        public UserVotingState(Long userId, boolean breakfast, boolean lunch, 
                              boolean snack, String emoji) {
            this.userId = userId;
            this.breakfastVoted = breakfast;
            this.lunchVoted = lunch;
            this.snackVoted = snack;
            this.progressEmoji = emoji;
        }
        
        public boolean isComplete() {
            return breakfastVoted && lunchVoted && snackVoted;
        }
        
        public int getCompletionPercentage() {
            int count = (breakfastVoted ? 1 : 0) + (lunchVoted ? 1 : 0) + (snackVoted ? 1 : 0);
            return (count * 100) / 3;
        }
    }
}
