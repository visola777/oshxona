//package com.example.demo.entity;
//
//import jakarta.persistence.*;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "votes")
//public class Vote {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(nullable = false)
//    private Long userId;
//
//    @Column(nullable = false)
//    private String foodName;
//
//    @Column(nullable = false)
//    private LocalDate voteDate;
//
//    @Column(nullable = false)
//    private String category;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "dish_id")
//    private Dish dish;
//
//    @ManyToOne
//    @JoinColumn(name = "user_id")
//    private TelegramUser user;
//
//    private LocalDateTime votedAt = LocalDateTime.now();
//
//    public Vote() {
//    }
//
//    public Vote(Long userId, String foodName) {
//        this.userId = userId;
//        this.foodName = foodName;
//        this.voteDate = LocalDate.now();
//    }
//
//    public Vote(Long id, Long userId, String foodName, LocalDate voteDate, String category, Dish dish,
//            LocalDateTime votedAt) {
//        this.id = id;
//        this.userId = userId;
//        this.foodName = foodName;
//        this.voteDate = voteDate;
//        this.category = category;
//        this.dish = dish;
//        this.votedAt = votedAt;
//    }
//
//    // Getters and Setters
//    public Long getId() {
//        return id;
//    }
//
//    public void setId(Long id) {
//        this.id = id;
//    }
//
//    public Long getUserId() {
//        return userId;
//    }
//
//    public void setUserId(Long userId) {
//        this.userId = userId;
//    }
//
//    public String getFoodName() {
//        return foodName;
//    }
//
//    public void setFoodName(String foodName) {
//        this.foodName = foodName;
//    }
//
//    public LocalDate getVoteDate() {
//        return voteDate;
//    }
//
//    public void setVoteDate(LocalDate voteDate) {
//        this.voteDate = voteDate;
//    }
//
//    public String getCategory() {
//        return category;
//    }
//
//    public void setCategory(String category) {
//        this.category = category;
//    }
//
//    public Dish getDish() {
//        return dish;
//    }
//
//    public void setDish(Dish dish) {
//        this.dish = dish;
//    }
//
//    public LocalDateTime getVotedAt() {
//        return votedAt;
//    }
//
//    public void setVotedAt(LocalDateTime votedAt) {
//        this.votedAt = votedAt;
//    }
//
//    public TelegramUser getUser() {
//        return user;
//    }
//
//    public void setUser(TelegramUser user) {
//        this.user = user;
//    }
//}
package com.example.demo.entity;


import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "Vote")
public class Vote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;                 // ← only this, no @ManyToOne

    @ManyToOne
    @JoinColumn(name = "dish_id", nullable = false)
    private Dish dish;

    @Column(nullable = false)
    private String category;

    @Column(name = "vote_date", nullable = false)
    private LocalDate voteDate;

    @Column(name = "voted_at")
    private LocalDateTime votedAt;

    // Optional: foodName if you need it (but you can derive from dish)
    @Column(name = "food_name")
    private String foodName;

    // Constructors
    public Vote() {}

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Dish getDish() { return dish; }
    public void setDish(Dish dish) {
        this.dish = dish;
        if (dish != null) {
            this.foodName = dish.getName();  // optional denormalization
        }
    }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public LocalDate getVoteDate() { return voteDate; }
    public void setVoteDate(LocalDate voteDate) { this.voteDate = voteDate; }

    public LocalDateTime getVotedAt() { return votedAt; }
    public void setVotedAt(LocalDateTime votedAt) { this.votedAt = votedAt; }

    public String getFoodName() { return foodName; }
    public void setFoodName(String foodName) { this.foodName = foodName; }
}
