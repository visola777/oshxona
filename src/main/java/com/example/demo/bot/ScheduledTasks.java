package com.example.demo.bot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduledTasks {
    private final TelegramMealVoteBot telegramMealVoteBot;

    @Scheduled(cron = "${bot.reminder-cron}")
    public void sendDailyReminder() {
        log.info("Running daily reminder task");
        telegramMealVoteBot.sendDailyReminder();
    }
}
