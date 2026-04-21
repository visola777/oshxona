package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO representing a dish with its vote count
 */
public class DishVoteDTO {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("category")
    private String category;

    @JsonProperty("photoUrl")
    private String photoUrl;

    @JsonProperty("votes")
    private Long voteCount;

    @JsonProperty("percentage")
    private Double votePercentage;

    public DishVoteDTO() {
    }

    public DishVoteDTO(Long id, String name, String description, String category, String photoUrl, Long voteCount,
            Double votePercentage) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.photoUrl = photoUrl;
        this.voteCount = voteCount;
        this.votePercentage = votePercentage;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public Long getVoteCount() {
        return voteCount;
    }

    public void setVoteCount(Long voteCount) {
        this.voteCount = voteCount;
    }

    public Double getVotePercentage() {
        return votePercentage;
    }

    public void setVotePercentage(Double votePercentage) {
        this.votePercentage = votePercentage;
    }
}
