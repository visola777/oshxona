package com.example.demo;

import com.example.demo.bot.TelegramMealVoteBot;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@SpringBootApplication
public class Demo2Application {

    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(Demo2Application.class, args);

        // Qo'shimcha: Botni qo'lda ro'yxatdan o'tkazish
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(ctx.getBean(TelegramMealVoteBot.class));
            System.out.println("✅ Telegram bot successfully registered with Long Polling!");
        } catch (TelegramApiException e) {
            e.printStackTrace();
            System.err.println("❌ Failed to register bot: " + e.getMessage());
        }
    }
}