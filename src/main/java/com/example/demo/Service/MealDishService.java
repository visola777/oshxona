package com.example.demo.Service;

import com.example.demo.entity.Dish;
import com.example.demo.entity.VoteCategory;
import com.example.demo.repository.DishRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MealDishService {
    private static final Logger log = LoggerFactory.getLogger(MealDishService.class);
    private final DishRepository dishRepository;

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

    public List<Dish> getDefaultDishes() {
        List<Dish> dishes = new ArrayList<>();
        dishes.add(defaultDish("Shirguruch", VoteCategory.BREAKFAST.name(), "https://example.com/shirguruch.jpg", "Creamy rice with milk, honey and dried fruit."));
        dishes.add(defaultDish("Mannaya kasha", VoteCategory.BREAKFAST.name(), "https://example.com/kasha.jpg", "Warm semolina porridge with butter and jam."));
        dishes.add(defaultDish("Qovurilgan tuxum", VoteCategory.BREAKFAST.name(), "https://example.com/tuxum.jpg", "Golden fried eggs served with fresh herbs."));
        dishes.add(defaultDish("Lag'mon", VoteCategory.LUNCH.name(), "https://example.com/lagmon.jpg", "Hand-pulled noodle soup with beef and vegetables."));
        dishes.add(defaultDish("Mastava", VoteCategory.LUNCH.name(), "https://example.com/mastava.jpg", "Hearty rice soup with lamb and fresh greens."));
        dishes.add(defaultDish("Chuchvara", VoteCategory.LUNCH.name(), "https://example.com/chuchvara.jpg", "Savory dumplings in rich broth."));
        dishes.add(defaultDish("Somsa kartoshkali", VoteCategory.SNACK.name(), "https://example.com/somsa.jpg", "Crispy pastry filled with spiced potato."));
        dishes.add(defaultDish("Pitsa", VoteCategory.SNACK.name(), "https://example.com/pitsa.jpg", "Mini pizza slices perfect for a quick snack."));
        dishes.add(defaultDish("Sinabon", VoteCategory.SNACK.name(), "https://example.com/sinabon.jpg", "Sweet cinnamon roll with glaze."));
        return dishes;
    }

    private Dish defaultDish(String name, String category, String imageUrl, String description) {
        return Dish.builder()
                .name(name)
                .category(category)
                .photoUrl(imageUrl)
                .description(description)
                .active(true)
                .totalVotes(0)
                .build();
    }
}
