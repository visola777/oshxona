package com.example.demo.controller;

import com.example.demo.Service.ApiStatisticsService;
import com.example.demo.dto.DashboardDataDTO;
import com.example.demo.dto.DishVoteDTO;
import com.example.demo.dto.StatisticsDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API Controller for website dashboard statistics
 * Provides real-time voting data and statistics
 */
@RestController
@RequestMapping("/api/stats")
@CrossOrigin(origins = "*", maxAge = 3600)
public class StatisticsController {
    private final ApiStatisticsService apiStatisticsService;

    public StatisticsController(ApiStatisticsService apiStatisticsService) {
        this.apiStatisticsService = apiStatisticsService;
    }

    /**
     * Get comprehensive dashboard data with all statistics
     * GET /api/stats/dashboard
     * 
     * @return DashboardDataDTO containing all statistics
     */
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardDataDTO> getDashboard() {
        try {
            DashboardDataDTO data = apiStatisticsService.getDashboardData();
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Get today's top dishes across all categories
     * GET /api/stats/top-dishes?limit=5
     * 
     * @param limit Maximum number of dishes to return (default: 5)
     * @return List of top dishes sorted by vote count
     */
    @GetMapping("/top-dishes")
    public ResponseEntity<List<DishVoteDTO>> getTopDishes(
            @RequestParam(defaultValue = "5") int limit) {
        try {
            List<DishVoteDTO> topDishes = apiStatisticsService.getTodayTopDishes(limit);
            return ResponseEntity.ok(topDishes);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Get statistics for a specific category
     * GET /api/stats/category/{category}
     * 
     * @param category Category name (breakfast, lunch, snack)
     * @return StatisticsDTO with all dishes and their vote counts
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<StatisticsDTO> getCategoryStats(
            @PathVariable String category) {
        try {
            StatisticsDTO stats = apiStatisticsService.getStatsByCategory(category);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Get statistics for a specific dish
     * GET /api/stats/dish/{dishId}
     * 
     * @param dishId ID of the dish
     * @return DishVoteDTO with vote count and percentage
     */
    @GetMapping("/dish/{dishId}")
    public ResponseEntity<DishVoteDTO> getDishStats(
            @PathVariable Long dishId) {
        try {
            DishVoteDTO dishStats = apiStatisticsService.getDishStatistics(dishId);
            if (dishStats == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(dishStats);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Health check endpoint
     * GET /api/stats/health
     * 
     * @return Simple status message
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Statistics API is running ✓");
    }
}
