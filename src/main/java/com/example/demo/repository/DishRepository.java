package com.example.demo.repository;

import com.example.demo.entity.Dish;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DishRepository extends JpaRepository<Dish, Long> {

    List<Dish> findByName(String name);


    List<Dish> findAllByActiveTrueOrderByTotalVotesDesc();

    List<Dish> findAllByCategoryIgnoreCaseAndActiveTrueOrderByTotalVotesDesc(String category);

    Optional<Object> findByNameIgnoreCase(String name);
    // Find all dishes with exactly the given name (case-sensitive)
    List<Dish> z(String name);

    // Find all dishes in a given category
    List<Dish> findByCategory(String category);

    // Optional: find by name ignoring case
    // List<Dish> findByNameIgnoreCase(String name);
    // Add this method to your DishRepository interface
    @Query("SELECT d FROM Dish d WHERE d.category = :category")
    List<Dish> findActiveByCategory(@Param("category") String category);
}
