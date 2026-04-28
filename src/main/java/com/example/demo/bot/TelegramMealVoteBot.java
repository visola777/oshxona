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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.ByteArrayInputStream;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class TelegramMealVoteBot extends TelegramLongPollingBot {
    private static final Logger log = LoggerFactory.getLogger(TelegramMealVoteBot.class);
    private final BotConfig botConfig;
    private final BotMessages botMessages;
    private final BotUserService userService;
    private final MealDishService dishService;
    private final VotingService votingService;
    private final StatisticsService statisticsService;
    private final AdminService adminService;

    public TelegramMealVoteBot(BotConfig botConfig, BotMessages botMessages, BotUserService userService,
            MealDishService dishService, VotingService votingService,
            StatisticsService statisticsService, AdminService adminService) {
        this.botConfig = botConfig;
        this.botMessages = botMessages;
        this.userService = userService;
        this.dishService = dishService;
        this.votingService = votingService;
        this.statisticsService = statisticsService;
        this.adminService = adminService;
    }

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
        log.info("📩 Update received: {}", update.hasMessage() ? update.getMessage().getFrom().getId() : "callback");
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

    // ----------------------------------------------------------------------
    // Text commands
    // ----------------------------------------------------------------------
    private void handleTextMessage(Message message) {
        User from = message.getFrom();
        if (from == null)
            return;

        TelegramUser user = userService.registerOrUpdate(
                from.getId(), from.getUserName(), from.getFirstName(),
                from.getLastName(), from.getLanguageCode());

        String text = message.getText().trim().toLowerCase();
        Long chatId = message.getChatId();

        switch (text.split(" ")[0]) {
            case "/start" -> sendMainMenu(chatId, user);
            case "/help" -> sendText(chatId, botMessages.help(user.getLanguageCode()), user.getLanguageCode());
            case "/myvotes" -> sendText(chatId,
                    statisticsService.renderPersonalHistory(user.getTelegramId(), 30, user.getLanguageCode()),
                    user.getLanguageCode());
            case "/top" -> sendText(chatId,
                    statisticsService.renderGlobalTop(10, user.getLanguageCode()),
                    user.getLanguageCode());
            case "/admin" -> sendAdminMenu(chatId, user);
            case "/broadcast" -> handleBroadcast(message, user);
            case "/export" -> handleExport(message, user);
            case "/reset_today" -> handleResetToday(message, user);
            default -> sendText(chatId, "Use /start to open the menu", user.getLanguageCode());
        }
    }

    // ----------------------------------------------------------------------
    // Callback handling (all UI interactions)
    // ----------------------------------------------------------------------
    private void handleCallbackQuery(Update update) {
        var callback = update.getCallbackQuery();
        if (callback == null)
            return;

        User from = callback.getFrom();
        if (from == null)
            return;

        TelegramUser user = userService.registerOrUpdate(
                from.getId(), from.getUserName(), from.getFirstName(),
                from.getLastName(), from.getLanguageCode());

        String data = callback.getData();
        Long chatId = callback.getMessage().getChatId();
        String callbackId = callback.getId();

        if (data == null) {
            answerCallback(callbackId, "Invalid action");
            return;
        }

        log.info("Callback data: {}", data);

        // Main menu categories
        if (data.startsWith("CATEGORY:")) {
            String categoryKey = data.split(":", 2)[1];
            handleCategorySelection(chatId, user, categoryKey);
        }
        // Back to category list
        else if (data.startsWith("BACK_TO_CATEGORY:")) {
            String categoryKey = data.split(":", 2)[1];
            handleCategorySelection(chatId, user, categoryKey);
        }
        // Show dish details
        else if (data.startsWith("DISH:")) {
            Long dishId = parseId(data);
            if (dishId != null)
                sendDishDetail(chatId, user, dishId);
        }
        // Confirm vote
        else if (data.startsWith("CONFIRM_VOTE:")) {
            Long dishId = parseId(data);
            if (dishId != null)
                handleConfirmVote(chatId, user, dishId);
        }
        // Show statistics (today's top)
        else if (data.equals("SHOW_STATS")) {
            sendTodayStats(chatId, user);
        }
        // Done / exit
        else if (data.equals("DONE")) {
            sendText(chatId, "✅ Siz asosiy menyudasiz. /start bosing.", user.getLanguageCode());
        }
        // Back to main menu
        else if (data.equals("MENU")) {
            sendMainMenu(chatId, user);
        }
        // Admin fallback
        else if (data.startsWith("ADMIN:")) {
            sendAdminMenu(chatId, user);
        } else {
            sendText(chatId, "Noma'lum buyruq. /start bosing.", user.getLanguageCode());
        }

        answerCallback(callbackId, "✅");
    }

    // ----------------------------------------------------------------------
    // Main menu with 3 inline buttons
    // ----------------------------------------------------------------------
    private void sendMainMenu(Long chatId, TelegramUser user) {
        String welcome = botMessages.welcome(
                user.getFirstName() != null ? user.getFirstName() : "friend",
                user.getLanguageCode());
        sendText(chatId, welcome, user.getLanguageCode());

        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("🍽️ *Ovqat turini tanlang:*");
        message.setParseMode("Markdown");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        // Breakfast button
        InlineKeyboardButton breakfast = new InlineKeyboardButton();
        breakfast.setText(VoteCategory.BREAKFAST.label(user.getLanguageCode()));
        breakfast.setCallbackData("CATEGORY:" + VoteCategory.BREAKFAST.name());

        // Lunch button
        InlineKeyboardButton lunch = new InlineKeyboardButton();
        lunch.setText(VoteCategory.LUNCH.label(user.getLanguageCode()));
        lunch.setCallbackData("CATEGORY:" + VoteCategory.LUNCH.name());

        // Snack button
        InlineKeyboardButton snack = new InlineKeyboardButton();
        snack.setText(VoteCategory.SNACK.label(user.getLanguageCode()));
        snack.setCallbackData("CATEGORY:" + VoteCategory.SNACK.name());

        rows.add(List.of(breakfast, lunch, snack));
        markup.setKeyboard(rows);
        message.setReplyMarkup(markup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Failed to send main menu", e);
        }
    }

    // ----------------------------------------------------------------------
    // Category selection with sequential unlock check
    // ----------------------------------------------------------------------
    private void handleCategorySelection(Long chatId, TelegramUser user, String categoryKey) {
        VoteCategory category = VoteCategory.fromName(categoryKey);
        if (category == null) {
            sendText(chatId, "Kategoriya topilmadi.", user.getLanguageCode());
            return;
        }

        // Sequential unlock validation
        if (!isCategoryAccessible(user, category)) {
            String errorMsg = switch (category) {
                case LUNCH -> "❌ Avval *Tonggi ovqat* uchun ovoz berishingiz kerak!";
                case SNACK -> "❌ Avval *Tushlik* uchun ovoz berishingiz kerak!";
                default -> "";
            };
            sendText(chatId, errorMsg, user.getLanguageCode());
            return;
        }

        // Voting time check
        if (!isVotingAllowed()) {
            sendText(chatId, "⚠️ Ovoz berish vaqti tugadi (13:00 dan keyin).", user.getLanguageCode());
            return;
        }

        List<Dish> dishes = dishService.getActiveDishesByCategory(category.name());
        if (dishes.isEmpty()) {
            sendText(chatId, "Bu kategoriya uchun hech qanday taom mavjud emas.", user.getLanguageCode());
            return;
        }

        // Send list of dishes as inline buttons
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("🍽️ *" + category.label(user.getLanguageCode()) + "*\nQuyidagi taomlardan birini tanlang:");
        message.setParseMode("Markdown");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (Dish dish : dishes) {
            InlineKeyboardButton btn = new InlineKeyboardButton();
            btn.setText(dish.getName() + " (" + dish.getTotalVotes() + " ovoz)");
            btn.setCallbackData("DISH:" + dish.getId());
            rows.add(List.of(btn));
        }

        // Back to main menu button
        InlineKeyboardButton back = new InlineKeyboardButton();
        back.setText("🔙 Asosiy menyu");
        back.setCallbackData("MENU");
        rows.add(List.of(back));

        markup.setKeyboard(rows);
        message.setReplyMarkup(markup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Failed to send category list", e);
        }
    }

    // ----------------------------------------------------------------------
    // Dish detail with photo + Back & Confirm buttons
    // ----------------------------------------------------------------------
    private void sendDishDetail(Long chatId, TelegramUser user, Long dishId) {
        Dish dish = dishService.getDish(dishId);
        if (dish == null) {
            sendText(chatId, "Taom topilmadi.", user.getLanguageCode());
            return;
        }

        // Build caption without markdown special characters problem
        String captionRaw = "🍽️ " + dish.getName() + "\n\n" +
                (dish.getDescription() != null ? dish.getDescription() + "\n\n" : "") +
                "📊 Hozirgi ovozlar: " + dish.getTotalVotes() + "\n\n" +
                "✅ Ushbu taomga ovoz berishni tasdiqlaysizmi?";

        // Create inline buttons (always present)
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        InlineKeyboardButton back = new InlineKeyboardButton();
        back.setText("⬅️ Orqaga");
        back.setCallbackData("BACK_TO_CATEGORY:" + dish.getCategory());

        InlineKeyboardButton confirm = new InlineKeyboardButton();
        confirm.setText("✅ Tasdiqlash");
        confirm.setCallbackData("CONFIRM_VOTE:" + dish.getId());

        rows.add(List.of(back, confirm));
        markup.setKeyboard(rows);

        // Try to send photo + caption (if URL exists)
        boolean sent = false;
        if (dish.getPhotoUrl() != null && !dish.getPhotoUrl().isBlank()) {
            SendPhoto photo = new SendPhoto();
            photo.setChatId(chatId.toString());
            photo.setPhoto(new InputFile(dish.getPhotoUrl()));
            photo.setCaption(captionRaw);
            photo.setReplyMarkup(markup);
            // Don't use markdown for photo caption – it's risky. Send as plain text.
            try {
                execute(photo);
                sent = true;
            } catch (TelegramApiException e) {
                log.warn("Photo failed for dish {}: {}", dish.getName(), e.getMessage());
                // fall through to text-only
            }
        }

        if (!sent) {
            // Send text message with buttons – try Markdown first, then plain text
            SendMessage msg = new SendMessage();
            msg.setChatId(chatId.toString());
            msg.setText(captionRaw);
            msg.setReplyMarkup(markup);

            // Try Markdown
            msg.setParseMode("Markdown");
            try {
                execute(msg);
            } catch (TelegramApiException e) {
                // Markdown failed – send without formatting
                log.warn("Markdown failed, sending plain text: {}", e.getMessage());
                msg.setParseMode(null);
                try {
                    execute(msg);
                } catch (TelegramApiException ex) {
                    log.error("Failed to send dish detail even as plain text", ex);
                }
            }
        }
    }

    // ----------------------------------------------------------------------
    // Confirm vote -> process and show success with Stats/Done buttons
    // ----------------------------------------------------------------------
    private void handleConfirmVote(Long chatId, TelegramUser user, Long dishId) {
        if (!isVotingAllowed()) {
            sendText(chatId, "⚠️ Ovoz berish vaqti tugadi (13:00 dan keyin).", user.getLanguageCode());
            return;
        }

        Dish dish = dishService.getDish(dishId);
        if (dish == null) {
            sendText(chatId, "Taom topilmadi.", user.getLanguageCode());
            return;
        }

        VoteResult result = votingService.voteForDish(user.getTelegramId(), dish);
        if (!result.isSuccess()) {
            sendText(chatId, result.getMessage(), user.getLanguageCode());
            return;
        }

        // Success message with two inline buttons
        String successText = "✅ " + botMessages.voteSuccess(
                new BotMessages.DishInfo(dish.getName(),
                        VoteCategory.fromName(dish.getCategory()).label(user.getLanguageCode())),
                user.getLanguageCode());

        SendMessage msg = new SendMessage();
        msg.setChatId(chatId.toString());
        msg.setText(successText);
        msg.setParseMode("Markdown");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        InlineKeyboardButton statsBtn = new InlineKeyboardButton();
        statsBtn.setText("📊 Statistika (Bugungi TOP)");
        statsBtn.setCallbackData("SHOW_STATS");

        InlineKeyboardButton doneBtn = new InlineKeyboardButton();
        doneBtn.setText("✅ Tugatish");
        doneBtn.setCallbackData("DONE");

        rows.add(List.of(statsBtn, doneBtn));
        markup.setKeyboard(rows);
        msg.setReplyMarkup(markup);

        try {
            execute(msg);
        } catch (TelegramApiException e) {
            log.error("Failed to send vote confirmation", e);
        }
    }

    // ----------------------------------------------------------------------
    // Today's top dish statistics (percentage + vote count)
    // ----------------------------------------------------------------------
    private void sendTodayStats(Long chatId, TelegramUser user) {
        String stats = statisticsService.renderDailyTopWithPercentage(user.getLanguageCode());
        sendText(chatId, stats, user.getLanguageCode());
    }

    // ----------------------------------------------------------------------
    // Helper: check if category is accessible (sequential unlock)
    // ----------------------------------------------------------------------
    private boolean isCategoryAccessible(TelegramUser user, VoteCategory category) {
        return switch (category) {
            case BREAKFAST -> true;
            case LUNCH -> votingService.hasVotedToday(user.getTelegramId(), VoteCategory.BREAKFAST);
            case SNACK -> votingService.hasVotedToday(user.getTelegramId(), VoteCategory.LUNCH);
        };
    }

    private boolean isVotingAllowed() {
        return LocalTime.now().isBefore(LocalTime.parse("15:00"));
    }

    // ----------------------------------------------------------------------
    // Admin commands (unchanged)
    // ----------------------------------------------------------------------
    private void sendAdminMenu(Long chatId, TelegramUser user) {
        if (!userService.isAdmin(user.getTelegramId())) {
            sendText(chatId, "Siz admin emassiz.", user.getLanguageCode());
            return;
        }
        String message = """
                🔧 Admin panel:
                /export - Eksport CSV
                /reset_today - Bugungi ovozlarni tozalash
                /broadcast <matn> - Xabar yuborish
                /admin - Menyuni yangilash
                """;
        sendText(chatId, message, user.getLanguageCode());
    }

    private void handleBroadcast(Message message, TelegramUser user) {
        if (!userService.isAdmin(user.getTelegramId()))
            return;
        String payload = message.getText().replaceFirst("/broadcast", "").trim();
        if (payload.isBlank()) {
            sendText(message.getChatId(), "Ishlatish: /broadcast xabar matni", user.getLanguageCode());
            return;
        }
        userService.getAllUsers()
                .forEach(target -> sendText(target.getTelegramId(), "📢 E'lon:\n" + payload, target.getLanguageCode()));
        sendText(message.getChatId(), "Xabar " + userService.getAllUsers().size() + " foydalanuvchiga yuborildi.",
                user.getLanguageCode());
    }

    private void handleExport(Message message, TelegramUser user) {
        if (!userService.isAdmin(user.getTelegramId()))
            return;
        byte[] data = adminService.exportVotesCsv();
        SendDocument doc = new SendDocument();
        doc.setChatId(message.getChatId().toString());      
        doc.setDocument(new InputFile(new ByteArrayInputStream(data), "votes.csv"));
        doc.setCaption("Ovozlar eksporti");
        try {
            execute(doc);
        } catch (TelegramApiException e) {
            log.error("Export failed", e);
            sendText(message.getChatId(), "Eksport qilmadi.", user.getLanguageCode());
        }
    }

    private void handleResetToday(Message message, TelegramUser user) {
        if (!userService.isAdmin(user.getTelegramId()))
            return;
        adminService.resetTodayVotes();
        sendText(message.getChatId(), "Bugungi ovozlar tozalandi.", user.getLanguageCode());
    }

    // ----------------------------------------------------------------------
    // Utility send methods
    // ----------------------------------------------------------------------
    private void sendText(Long chatId, String text, String lang) {
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId.toString());
        msg.setText(text);
        msg.enableMarkdown(true);
        try {
            execute(msg);
        } catch (TelegramApiException e) {
            log.error("Send text failed", e);
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
            log.error("Answer callback failed", e);
        }
    }

    private Long parseId(String data) {
        String[] parts = data.split(":");
        if (parts.length < 2)
            return null;
        try {
            return Long.parseLong(parts[1]);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public void sendDailyReminder() {
        List<TelegramUser> users = userService.getAllUsers();
        for (TelegramUser user : users) {
            boolean hasVotedAll = true;
            for (VoteCategory category : VoteCategory.values()) {
                if (!votingService.hasVotedToday(user.getTelegramId(), category)) {
                    hasVotedAll = false;
                    break;
                }
            }
            if (!hasVotedAll) {
                sendText(user.getTelegramId(),
                        "🌅 Eslatma: Iltimos, bugungi ovozlaringizni 13:00 gacha bering!\n" +
                                "Tonggi ovqat → Tushlik → Poldnik",
                        user.getLanguageCode());
            }
        }
    }
}