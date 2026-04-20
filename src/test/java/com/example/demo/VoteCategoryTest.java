package com.example.demo;

import com.example.demo.entity.VoteCategory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("VoteCategory enum testlar")
class VoteCategoryTest {

    @Test
    @DisplayName("BREAKFAST — ingliz tilida 'Breakfast' qaytarishi kerak")
    void breakfast_englishLabel() {
        assertThat(VoteCategory.BREAKFAST.label("en")).isEqualTo("Breakfast");
    }

    @Test
    @DisplayName("BREAKFAST — o'zbek tilida 'Nonushta' qaytarishi kerak")
    void breakfast_uzbekLabel() {
        assertThat(VoteCategory.BREAKFAST.label("uz")).isEqualTo("Nonushta");
    }

    @Test
    @DisplayName("LUNCH — ingliz tilida 'Lunch' qaytarishi kerak")
    void lunch_englishLabel() {
        assertThat(VoteCategory.LUNCH.label("en")).isEqualTo("Lunch");
    }

    @Test
    @DisplayName("LUNCH — o'zbek tilida 'Obed' qaytarishi kerak")
    void lunch_uzbekLabel() {
        assertThat(VoteCategory.LUNCH.label("uz")).isEqualTo("Obed");
    }

    @Test
    @DisplayName("SNACK — ingliz tilida 'Afternoon snack' qaytarishi kerak")
    void snack_englishLabel() {
        assertThat(VoteCategory.SNACK.label("en")).isEqualTo("Afternoon snack");
    }

    @Test
    @DisplayName("SNACK — o'zbek tilida 'Poldnik' qaytarishi kerak")
    void snack_uzbekLabel() {
        assertThat(VoteCategory.SNACK.label("uz")).isEqualTo("Poldnik");
    }

    @Test
    @DisplayName("Til kodi null — ingliz tili ishlatilishi kerak")
    void nullLanguageCode_shouldReturnEnglish() {
        assertThat(VoteCategory.BREAKFAST.label(null)).isEqualTo("Breakfast");
        assertThat(VoteCategory.LUNCH.label(null)).isEqualTo("Lunch");
        assertThat(VoteCategory.SNACK.label(null)).isEqualTo("Afternoon snack");
    }

    @ParameterizedTest
    @DisplayName("fromName — turli yozuvlar bilan to'g'ri kategoriya qaytarishi kerak")
    @CsvSource({
            "BREAKFAST, BREAKFAST",
            "breakfast, BREAKFAST",
            "Breakfast, BREAKFAST",
            "LUNCH, LUNCH",
            "lunch, LUNCH",
            "Lunch, LUNCH",
            "SNACK, SNACK",
            "snack, SNACK",
            "Nonushta, BREAKFAST",
            "Obed, LUNCH",
            "Poldnik, SNACK"
    })
    void fromName_variousInputs_shouldReturnCorrectCategory(String input, String expected) {
        VoteCategory result = VoteCategory.fromName(input);
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo(expected);
    }

    @Test
    @DisplayName("fromName — noto'g'ri nom — null qaytarishi kerak")
    void fromName_invalidName_shouldReturnNull() {
        assertThat(VoteCategory.fromName("INVALID")).isNull();
        assertThat(VoteCategory.fromName("")).isNull();
        assertThat(VoteCategory.fromName("dinner")).isNull();
    }

    @Test
    @DisplayName("Barcha 3 ta kategoriya mavjud bo'lishi kerak")
    void allThreeCategoriesShouldExist() {
        VoteCategory[] values = VoteCategory.values();
        assertThat(values).hasSize(3);
        assertThat(values).containsExactlyInAnyOrder(
                VoteCategory.BREAKFAST,
                VoteCategory.LUNCH,
                VoteCategory.SNACK
        );
    }
}
