package com.example.demo.Service;

import com.example.demo.entity.Dish;
import com.example.demo.entity.Vote;
import com.example.demo.entity.VoteCategory;
import com.example.demo.repository.DishRepository;
import com.example.demo.repository.VoteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class VotingService {
    public VotingService(VoteRepository voteRepository, DishRepository dishRepository) {
        this.voteRepository = voteRepository;
        this.dishRepository = dishRepository;
    }

    private static final Logger log = LoggerFactory.getLogger(VotingService.class);
    private final VoteRepository voteRepository;
    private final DishRepository dishRepository;

    public boolean hasVotedToday(Long telegramUserId, VoteCategory category) {
        return voteRepository.existsByUserIdAndVoteDateAndCategory(telegramUserId, LocalDate.now(), category.name());
    }

    public Optional<Vote> findTodayVote(Long telegramUserId, VoteCategory category) {
        return voteRepository.findByUserIdAndVoteDateAndCategory(telegramUserId, LocalDate.now(), category.name());
    }

    @Transactional
    public VoteResult voteForDish(Long telegramUserId, Dish dish) {
        VoteCategory category = VoteCategory.fromName(dish.getCategory());
        if (category == null) {
            return VoteResult.error("Unsupported dish category.");
        }

        if (hasVotedToday(telegramUserId, category)) {
            Optional<Vote> existingVote = findTodayVote(telegramUserId, category);
            if (existingVote.isPresent() && existingVote.get().getDish().getId().equals(dish.getId())) {
                return VoteResult.alreadyVotedSame(dish);
            }
            return VoteResult.alreadyVotedDifferent(existingVote.orElse(null));
        }

        return recordVote(telegramUserId, dish, category);
    }

    @Transactional
    public VoteResult changeVote(Long telegramUserId, Dish newDish) {
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
        voteRepository.save(vote);

        log.info("New vote recorded: user={} dish={} category={}", telegramUserId, dish.getName(), category);
        return VoteResult.success(dish);
    }

    public List<Vote> getUserHistory(Long telegramUserId, int daysBack) {
        return voteRepository.findAllByUserIdOrderByVoteDateDesc(telegramUserId).stream()
                .filter(vote -> vote.getVoteDate().isAfter(LocalDate.now().minusDays(daysBack)))
                .toList();
    }

    public List<Object[]> getGlobalTopDishes() {
        return voteRepository.findGlobalTopDishes();
    }

    public List<Object[]> getDailyTopDishes() {
        return voteRepository.findDailyTopDishes(LocalDate.now());
    }

    public long countTodayVotes() {
        return voteRepository.findAllByVoteDate(LocalDate.now()).size();
    }

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
            return new VoteResult(true, "✅ Your vote has been recorded!", dish, null, false, false);
        }

        public static VoteResult changed(Dish previousDish, Dish newDish) {
            return new VoteResult(true, "✅ Your vote has been changed!", newDish, previousDish, false, true);
        }

        public static VoteResult alreadyVotedSame(Dish dish) {
            return new VoteResult(false, "You already voted today for " + dish.getName() + ".", dish, null, true,
                    false);
        }

        public static VoteResult alreadyVotedDifferent(Vote existingVote) {
            String current = existingVote != null ? existingVote.getDish().getName() : "another dish";
            return new VoteResult(false, "You already voted today for " + current + ".", null, null, true, false);
        }

        public static VoteResult error(String message) {
            return new VoteResult(false, message, null, null, false, false);
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
}
