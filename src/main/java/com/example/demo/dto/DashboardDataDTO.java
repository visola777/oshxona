package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO for website dashboard showing aggregated statistics
 */
public class DashboardDataDTO {
    @JsonProperty("date")
    private LocalDate date;

    @JsonProperty("lastUpdated")
    private String lastUpdated;

    @JsonProperty("categories")
    private List<StatisticsDTO> categories;

    @JsonProperty("globalTopDishes")
    private List<DishVoteDTO> globalTopDishes;

    @JsonProperty("totalVotesToday")
    private Long totalVotesToday;

    @JsonProperty("totalVotersToday")
    private Long totalVotersToday;

    public DashboardDataDTO() {
    }

    public DashboardDataDTO(LocalDate date, String lastUpdated, List<StatisticsDTO> categories,
            List<DishVoteDTO> globalTopDishes, Long totalVotesToday, Long totalVotersToday) {
        this.date = date;
        this.lastUpdated = lastUpdated;
        this.categories = categories;
        this.globalTopDishes = globalTopDishes;
        this.totalVotesToday = totalVotesToday;
        this.totalVotersToday = totalVotersToday;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public List<StatisticsDTO> getCategories() {
        return categories;
    }

    public void setCategories(List<StatisticsDTO> categories) {
        this.categories = categories;
    }

    public List<DishVoteDTO> getGlobalTopDishes() {
        return globalTopDishes;
    }

    public void setGlobalTopDishes(List<DishVoteDTO> globalTopDishes) {
        this.globalTopDishes = globalTopDishes;
    }

    public Long getTotalVotesToday() {
        return totalVotesToday;
    }

    public void setTotalVotesToday(Long totalVotesToday) {
        this.totalVotesToday = totalVotesToday;
    }

    public Long getTotalVotersToday() {
        return totalVotersToday;
    }

    public void setTotalVotersToday(Long totalVotersToday) {
        this.totalVotersToday = totalVotersToday;
    }
}
