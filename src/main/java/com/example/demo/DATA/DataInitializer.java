package com.example.demo.DATA;

import com.example.demo.Service.DishExcelImportService;
import com.example.demo.entity.Dish;
import com.example.demo.repository.DishRepository;
import com.example.demo.repository.VoteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Bot ishga tushganda dishes.xlsx faylidan barcha taomlarni yuklab oladi.
 *
 * Logika:
 *   1. Excelni o'qish (yoki namuna fayl yaratish)
 *   2. Eski Dish lar va ularga bog'liq Vote lar tozalanadi
 *   3. Exceldagi taomlar yangidan saqlanadi
 *
 * Shunday qilib excelda nima yozsen — bot menyusi shunday bo'ladi.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final DishExcelImportService excelService;
    private final DishRepository dishRepository;
    private final VoteRepository voteRepository;

    public DataInitializer(DishExcelImportService excelService,
                           DishRepository dishRepository,
                           VoteRepository voteRepository) {
        this.excelService = excelService;
        this.dishRepository = dishRepository;
        this.voteRepository = voteRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        log.info("🍽️  Excel orqali taomlar yuklanmoqda...");

        List<Dish> excelDishes = excelService.importFromExcel();

        if (excelDishes.isEmpty()) {
            log.warn("⚠️  Exceldan hech qanday taom o'qilmadi! Faylni tekshiring: {}",
                    excelService.getExcelFilePath());
            return;
        }

        // Eski ma'lumotlarni tozalash (avval votes — chunki Dish ga bog'langan)
        long oldVotes = voteRepository.count();
        long oldDishes = dishRepository.count();

        if (oldVotes > 0) {
            voteRepository.deleteAllInBatch();
            log.info("🗑️  Eski {} ta ovoz tozalandi", oldVotes);
        }
        if (oldDishes > 0) {
            dishRepository.deleteAllInBatch();
            log.info("🗑️  Eski {} ta taom tozalandi", oldDishes);
        }

        // Yangilarini saqlash
        dishRepository.saveAll(excelDishes);

        log.info("✅ Exceldan {} ta taom muvaffaqiyatli yuklandi", excelDishes.size());

        // Kategoriyalar bo'yicha hisobot
        long breakfast = excelDishes.stream().filter(d -> "BREAKFAST".equals(d.getCategory())).count();
        long lunch = excelDishes.stream().filter(d -> "LUNCH".equals(d.getCategory())).count();
        long snack = excelDishes.stream().filter(d -> "SNACK".equals(d.getCategory())).count();

        log.info("   🌅 Nonushta: {} ta | 🍜 Tushlik: {} ta | 🍰 Poldnik: {} ta", breakfast, lunch, snack);
    }
}
