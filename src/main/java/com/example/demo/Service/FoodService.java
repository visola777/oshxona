package com.example.demo.Service;

// ============================================
// 5. SERVICE LAYER
// ============================================

// FoodService.java

import com.example.demo.entity.Food;
import com.example.demo.repository.FoodRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class FoodService {
    public FoodService(FoodRepository foodRepository) {
        this.foodRepository = foodRepository;
    }

    private final FoodRepository foodRepository;

    public List<Food> getFoodsByCategory(String category) {
        return foodRepository.findByCategory(category);
    }

    public Food getFoodByName(String name) {
        return foodRepository.findByName(name).orElse(null);
    }

    public void updateFood(Food food) {
        foodRepository.save(food);
    }

    public void initFoods() {
        if (foodRepository.count() == 0) {
            // Nonushta
            saveFood("Shirguruch", "nonushta", "https://example.com/shirguruch.jpg");
            saveFood("Mannaya kasha", "nonushta", "https://example.com/kasha.jpg");
            saveFood("Quymoq (shokoladli)", "nonushta", "https://example.com/quymoq.jpg");
            saveFood("Quymoq (tvorogli)", "nonushta", "https://example.com/quymoq.jpg");
            saveFood("Qovurilgan tuxum", "nonushta", "https://example.com/tuxum.jpg");
            saveFood("Qaynatilgan tuxum", "nonushta", "https://example.com/tuxum.jpg");
            saveFood("Bulochka", "nonushta", "https://example.com/bulochka.jpg");

            // Obed
            saveFood("Lag'mon", "obed", "https://example.com/lagmon.jpg");
            saveFood("Mampar", "obed", "https://example.com/mampar.jpg");
            saveFood("Mastava", "obed", "https://example.com/mastava.jpg");
            saveFood("Teftel", "obed", "https://example.com/teftel.jpg");
            saveFood("Chuchvara", "obed", "https://example.com/chuchvara.jpg");
            saveFood("Sho'rva", "obed", "https://example.com/shorva.jpg");
            saveFood("Borsh", "obed", "https://example.com/borsh.jpg");
            saveFood("Noxot sho'rva", "obed", "https://example.com/noxot.jpg");

            // Poldnik
            saveFood("Somsa kartoshkali", "poldnik", "https://example.com/somsa.jpg");
            saveFood("Somsa tovuqli", "poldnik", "https://example.com/somsa.jpg");
            saveFood("Pitsa", "poldnik", "https://example.com/pitsa.jpg");
            saveFood("Pirog", "poldnik", "https://example.com/pirog.jpg");
            saveFood("Bulochka tvorogli", "poldnik", "https://example.com/bulochka.jpg");
            saveFood("Bulochka quyultirilgan sutli", "poldnik", "https://example.com/bulochka.jpg");
            saveFood("Sinabon", "poldnik", "https://example.com/sinabon.jpg");
            saveFood("Rastegay", "poldnik", "https://example.com/rastegay.jpg");
        }
    }

    private void saveFood(String name, String category, String imageUrl) {
        Food food = new Food();
        food.setName(name);
        food.setCategory(category);
        food.setImageUrl(imageUrl);
        foodRepository.save(food);
    }
}
