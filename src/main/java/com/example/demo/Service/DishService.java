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
public class DishService {
    public DishService(DishRepository dishRepository) {
        this.dishRepository = dishRepository;
    }
    private static final Logger log = LoggerFactory.getLogger(DishService.class);
    private final DishRepository dishRepository;

    public List<Dish> getActiveDishesByCategory(String category) {
        return dishRepository.findAllByCategoryIgnoreCaseAndActiveTrueOrderByTotalVotesDesc(category);
    }

    public List<Dish> getActiveDishes() {
        return dishRepository.findAllByActiveTrueOrderByTotalVotesDesc();
    }

    public Dish getDishById(Long id) {
        return dishRepository.findById(id).orElse(null);
    }

    public Dish getDishByName(String name) {
        return dishRepository.findByNameIgnoreCase(name).orElse(null);
    }

    @Transactional
    public Dish saveDish(Dish dish) {
        Dish saved = dishRepository.save(dish);
        log.debug("Saved dish {} with id {}", saved.getName(), saved.getId());
        return saved;
    }

    public List<Dish> getDefaultDishes() {
        List<Dish> dishes = new ArrayList<>();
        dishes.add(createDish("Shirguruch", VoteCategory.BREAKFAST.name(), "https://example.com/shirguruch.jpg", "Creamy rice with milk, honey and dried fruit."));
        dishes.add(createDish("Mannaya kasha", VoteCategory.BREAKFAST.name(), "https://example.com/kasha.jpg", "Warm semolina porridge with butter and jam."));
        dishes.add(createDish("Qovurilgan tuxum", VoteCategory.BREAKFAST.name(), "https://example.com/tuxum.jpg", "Golden fried eggs served with fresh herbs."));
        dishes.add(createDish("Lag'mon", VoteCategory.LUNCH.name(), "https://example.com/lagmon.jpg", "Hand-pulled noodle soup with beef and vegetables."));
        dishes.add(createDish("Mastava", VoteCategory.LUNCH.name(), "https://example.com/mastava.jpg", "Hearty rice soup with lamb and fresh greens."));
        dishes.add(createDish("Chuchvara", VoteCategory.LUNCH.name(), "https://example.com/chuchvara.jpg", "Savory dumplings in rich broth."));
        dishes.add(createDish("Somsa kartoshkali", VoteCategory.SNACK.name(), "https://example.com/somsa.jpg", "Crispy pastry filled with spiced potato."));
        dishes.add(createDish("Pitsa", VoteCategory.SNACK.name(), "https://example.com/pitsa.jpg", "Mini pizza slices perfect for a quick snack."));
        dishes.add(createDish("Sinabon", VoteCategory.SNACK.name(), "https://example.com/sinabon.jpg", "Sweet cinnamon roll with glaze."));
        return dishes;
    }

    private Dish createDish(String name, String category, String url, String description) {
        Dish dish = new Dish();
        dish.setName(name);
        dish.setCategory(category);
        dish.setPhotoUrl(url);
        dish.setDescription(description);
        dish.setActive(true);
        dish.setTotalVotes(0);
        return dish;
    }
}
