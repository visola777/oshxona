package com.example.demo.repository;

import com.example.demo.entity.Dish;
import com.example.demo.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
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

    // API Statistics Methods
    List<Vote> findByVoteDateAndCategory(LocalDate voteDate, String category);

    List<Vote> findByVoteDate(LocalDate voteDate);

    @Query("SELECT COUNT(v) FROM Vote v WHERE v.dish.id = :dishId AND v.voteDate = :voteDate")
    long countByDishIdAndVoteDate(Long dishId, LocalDate voteDate);

    // Validation Methods
    @Query(value = "SELECT v.* FROM votes v WHERE v.vote_date = :voteDate ORDER BY (SELECT COUNT(*) FROM votes v2 WHERE v2.dish_id = v.dish_id AND v2.vote_date = :voteDate) DESC LIMIT 1", nativeQuery = true)
    Vote findGlobalTopDishByDate(LocalDate voteDate);

    @Query("SELECT COUNT(DISTINCT v.userId) FROM Vote v WHERE v.voteDate = :voteDate")
    long countUniqueVotersByDate(LocalDate voteDate);

    Collection<Object> findVoteCountsByCategoryAndDate(String name, LocalDate today);

    boolean hasVotedToday(Long telegramUserId, String name);

    @Query("SELECT COUNT(v) FROM Vote v WHERE v.voteDate = :voteDate")
    long countByVoteDate(LocalDate voteDate);

    @Query("SELECT COUNT(v) FROM Vote v WHERE v.category = :category AND v.voteDate = :voteDate")
    long countByCategoryAndDate(String category, LocalDate voteDate);
}