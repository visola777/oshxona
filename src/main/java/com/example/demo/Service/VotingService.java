package com.example.demo.Service;

import com.example.demo.entity.*;
import com.example.demo.repository.DishRepository;
import com.example.demo.repository.ExcludedFoodRepository;
import com.example.demo.repository.TelegramUserRepository;
import com.example.demo.repository.VoteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class VotingService {

    private static final Logger log = LoggerFactory.getLogger(VotingService.class);
    private static final LocalTime VOTING_DEADLINE = LocalTime.of(21, 0);

    private final VoteRepository voteRepository;
    private final DishRepository dishRepository;
    private final TelegramUserRepository userRepository;
    private final ExcludedFoodRepository excludedFoodRepository;

    public VotingService(VoteRepository voteRepository,
            DishRepository dishRepository,
            TelegramUserRepository userRepository,
            ExcludedFoodRepository excludedFoodRepository) {
        this.voteRepository = voteRepository;
        this.dishRepository = dishRepository;
        this.userRepository = userRepository;
        this.excludedFoodRepository = excludedFoodRepository;
    }

    // ========== Public methods ==========

    public boolean hasVotedToday(Long telegramUserId, VoteCategory category) {
        return voteRepository.existsByUserIdAndVoteDateAndCategory(
                telegramUserId,
                LocalDate.now(),          // добавьте эту строку
                category.name()
        );
    }

    public Optional<Vote> findTodayVote(Long telegramUserId, VoteCategory category) {
        return voteRepository.findByUserIdAndVoteDateAndCategory(telegramUserId, LocalDate.now(), category.name());
    }

    @Transactional
    public VoteResult voteForDish(Long telegramUserId, Dish dish) {
        // 1. Voting deadline check (13:00)
        if (LocalTime.now().isAfter(VOTING_DEADLINE) || LocalTime.now().equals(VOTING_DEADLINE)) {
            return VoteResult.error("Ovoz berish vaqti tugadi (13:00 dan keyin).");
        }

        // 3. Exclusion check (yesterday's winner)
        if (isExcludedToday(dish.getId())) {
            return VoteResult.error("Ushbu taom bugun ovoz berish uchun mavjud emas (kechagi g'olib).");
        }

        // 4. Get user (only for sequential unlock check)
        TelegramUser user = userRepository.findByTelegramId(telegramUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 5. Sequential unlock check
        VoteCategory category = VoteCategory.fromName(dish.getCategory());
        if (!isCategoryAccessible(user, category)) {
            return VoteResult.error("Avval oldingi kategoriya uchun ovoz berishingiz kerak.");
        }

        // 6. Check if already voted for this category today -> re-vote allowed
        Optional<Vote> existingVote = findTodayVote(telegramUserId, category);
        if (existingVote.isPresent()) {
            Vote vote = existingVote.get();
            if (vote.getDish().getId().equals(dish.getId())) {
                return VoteResult.alreadyVotedSame(dish);
            }
            // Re-vote: delete old vote, update dish total votes
            Dish oldDish = vote.getDish();
            oldDish.setTotalVotes(Math.max(0, oldDish.getTotalVotes() - 1));
            dishRepository.save(oldDish);
            voteRepository.delete(vote);
        }

        // 7. Record new vote
        return recordVote(telegramUserId, dish, category);
    }

    @Transactional
    public VoteResult changeVote(Long telegramUserId, Dish newDish) {
        // 1. Voting deadline check (13:00)
        if (LocalTime.now().isAfter(VOTING_DEADLINE) || LocalTime.now().equals(VOTING_DEADLINE)) {
            return VoteResult.error("Ovoz berish vaqti tugadi (13:00 dan keyin).");
        }

        VoteCategory newCategory = VoteCategory.fromName(newDish.getCategory());
        if (newCategory == null) {
            return VoteResult.error("Unsupported dish category.");
        }

        Optional<Vote> existingVote = findTodayVote(telegramUserId, newCategory);
        if (existingVote.isEmpty()) {
            return voteForDish(telegramUserId, newDish);
        }

        Vote vote = existingVote.get();
        if (vote.getDish().getId().equals(newDish.getId())) {
            return VoteResult.alreadyVotedSame(newDish);
        }

        Dish previousDish = vote.getDish();
        previousDish.setTotalVotes(Math.max(0, previousDish.getTotalVotes() - 1));
        dishRepository.save(previousDish);

        newDish.setTotalVotes(newDish.getTotalVotes() + 1);
        dishRepository.save(newDish);

        vote.setDish(newDish);
        voteRepository.save(vote);

        return VoteResult.changed(previousDish, newDish);
    }

    @Transactional
    public VoteResult recordVote(Long telegramUserId, Dish dish, VoteCategory category) {
        dish.setTotalVotes(dish.getTotalVotes() + 1);
        dishRepository.save(dish);

        Vote vote = new Vote();
        vote.setUserId(telegramUserId);
        vote.setDish(dish);
        vote.setVoteDate(LocalDate.now());
        vote.setCategory(category.name());
        vote.setVotedAt(LocalDateTime.now());
        vote.setFoodName(dish.getName());
        voteRepository.save(vote);

        log.info("New vote recorded: user={} dish={} category={}", telegramUserId, dish.getName(), category);
        return VoteResult.success(dish);
    }

    public List<Vote> getUserHistory(Long telegramUserId, int daysBack) {
        return voteRepository.findAllByUserIdOrderByVoteDateDesc(telegramUserId).stream()
                .filter(vote -> vote.getVoteDate().isAfter(LocalDate.now().minusDays(daysBack)))
                .collect(Collectors.toList());
    }

    public List<Object[]> getGlobalTopDishes() {
        return voteRepository.findGlobalTopDishes();
    }

    public List<Object[]> getDailyTopDishes() {
        return voteRepository.findDailyTopDishes(LocalDate.now());
    }

    public long countTodayVotes() {
        return voteRepository.countByVoteDate(LocalDate.now());
    }

    // ========== Private helpers ==========

    private boolean isCategoryAccessible(TelegramUser user, VoteCategory category) {
        return switch (category) {
            case BREAKFAST -> true;
            case LUNCH -> hasVotedToday(user.getTelegramId(), VoteCategory.BREAKFAST);
            case SNACK -> hasVotedToday(user.getTelegramId(), VoteCategory.LUNCH);
        };
    }

    private boolean isExcludedToday(Long dishId) {
        return excludedFoodRepository.existsByDishIdAndExcludedDate(dishId, LocalDate.now());
    }

    // ========== Inner VoteResult class ==========

    public static class VoteResult {
        private final boolean success;
        private final String message;
        private final Dish dish;
        private final Dish previousDish;
        private final boolean alreadyVoted;
        private final boolean changed;

        private VoteResult(boolean success, String message, Dish dish, Dish previousDish, boolean alreadyVoted,
                boolean changed) {
            this.success = success;
            this.message = message;
            this.dish = dish;
            this.previousDish = previousDish;
            this.alreadyVoted = alreadyVoted;
            this.changed = changed;
        }

        public static VoteResult success(Dish dish) {
            return new VoteResult(true, "✅ Ovozingiz qabul qilindi!", dish, null, false, false);
        }

        public static VoteResult changed(Dish previousDish, Dish newDish) {
            return new VoteResult(true, "✅ Ovozingiz o'zgartirildi!", newDish, previousDish, false, true);
        }

        public static VoteResult alreadyVotedSame(Dish dish) {
            return new VoteResult(false, "Siz bugun allaqachon " + dish.getName() + " uchun ovoz bergansiz.", dish,
                    null, true, false);
        }

        public static VoteResult alreadyVotedDifferent(Vote existingVote) {
            String current = existingVote != null ? existingVote.getDish().getName() : "boshqa taom";
            return new VoteResult(false, "Siz bugun allaqachon " + current + " uchun ovoz bergansiz.", null, null, true,
                    false);
        }

        public static VoteResult error(String message) {
            return new VoteResult(false, message, null, null, false, false);
        }

        public static VoteResult fail(String message) {
            return error(message);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public Dish getDish() {
            return dish;
        }

        public Dish getPreviousDish() {
            return previousDish;
        }

        public boolean isAlreadyVoted() {
            return alreadyVoted;
        }

        public boolean isChanged() {
            return changed;
        }
    }

    // ========== Scheduler inner class (can be moved to separate file) ==========
    @Component
    public static class VoteResetScheduler {
        @Autowired
        private VoteRepository voteRepository;
        @Autowired
        private DishRepository dishRepository;
        @Autowired
        private ExcludedFoodRepository excludedFoodRepository;

        @Scheduled(cron = "0 5 0 * * ?")
        public void resetAndExcludeWinners() {
            LocalDate yesterday = LocalDate.now().minusDays(1);
            LocalDate today = LocalDate.now();

            for (VoteCategory category : VoteCategory.values()) {
                List<Object[]> results = voteRepository.findVoteCountsByCategoryAndDate(category.name(), yesterday);
                if (results.isEmpty())
                    continue;

                int maxVotes = results.stream()
                        .map(obj -> (Object[]) obj)
                        .mapToInt(row -> ((Number) row[1]).intValue())
                        .max()
                        .orElse(0);

                List<Long> winnerIds = results.stream()
                        .map(obj -> (Object[]) obj)
                        .filter(row -> ((Number) row[1]).intValue() == maxVotes)
                        .map(row -> (Long) row[0])
                        .collect(Collectors.toList());

                for (Long dishId : winnerIds) {
                    ExcludedFood excluded = new ExcludedFood();
                    excluded.setDish(dishRepository.findById(dishId).orElse(null));
                    excluded.setExcludedDate(today);
                    excludedFoodRepository.save(excluded);
                }
            }
        }
    }
}