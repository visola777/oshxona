//package com.example.demo.repository;
//
//import com.example.demo.entity.Dish;
//import com.example.demo.entity.Vote;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;
//
//import java.time.LocalDate;
//import java.util.List;
//import java.util.Optional;
//
//@Repository
//public interface VoteRepository extends JpaRepository<Vote, Long> {
//
//    // Existing methods (keep as is)
//    boolean existsByUserIdAndVoteDateAndCategory(Long userId, LocalDate voteDate, String category);
//    Optional<Vote> findByUserIdAndVoteDateAndCategory(Long userId, LocalDate voteDate, String category);
//    List<Vote> findAllByUserIdOrderByVoteDateDesc(Long userId);
//    List<Vote> findAllByVoteDate(LocalDate voteDate);
//    boolean existsByUserIdAndFoodName(Long userId, String foodName);
//
//    @Query("SELECT v.dish, COUNT(v) FROM Vote v GROUP BY v.dish ORDER BY COUNT(v) DESC")
//    List<Object[]> findGlobalTopDishes();
//
//    @Query("SELECT v.dish, COUNT(v) FROM Vote v WHERE v.voteDate = :voteDate GROUP BY v.dish ORDER BY COUNT(v) DESC")
//    List<Object[]> findDailyTopDishes(@Param("voteDate") LocalDate voteDate);
//
//    long countByDish(Dish dish);
//
//    // API Statistics Methods
//    List<Vote> findByVoteDateAndCategory(LocalDate voteDate, String category);
//    List<Vote> findByVoteDate(LocalDate voteDate);
//
//    @Query("SELECT COUNT(v) FROM Vote v WHERE v.dish.id = :dishId AND v.voteDate = :voteDate")
//    long countByDishIdAndVoteDate(@Param("dishId") Long dishId, @Param("voteDate") LocalDate voteDate);
//
//    @Query(value = "SELECT v.* FROM votes v WHERE v.vote_date = :voteDate ORDER BY (SELECT COUNT(*) FROM votes v2 WHERE v2.dish_id = v.dish_id AND v2.vote_date = :voteDate) DESC LIMIT 1", nativeQuery = true)
//    Vote findGlobalTopDishByDate(@Param("voteDate") LocalDate voteDate);
//
//    @Query("SELECT COUNT(DISTINCT v.userId) FROM Vote v WHERE v.voteDate = :voteDate")
//    long countUniqueVotersByDate(@Param("voteDate") LocalDate voteDate);
//
//    // ========== FIXED QUERIES (use userId, not user.telegramId) ==========
//
//    @Query("SELECT COUNT(v) FROM Vote v WHERE v.category = :category AND v.voteDate = :date")
//    int countByCategoryAndDate(@Param("category") String category, @Param("date") LocalDate date);
//
//    @Query("SELECT v.dish.id, COUNT(v) FROM Vote v WHERE v.category = :category AND v.voteDate = :date GROUP BY v.dish.id")
//    List<Object[]> findVoteCountsByCategoryAndDate(@Param("category") String category, @Param("date") LocalDate date);
//
//    @Query("SELECT v FROM Vote v WHERE v.userId = :userId AND v.voteDate >= :fromDate ORDER BY v.voteDate DESC")
//    List<Vote> findByUserIdAndDateAfter(@Param("userId") Long userId, @Param("fromDate") LocalDate fromDate);
//
//    @Query("SELECT d.name, COUNT(v) FROM Vote v JOIN v.dish d GROUP BY d.name ORDER BY COUNT(v) DESC")
//    List<Object[]> findGlobalTopDishesWithLimit(@Param("limit") int limit);
//
//    @Query("SELECT CASE WHEN COUNT(v) > 0 THEN true ELSE false END FROM Vote v WHERE v.userId = :userId AND v.category = :category AND v.voteDate = CURRENT_DATE")
//    boolean hasVotedToday(@Param("userId") Long userId, @Param("category") String category);
//
//    @Query("SELECT v FROM Vote v WHERE v.userId = :userId AND v.category = :category AND v.voteDate = CURRENT_DATE")
//    Vote findTodayVoteByUserAndCategory(@Param("userId") Long userId, @Param("category") String category);
//
//    @Query("SELECT COUNT(v) FROM Vote v WHERE v.voteDate = :voteDate")
//    long countByVoteDate(@Param("voteDate") LocalDate voteDate);
//
//    @Query("SELECT COUNT(v) FROM Vote v WHERE v.voteDate = :voteDate AND v.category = :category")
//    long countByVoteDateAndCategory(@Param("voteDate") LocalDate voteDate, @Param("category") String category);
//}

