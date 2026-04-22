package com.example.demo.entity;


import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "excluded_foods")
public class ExcludedFood {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "dish_id")
    private Dish dish;

    @Column(name = "excluded_date")
    private LocalDate excludedDate;

    // getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Dish getDish() { return dish; }
    public void setDish(Dish dish) { this.dish = dish; }
    public LocalDate getExcludedDate() { return excludedDate; }
    public void setExcludedDate(LocalDate excludedDate) { this.excludedDate = excludedDate; }
}