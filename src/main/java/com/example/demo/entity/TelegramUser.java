package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "telegram_users", uniqueConstraints = @UniqueConstraint(columnNames = { "telegramId" }))
public class TelegramUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long telegramId;

    private String username;
    private String firstName;
    private String lastName;
    private String languageCode;

    @Column(nullable = false)
    private LocalDateTime joinedAt = LocalDateTime.now();

    private LocalDateTime lastSeenAt = LocalDateTime.now();

    @Column(nullable = false)
    private boolean admin = false;

    private boolean blocked = false;   // default not blocked

    // ... getters and setters ...

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }
    public TelegramUser() {
    }

    public TelegramUser(Long id, Long telegramId, String username, String firstName, String lastName,
            String languageCode, LocalDateTime joinedAt, LocalDateTime lastSeenAt, boolean admin) {
        this.id = id;
        this.telegramId = telegramId;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.languageCode = languageCode;
        this.joinedAt = joinedAt;
        this.lastSeenAt = lastSeenAt;
        this.admin = admin;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTelegramId() {
        return telegramId;
    }

    public void setTelegramId(Long telegramId) {
        this.telegramId = telegramId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }

    public LocalDateTime getLastSeenAt() {
        return lastSeenAt;
    }

    public void setLastSeenAt(LocalDateTime lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }
}
