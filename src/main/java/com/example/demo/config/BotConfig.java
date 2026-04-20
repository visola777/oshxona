package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class BotConfig {
    @Value("${bot.token}")
    private String botToken;

    @Value("${bot.username}")
    private String botUsername;

    @Value("${bot.reminder-cron}")
    private String reminderCron;

    @Value("${bot.change-deadline}")
    private String changeDeadline;

    @Value("${bot.admin-ids:}")
    private String adminIds;

    @Value("${bot.default-language:en}")
    private String defaultLanguage;

    public String getBotToken() {
        return botToken;
    }

    public String getBotUsername() {
        return botUsername;
    }

    public String getReminderCron() {
        return reminderCron;
    }

    public String getChangeDeadline() {
        return changeDeadline;
    }

    public String getDefaultLanguage() {
        return defaultLanguage;
    }

    public boolean isAdmin(Long telegramId) {
        List<String> ids = Arrays.stream(adminIds.split(","))
                .filter(s -> !s.isBlank())
                .map(String::trim)
                .collect(Collectors.toList());
        return ids.contains(String.valueOf(telegramId));
    }
}
