package com.example.demo;

import com.example.demo.bot.BotMessages;
import com.example.demo.entity.VoteCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("BotMessages — ko'p tilli xabarlar testlar")
class BotMessagesTest {

    private BotMessages botMessages;

    @BeforeEach
    void setUp() {
        botMessages = new BotMessages();
    }

    // =========================================================
    // welcome()
    // =========================================================

    @Test
    @DisplayName("welcome — o'zbek tilida ism va kalit so'z bo'lishi kerak")
    void welcome_uzbek_shouldContainNameAndKeywords() {
        String msg = botMessages.welcome("Sardor", "uz");

        assertThat(msg).contains("Sardor");
        assertThat(msg).contains("Assalomu");
        assertThat(msg).contains("ovoz");
    }

    @Test
    @DisplayName("welcome — ingliz tilida ism va kalit so'z bo'lishi kerak")
    void welcome_english_shouldContainNameAndKeywords() {
        String msg = botMessages.welcome("Alice", "en");

        assertThat(msg).contains("Alice");
        assertThat(msg).contains("Hello");
        assertThat(msg).contains("Vote");
    }

    @Test
    @DisplayName("welcome — til kodi null — ingliz tili ishlatilishi kerak")
    void welcome_nullLanguage_shouldFallbackToEnglish() {
        String msg = botMessages.welcome("Bob", null);

        assertThat(msg).contains("Bob");
        assertThat(msg).contains("Hello");
    }

    // =========================================================
    // help()
    // =========================================================

    @Test
    @DisplayName("help — o'zbek tilida qoidalar bo'lishi kerak")
    void help_uzbek_shouldContainUzbekRules() {
        String msg = botMessages.help("uz");

        assertThat(msg).contains("Qoidalar");
        assertThat(msg).contains("Nonushta");
    }

    @Test
    @DisplayName("help — ingliz tilida qoidalar bo'lishi kerak")
    void help_english_shouldContainEnglishRules() {
        String msg = botMessages.help("en");

        assertThat(msg).contains("Rules");
        assertThat(msg).contains("Breakfast");
    }

    // =========================================================
    // alreadyVoted()
    // =========================================================

    @Test
    @DisplayName("alreadyVoted — taom nomini o'z ichiga olishi kerak (uzbek)")
    void alreadyVoted_uzbek_shouldContainDishName() {
        String msg = botMessages.alreadyVoted("Lag'mon", "uz");

        assertThat(msg).contains("Lag'mon");
        assertThat(msg).contains("ovoz");
    }

    @Test
    @DisplayName("alreadyVoted — taom nomini o'z ichiga olishi kerak (english)")
    void alreadyVoted_english_shouldContainDishName() {
        String msg = botMessages.alreadyVoted("Lag'mon", "en");

        assertThat(msg).contains("Lag'mon");
        assertThat(msg).containsIgnoringCase("voted");
    }

    // =========================================================
    // voteSuccess()
    // =========================================================

    @Test
    @DisplayName("voteSuccess — taom va kategoriya nomi bo'lishi kerak (uzbek)")
    void voteSuccess_uzbek_shouldContainDishAndCategory() {
        BotMessages.DishInfo info = new BotMessages.DishInfo("Shirguruch", "Nonushta");
        String msg = botMessages.voteSuccess(info, "uz");

        assertThat(msg).contains("Shirguruch");
        assertThat(msg).contains("Nonushta");
        assertThat(msg).contains("✅");
    }

    @Test
    @DisplayName("voteSuccess — taom va kategoriya nomi bo'lishi kerak (english)")
    void voteSuccess_english_shouldContainDishAndCategory() {
        BotMessages.DishInfo info = new BotMessages.DishInfo("Shirguruch", "Breakfast");
        String msg = botMessages.voteSuccess(info, "en");

        assertThat(msg).contains("Shirguruch");
        assertThat(msg).contains("Breakfast");
        assertThat(msg).contains("✅");
    }

    // =========================================================
    // DishInfo
    // =========================================================

    @Test
    @DisplayName("DishInfo — nom va kategoriya to'g'ri saqlanishi kerak")
    void dishInfo_shouldHoldCorrectData() {
        BotMessages.DishInfo info = new BotMessages.DishInfo("Mastava", "Obed");

        assertThat(info.name).isEqualTo("Mastava");
        assertThat(info.categoryLabel).isEqualTo("Obed");
    }
}
