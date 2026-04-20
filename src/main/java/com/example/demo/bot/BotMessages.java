package com.example.demo.bot;

import com.example.demo.entity.VoteCategory;
import org.springframework.stereotype.Component;

@Component
public class BotMessages {

    public String welcome(String firstName, String languageCode) {
        if (languageCode != null && languageCode.startsWith("uz")) {
            return "Assalomu alaykum, " + firstName + "!\n\n" +
                    "🍽️ *Daily Meal Voting Bot* ga xush kelibsiz. Siz har kuni nonushta, obed yoki poldnik uchun eng yaxshi taomni tanlaysiz.\n\n" +
                    "⏳ Ovozingizni bugun kiritishni unutmang!";
        }
        return "Hello, " + firstName + "!\n\n" +
                "Welcome to the *Daily Meal Voting Bot*. Vote every day for breakfast, lunch or afternoon snack and see the leaderboards in real time.\n\n" +
                "⏳ Don’t forget to vote today!";
    }

    public String help(String languageCode) {
        if (languageCode != null && languageCode.startsWith("uz")) {
            return "ℹ️ *Qoidalar va yordam*\n\n" +
                    "• Har bir kishi har kuni har bir kategoriya uchun bitta ovoz berishi mumkin.\n" +
                    "• Ovozlar 00:00 da yangilanadi va yangi kun boshlanadi.\n" +
                    "• 11:00 gacha ovozingizni o'zgartirishingiz mumkin.\n" +
                    "• " + VoteCategory.BREAKFAST.label(languageCode) + ", " + VoteCategory.LUNCH.label(languageCode) + " va " + VoteCategory.SNACK.label(languageCode) + " mustaqil.";
        }
        return "ℹ️ *Help & Rules*\n\n" +
                "• Each user can vote once per day in every category.\n" +
                "• Daily counts reset automatically at 00:00.\n" +
                "• You can change your vote until 11:00 AM.\n" +
                "• Breakfast, lunch and afternoon snack are independent categories.";
    }

    public String alreadyVoted(String dishName, String languageCode) {
        if (languageCode != null && languageCode.startsWith("uz")) {
            return "Siz bugun allaqachon " + dishName + " uchun ovoz bergansiz.";
        }
        return "You have already voted today for " + dishName + ".";
    }

    public String voteSuccess(DishInfo dishInfo, String languageCode) {
        if (languageCode != null && languageCode.startsWith("uz")) {
            return "✅ Sizning ovozingiz muvaffaqiyatli saqlandi!\n\n" +
                    "Siz tanladingiz: " + dishInfo.name + "\n" +
                    "Kategoriya: " + dishInfo.categoryLabel;
        }
        return "✅ Your vote has been recorded!\n\n" +
                "You selected: " + dishInfo.name + "\n" +
                "Category: " + dishInfo.categoryLabel;
    }

    public static class DishInfo {
        public final String name;
        public final String categoryLabel;

        public DishInfo(String name, String categoryLabel) {
            this.name = name;
            this.categoryLabel = categoryLabel;
        }
    }
}
