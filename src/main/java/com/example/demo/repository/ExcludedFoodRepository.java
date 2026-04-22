package com.example.demo.repository;

import com.example.demo.entity.ExcludedFood;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;

public interface ExcludedFoodRepository extends JpaRepository<ExcludedFood, Long> {
    boolean existsByDishIdAndExcludedDate(Long dishId, LocalDate date);
}