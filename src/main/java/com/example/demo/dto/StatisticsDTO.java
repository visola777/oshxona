package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO representing comprehensive statistics for a voting category
 */
public class StatisticsDTO {
    @JsonProperty("date")
    private LocalDate date;

    @JsonProperty("category")
    private String category;

    @JsonProperty("totalVotes")
    private Long totalVotes;

    @JsonProperty("totalVoters")
    private Long totalVoters;

    @JsonProperty("topDish")
    private DishVoteDTO topDish;

    @JsonProperty("allDishes")
    private List<DishVoteDTO> allDishes;

    @JsonProperty("categoryName")
    private String categoryName;

    public StatisticsDTO() {
    }

    public StatisticsDTO(LocalDate date, String category, String categoryName, Long totalVotes, Long totalVoters,
            DishVoteDTO topDish, List<DishVoteDTO> allDishes) {
        this.date = date;
        this.category = category;
        this.categoryName = categoryName;
        this.totalVotes = totalVotes;
        this.totalVoters = totalVoters;
        this.topDish = topDish;
        this.allDishes = allDishes;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Long getTotalVotes() {
        return totalVotes;
    }

    public void setTotalVotes(Long totalVotes) {
        this.totalVotes = totalVotes;
    }

    public Long getTotalVoters() {
        return totalVoters;
    }

    public void setTotalVoters(Long totalVoters) {
        this.totalVoters = totalVoters;
    }

    public DishVoteDTO getTopDish() {
        return topDish;
    }

    public void setTopDish(DishVoteDTO topDish) {
        this.topDish = topDish;
    }

    public List<DishVoteDTO> getAllDishes() {
        return allDishes;
    }

    public void setAllDishes(List<DishVoteDTO> allDishes) {
        this.allDishes = allDishes;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}
