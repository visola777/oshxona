package com.example.demo.repository;

import com.example.demo.entity.Dish;
import com.example.demo.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {
    boolean existsByUserIdAndVoteDateAndCategory(Long userId, LocalDate voteDate, String category);

    Optional<Vote> findByUserIdAndVoteDateAndCategory(Long userId, LocalDate voteDate, String category);

    List<Vote> findAllByUserIdOrderByVoteDateDesc(Long userId);

    List<Vote> findAllByVoteDate(LocalDate voteDate);

    boolean existsByUserIdAndFoodName(Long userId, String foodName);

    @Query("SELECT v.dish, COUNT(v) FROM Vote v GROUP BY v.dish ORDER BY COUNT(v) DESC")
    List<Object[]> findGlobalTopDishes();

    @Query("SELECT v.dish, COUNT(v) FROM Vote v WHERE v.voteDate = :voteDate GROUP BY v.dish ORDER BY COUNT(v) DESC")
    List<Object[]> findDailyTopDishes(LocalDate voteDate);

    long countByDish(Dish dish);
}