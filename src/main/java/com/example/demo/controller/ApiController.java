package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

/**
 * REST API Controller for general API information and health checks
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ApiController {

    /**
     * API health check
     * GET /api/health
     * 
     * @return Health status
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "healthy");
        response.put("service", "Telegram Food Voting Bot API");
        response.put("version", "1.0.0");
        return ResponseEntity.ok(response);
    }

    /**
     * API information
     * GET /api/info
     * 
     * @return API information and available endpoints
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getInfo() {
        Map<String, Object> response = new HashMap<>();
        response.put("name", "Telegram Food Voting Bot");
        response.put("version", "1.0.0");
        response.put("description", "Real-time voting statistics API for school meal voting system");

        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("GET /api/health", "Check API health status");
        endpoints.put("GET /api/info", "Get API information");
        endpoints.put("GET /api/stats/dashboard", "Get complete dashboard data");
        endpoints.put("GET /api/stats/top-dishes", "Get today's top dishes");
        endpoints.put("GET /api/stats/category/{category}", "Get statistics for a category");
        endpoints.put("GET /api/stats/dish/{dishId}", "Get statistics for a specific dish");

        response.put("endpoints", endpoints);
        return ResponseEntity.ok(response);
    }
}
