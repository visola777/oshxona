package com.example.demo.entity;



import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "votes")
@Data
@NoArgsConstructor
public class Vote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String foodName;

    private LocalDateTime votedAt = LocalDateTime.now();

    public Vote(Long userId, String foodName) {
        this.userId = userId;
        this.foodName = foodName;
    }
}

