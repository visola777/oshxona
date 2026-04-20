package com.example.demo.bot;

import com.example.demo.Service.AdminService;
import com.example.demo.Service.BotUserService;
import com.example.demo.Service.MealDishService;
import com.example.demo.Service.StatisticsService;
import com.example.demo.Service.VotingService;
import com.example.demo.config.BotConfig;
import com.example.demo.entity.Dish;
import com.example.demo.entity.TelegramUser;
import com.example.demo.entity.VoteCategory;
import com.example.demo.Service.VotingService.VoteResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.ByteArrayInputStream;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class TelegramMealVoteBot extends TelegramLongPollingBot {
    private final BotConfig botConfig;
    private final BotMessages botMessages;
    private final BotUserService userService;
    private final MealDishService dishService;
    private final VotingService votingService;
    private final StatisticsService statisticsService;
    private final AdminService adminService;

    @Override
    public String getBotUsername() {
        return botConfig.getBotUsername();
    }

    @Override
    public String getBotToken() {
        return botConfig.getBotToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                handleTextMessage(update.getMessage());
            } else if (update.hasCallbackQuery()) {
                handleCallbackQuery(update);
            }
        } catch (Exception ex) {
            log.error("Failed to process update", ex);
        }
    }

    private void handleTextMessage(Message message) {
        User from = message.getFrom();
        if (from == null) {
            return;
        }

        TelegramUser user = userService.registerOrUpdate(from.getId(), from.getUserName(), from.getFirstName(), from.getLastName(), from.getLanguageCode());
        String text = message.getText().trim();
        switch (text.split(" ")[0].toLowerCase()) {
            case "/start" -> sendMainMenu(message.getChatId(), user);
            case "/help" -> sendText(message.getChatId(), botMessages.help(user.getLanguageCode()), user.getLanguageCode());
            case "/myvotes" -> sendText(message.getChatId(), statisticsService.renderPersonalHistory(user.getTelegramId(), 30, user.getLanguageCode()), user.getLanguageCode());
            case "/top" -> sendText(message.getChatId(), statisticsService.renderGlobalTop(10, user.getLanguageCode()), user.getLanguageCode());
            case "/admin" -> sendAdminMenu(message.getChatId(), user);
            case "/broadcast" -> handleBroadcast(message, user);
            case "/export" -> handleExport(message, user);
            case "/reset_today" -> handleResetToday(message, user);
            default -> sendText(message.getChatId(), "Use /start to open the menu or /help for instructions.", user.getLanguageCode());
        }
    }

    private void handleCallbackQuery(Update update) {
        var callback = update.getCallbackQuery();
        if (callback == null) {
            return;
        }

        User from = callback.getFrom();
        if (from == null) {
            return;
        }

        TelegramUser user = userService.registerOrUpdate(from.getId(), from.getUserName(), from.getFirstName(), from.getLastName(), from.getLanguageCode());
        String data = callback.getData();
        Long chatId = callback.getMessage().getChatId();

        if (data == null) {
            answerCallback(callback.getId(), "Invalid action.");
            return;
        }

        if (data.equals("MENU")) {
            sendMainMenu(chatId, user);
        } else if (data.startsWith("CATEGORY:")) {
            sendCategoryList(chatId, user, data.split(":", 2)[1]);
        } else if (data.startsWith("DISH:")) {
            sendDishDetail(chatId, user, parseId(data));
        } else if (data.startsWith("VOTE:")) {
            processVote(chatId, user, parseId(data));
        } else if (data.startsWith("CHANGE:")) {
            processChange(chatId, user, parseId(data));
        } else if (data.equals("MY_VOTES")) {
            sendText(chatId, statisticsService.renderPersonalHistory(user.getTelegramId(), 30, user.getLanguageCode()), user.getLanguageCode());
        } else if (data.equals("GLOBAL_TOP")) {
            sendText(chatId, statisticsService.renderGlobalTop(10, user.getLanguageCode()), user.getLanguageCode());
        } else if (data.startsWith("ADMIN:")) {
            sendAdminMenu(chatId, user);
        } else {
            sendText(chatId, "Unknown action. Use /start to return to the main menu.", user.getLanguageCode());
        }
        answerCallback(callback.getId(), "Done");
    }

    private void sendMainMenu(Long chatId, TelegramUser user) {
        String menu = botMessages.welcome(user.getFirstName() != null ? user.getFirstName() : "friend", user.getLanguageCode());
        sendText(chatId, menu, user.getLanguageCode());
        sendText(chatId, buildMenuText(user.getLanguageCode()), user.getLanguageCode());
    }

    private void sendCategoryList(Long chatId, TelegramUser user, String categoryKey) {
        VoteCategory category = VoteCategory.fromName(categoryKey);
        if (category == null) {
            sendText(chatId, "Category not found.", user.getLanguageCode());
            return;
        }

        List<Dish> dishes = dishService.getActiveDishesByCategory(category.name());
        if (dishes.isEmpty()) {
            sendText(chatId, "No dishes available for " + category.label(user.getLanguageCode()) + ".", user.getLanguageCode());
            return;
        }

        StringBuilder builder = new StringBuilder("🍽️ " + category.label(user.getLanguageCode()) + "\n\n");
        for (Dish dish : dishes) {
            builder.append(dish.getName()).append(" — ").append(dish.getTotalVotes()).append(" votes\n");
        }
        builder.append("\nChoose a dish to see details and vote.");
        sendText(chatId, builder.toString(), user.getLanguageCode());
    }

    private void sendDishDetail(Long chatId, TelegramUser user, Long dishId) {
        Dish dish = dishService.getDish(dishId);
        if (dish == null) {
            sendText(chatId, "Dish not found.", user.getLanguageCode());
            return;
        }

        String caption = "🍽️ " + dish.getName() + "\n" +
                (dish.getDescription() != null ? dish.getDescription() + "\n" : "") +
                "\nCurrent votes: " + dish.getTotalVotes() + "\n" +
                "\nDo you want to vote for this dish today?";
        if (dish.getPhotoUrl() != null && !dish.getPhotoUrl().isBlank()) {
            sendPhoto(chatId, dish.getPhotoUrl(), caption);
        } else {
            sendText(chatId, caption, user.getLanguageCode());
        }
    }

    private void processVote(Long chatId, TelegramUser user, Long dishId) {
        Dish dish = dishService.getDish(dishId);
        if (dish == null) {
            sendText(chatId, "Dish not found.", user.getLanguageCode());
            return;
        }

        VoteResult result = votingService.voteForDish(user.getTelegramId(), dish);
        if (result.isSuccess()) {
            sendText(chatId, botMessages.voteSuccess(new BotMessages.DishInfo(dish.getName(), VoteCategory.fromName(dish.getCategory()).label(user.getLanguageCode())), user.getLanguageCode()), user.getLanguageCode());
            sendText(chatId, statisticsService.renderDailySummary(user.getLanguageCode()), user.getLanguageCode());
        } else if (result.isAlreadyVoted()) {
            if (isChangeAllowed()) {
                sendText(chatId, result.getMessage() + " You may change until " + botConfig.getChangeDeadline() + ".", user.getLanguageCode());
                sendText(chatId, "Press change if you want to switch to this dish.", user.getLanguageCode());
            } else {
                sendText(chatId, result.getMessage(), user.getLanguageCode());
            }
        } else {
            sendText(chatId, result.getMessage(), user.getLanguageCode());
        }
    }

    private void processChange(Long chatId, TelegramUser user, Long dishId) {
        if (!isChangeAllowed()) {
            sendText(chatId, "Vote changes are allowed only until " + botConfig.getChangeDeadline() + ".", user.getLanguageCode());
            return;
        }
        Dish dish = dishService.getDish(dishId);
        if (dish == null) {
            sendText(chatId, "Dish not found.", user.getLanguageCode());
            return;
        }

        VoteResult result = votingService.changeVote(user.getTelegramId(), dish);
        sendText(chatId, result.getMessage(), user.getLanguageCode());
    }

    private void sendAdminMenu(Long chatId, TelegramUser user) {
        if (!userService.isAdmin(user.getTelegramId())) {
            sendText(chatId, "You are not authorized to use admin commands.", user.getLanguageCode());
            return;
        }
        String message = "🔧 Admin panel:\n" +
                "/export - Export all votes as CSV\n" +
                "/reset_today - Reset today\'s votes\n" +
                "/broadcast <message> - Broadcast text to all users\n" +
                "/admin - Refresh this panel";
        sendText(chatId, message, user.getLanguageCode());
    }

    private void handleBroadcast(Message message, TelegramUser user) {
        if (!userService.isAdmin(user.getTelegramId())) {
            sendText(message.getChatId(), "Unauthorized.", user.getLanguageCode());
            return;
        }
        String payload = message.getText().replaceFirst("/broadcast", "").trim();
        if (payload.isBlank()) {
            sendText(message.getChatId(), "Usage: /broadcast your message here", user.getLanguageCode());
            return;
        }
        List<TelegramUser> users = userService.getAllUsers();
        users.forEach(target -> sendText(target.getTelegramId(), "📢 Broadcast:\n" + payload, target.getLanguageCode()));
        sendText(message.getChatId(), "Broadcast sent to " + users.size() + " users.", user.getLanguageCode());
    }

    private void handleExport(Message message, TelegramUser user) {
        if (!userService.isAdmin(user.getTelegramId())) {
            sendText(message.getChatId(), "Unauthorized.", user.getLanguageCode());
            return;
        }
        byte[] data = adminService.exportVotesCsv();
        SendDocument document = new SendDocument();
        document.setChatId(message.getChatId().toString());
        document.setDocument(new InputFile(new ByteArrayInputStream(data), "votes.csv"));
        document.setCaption("Vote export file");
        try {
            execute(document);
        } catch (TelegramApiException e) {
            log.error("Failed to send export", e);
            sendText(message.getChatId(), "Failed to export votes.", user.getLanguageCode());
        }
    }

    private void handleResetToday(Message message, TelegramUser user) {
        if (!userService.isAdmin(user.getTelegramId())) {
            sendText(message.getChatId(), "Unauthorized.", user.getLanguageCode());
            return;
        }
        adminService.resetTodayVotes();
        sendText(message.getChatId(), "Today's votes were reset.", user.getLanguageCode());
    }

    public void sendDailyReminder() {
        List<TelegramUser> users = userService.getAllUsers();
        for (TelegramUser user : users) {
            if (!votingService.hasVotedToday(user.getTelegramId(), VoteCategory.BREAKFAST)
                    || !votingService.hasVotedToday(user.getTelegramId(), VoteCategory.LUNCH)
                    || !votingService.hasVotedToday(user.getTelegramId(), VoteCategory.SNACK)) {
                sendText(user.getTelegramId(), "Reminder: Please vote for today's meals before 11:00.", user.getLanguageCode());
            }
        }
    }

    private boolean isChangeAllowed() {
        LocalTime deadline = LocalTime.parse(botConfig.getChangeDeadline());
        return LocalTime.now().isBefore(deadline);
    }

    private void sendText(Long chatId, String text, String languageCode) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        message.enableMarkdown(true);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Failed to send message", e);
        }
    }

    private void sendPhoto(Long chatId, String url, String caption) {
        SendPhoto photo = new SendPhoto();
        photo.setChatId(chatId.toString());
        photo.setPhoto(new InputFile(url));
        photo.setCaption(caption);
        try {
            execute(photo);
        } catch (TelegramApiException e) {
            log.error("Failed to send photo", e);
            sendText(chatId, caption, null);
        }
    }

    private void answerCallback(String callbackId, String text) {
        AnswerCallbackQuery answer = new AnswerCallbackQuery();
        answer.setCallbackQueryId(callbackId);
        answer.setText(text);
        answer.setShowAlert(false);
        try {
            execute(answer);
        } catch (TelegramApiException e) {
            log.error("Failed to answer callback", e);
        }
    }

    private Long parseId(String callbackData) {
        String[] parts = callbackData.split(":");
        if (parts.length < 2) {
            return null;
        }
        try {
            return Long.parseLong(parts[1]);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String buildMenuText(String languageCode) {
        String breakfast = VoteCategory.BREAKFAST.label(languageCode);
        String lunch = VoteCategory.LUNCH.label(languageCode);
        String snack = VoteCategory.SNACK.label(languageCode);
        return "Main menu:\n" +
                "1. " + breakfast + " (/category " + VoteCategory.BREAKFAST.name() + ")\n" +
                "2. " + lunch + " (/category " + VoteCategory.LUNCH.name() + ")\n" +
                "3. " + snack + " (/category " + VoteCategory.SNACK.name() + ")\n" +
                "4. /myvotes - My votes\n" +
                "5. /top - Top dishes\n" +
                "6. /help - Help and rules";
    }
}
