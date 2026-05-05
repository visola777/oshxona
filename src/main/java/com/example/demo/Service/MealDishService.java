package com.example.demo.Service;

import com.example.demo.entity.Dish;
import com.example.demo.repository.DishRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Taomlar bilan ishlash servisi.
 * Barcha ma'lumot dishes.xlsx fayldan keladi (DataInitializer orqali).
 */
@Service
public class MealDishService {

    private static final Logger log = LoggerFactory.getLogger(MealDishService.class);
    private final DishRepository dishRepository;

    public MealDishService(DishRepository dishRepository) {
        this.dishRepository = dishRepository;
    }

    public List<Dish> getActiveDishesByCategory(String category) {
        return dishRepository.findAllByCategoryIgnoreCaseAndActiveTrueOrderByTotalVotesDesc(category);
    }

    public List<Dish> getAllActiveDishes() {
        return dishRepository.findAllByActiveTrueOrderByTotalVotesDesc();
    }

    public Dish getDish(Long id) {
        return dishRepository.findById(id).orElse(null);
    }

    @Transactional
    public Dish save(Dish dish) {
        Dish saved = dishRepository.save(dish);
        log.debug("Saved dish {} with id {}", saved.getName(), saved.getId());
        return saved;
    }
}
