package com.example.demo.repository;

import com.example.demo.entity.Dish;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DishRepository extends JpaRepository<Dish, Long> {
    List<Dish> findAllByCategoryIgnoreCaseAndActiveTrueOrderByTotalVotesDesc(String category);
    List<Dish> findAllByActiveTrueOrderByTotalVotesDesc();
    Optional<Dish> findByNameIgnoreCase(String name);
}
