package com.example.demo.Service;

// VoteService.java

import com.example.demo.entity.Food;
import com.example.demo.entity.Vote;
import com.example.demo.repository.FoodRepository;

import com.example.demo.repository.VoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@RequiredArgsConstructor
public class VoteService {
    private final VoteRepository voteRepository;
    private final FoodRepository foodRepository;

    @Transactional
    public boolean vote(Long userId, String foodName) {
        if (voteRepository.existsByUserIdAndFoodName(userId, foodName)) {
            return false;
        }

        Optional<Food> foodOpt = foodRepository.findByName(foodName);
        if (foodOpt.isPresent()) {
            Food food = foodOpt.get();
            food.setVotes(food.getVotes() + 1);
            foodRepository.save(food);

            Vote vote = new Vote(userId, foodName);
            voteRepository.save(vote);
            return true;
        }
        return false;
    }

    public List<Map.Entry<String, Integer>> getTop3Foods() {
        List<Food> foods = foodRepository.findAllByOrderByVotesDesc();
        List<Map.Entry<String, Integer>> top3 = new ArrayList<>();

        for (int i = 0; i < Math.min(3, foods.size()); i++) {
            Food food = foods.get(i);
            top3.add(new AbstractMap.SimpleEntry<>(food.getName(), food.getVotes()));
        }
        return top3;
    }

    public String getStatistics() {
        List<Food> foods = foodRepository.findAllByOrderByVotesDesc();
        StringBuilder sb = new StringBuilder("📊 Statistika:\n\n");

        for (Food food : foods) {
            if (food.getVotes() > 0) {
                sb.append(food.getName()).append(" — ").append(food.getVotes()).append("\n");
            }
        }

        if (sb.toString().equals("📊 Statistika:\n\n")) {
            sb.append("Hali hech kim ovoz bermagan");
        }

        return sb.toString();
    }
}