package com.example.demo.repository;

import com.example.demo.entity.Dish;
import com.example.demo.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {

    // Existing methods (keep as needed)
    boolean existsByUserIdAndVoteDateAndCategory(Long userId, LocalDate voteDate, String category);
    Optional<Vote> findByUserIdAndVoteDateAndCategory(Long userId, LocalDate voteDate, String category);
    List<Vote> findAllByUserIdOrderByVoteDateDesc(Long userId);
    List<Vote> findAllByVoteDate(LocalDate voteDate);
    boolean existsByUserIdAndFoodName(Long userId, String foodName);

    @Query("SELECT v.dish, COUNT(v) FROM Vote v GROUP BY v.dish ORDER BY COUNT(v) DESC")
    List<Object[]> findGlobalTopDishes();

    @Query("SELECT v.dish, COUNT(v) FROM Vote v WHERE v.voteDate = :voteDate GROUP BY v.dish ORDER BY COUNT(v) DESC")
    List<Object[]> findDailyTopDishes(@Param("voteDate") LocalDate voteDate);

    long countByDish(Dish dish);

    // API methods
    List<Vote> findByVoteDateAndCategory(LocalDate voteDate, String category);
    List<Vote> findByVoteDate(LocalDate voteDate);

    @Query("SELECT COUNT(v) FROM Vote v WHERE v.dish.id = :dishId AND v.voteDate = :voteDate")
    long countByDishIdAndVoteDate(@Param("dishId") Long dishId, @Param("voteDate") LocalDate voteDate);

    @Query(value = "SELECT v.* FROM votes v WHERE v.vote_date = :voteDate ORDER BY (SELECT COUNT(*) FROM votes v2 WHERE v2.dish_id = v.dish_id AND v2.vote_date = :voteDate) DESC LIMIT 1", nativeQuery = true)
    Vote findGlobalTopDishByDate(@Param("voteDate") LocalDate voteDate);

    @Query("SELECT COUNT(DISTINCT v.userId) FROM Vote v WHERE v.voteDate = :voteDate")
    long countUniqueVotersByDate(@Param("voteDate") LocalDate voteDate);

    // ========== Additional methods used by StatisticsService and VotingService ==========
    @Query("SELECT COUNT(v) FROM Vote v WHERE v.category = :category AND v.voteDate = :date")
    int countByCategoryAndDate(@Param("category") String category, @Param("date") LocalDate date);

    @Query("SELECT v.dish.id, COUNT(v) FROM Vote v WHERE v.category = :category AND v.voteDate = :date GROUP BY v.dish.id")
    List<Object[]> findVoteCountsByCategoryAndDate(@Param("category") String category, @Param("date") LocalDate date);

    @Query("SELECT v FROM Vote v WHERE v.userId = :userId AND v.voteDate >= :fromDate ORDER BY v.voteDate DESC")
    List<Vote> findByUserIdAndDateAfter(@Param("userId") Long userId, @Param("fromDate") LocalDate fromDate);

    @Query("SELECT d.name, COUNT(v) FROM Vote v JOIN v.dish d GROUP BY d.name ORDER BY COUNT(v) DESC")
    List<Object[]> findGlobalTopDishesWithLimit();

    @Query("SELECT CASE WHEN COUNT(v) > 0 THEN true ELSE false END FROM Vote v WHERE v.userId = :userId AND v.category = :category AND v.voteDate = CURRENT_DATE")
    boolean hasVotedToday(@Param("userId") Long userId, @Param("category") String category);

    @Query("SELECT v FROM Vote v WHERE v.userId = :userId AND v.category = :category AND v.voteDate = CURRENT_DATE")
    Vote findTodayVoteByUserAndCategory(@Param("userId") Long userId, @Param("category") String category);

    // Convenience default methods
    default long countByVoteDate(LocalDate voteDate) {
        return findAllByVoteDate(voteDate).size();
    }

    default long countByVoteDateAndCategory(LocalDate voteDate, String category) {
        return findByVoteDateAndCategory(voteDate, category).size();
    }
}