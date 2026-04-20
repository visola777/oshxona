package com.example.demo.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTasks {
    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);
    private final TelegramMealVoteBot telegramMealVoteBot;

    public ScheduledTasks(TelegramMealVoteBot telegramMealVoteBot) {
        this.telegramMealVoteBot = telegramMealVoteBot;
    }

    @Scheduled(cron = "${bot.reminder-cron}")
    public void sendDailyReminder() {
        log.info("Running daily reminder task");
        telegramMealVoteBot.sendDailyReminder();
    }
}
