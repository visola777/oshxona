package com.example.demo.Service;

import com.example.demo.entity.Dish;
import com.example.demo.repository.DishRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * MealDishService bilan parallel — bot kodida ishlatiladi.
 * Barcha ma'lumot dishes.xlsx fayldan keladi.
 */
@Service
public class DishService {

    private static final Logger log = LoggerFactory.getLogger(DishService.class);
    private final DishRepository dishRepository;

    public DishService(DishRepository dishRepository) {
        this.dishRepository = dishRepository;
    }

    public List<Dish> getActiveDishesByCategory(String category) {
        return dishRepository.findAllByCategoryIgnoreCaseAndActiveTrueOrderByTotalVotesDesc(category);
    }

    public List<Dish> getActiveDishes() {
        return dishRepository.findAllByActiveTrueOrderByTotalVotesDesc();
    }

    public Dish getDishById(Long id) {
        return dishRepository.findById(id).orElse(null);
    }

    @Transactional
    public Dish saveDish(Dish dish) {
        Dish saved = dishRepository.save(dish);
        log.debug("Saved dish {} with id {}", saved.getName(), saved.getId());
        return saved;
    }
}
