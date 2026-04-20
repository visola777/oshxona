package com.example.demo.DATA;

// ============================================
// 8. DATA INITIALIZER
// ============================================

import com.example.demo.Service.MealDishService;
import com.example.demo.entity.Dish;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private final MealDishService dishService;
    
    public DataInitializer(MealDishService dishService) {
        this.dishService = dishService;
    }

    @Override
    public void run(String... args) {
        if (dishService.getAllActiveDishes().isEmpty()) {
            List<Dish> defaultDishes = dishService.getDefaultDishes();
            defaultDishes.forEach(dishService::save);
            log.info("✅ Loaded default dishes into the database.");
        } else {
            log.info("✅ Dishes already exist in the database.");
        }
    }
}