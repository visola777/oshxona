package com.example.demo;

import com.example.demo.Service.StatisticsService;
import com.example.demo.Service.MealDishService;
import com.example.demo.Service.VotingService;
import com.example.demo.entity.Dish;
import com.example.demo.entity.Vote;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StatisticsService — statistika render testlar")
class StatisticsServiceTest {

    @Mock
    private VotingService votingService;

    @Mock
    private MealDishService dishService;

    @InjectMocks
    private StatisticsService statisticsService;

    private Dish dish1;
    private Dish dish2;

    @BeforeEach
    void setUp() {
        dish1 = new Dish();
        dish1.setId(1L);
        dish1.setName("Lag'mon");
        dish1.setCategory("LUNCH");
        dish1.setTotalVotes(10);
        dish1.setActive(true);

        dish2 = new Dish();
        dish2.setId(2L);
        dish2.setName("Mastava");
        dish2.setCategory("LUNCH");
        dish2.setTotalVotes(7);
        dish2.setActive(true);
    }

    @Test
    @DisplayName("renderGlobalTop — taomlar mavjud — to'g'ri matn qaytarishi kerak")
    void renderGlobalTop_withVotes_shouldReturnFormattedText() {
        when(votingService.getGlobalTopDishes()).thenReturn(List.of(
                new Object[] { dish1, 10L },
                new Object[] { dish2, 7L }));

        String result = statisticsService.renderGlobalTop(10, "en");

        assertThat(result).contains("Lag'mon");
        assertThat(result).contains("10");
        assertThat(result).contains("Mastava");
        assertThat(result).contains("7");
        assertThat(result).contains("🥇");
        assertThat(result).contains("🥈");
    }

    @Test
    @DisplayName("renderGlobalTop — ovozlar yo'q — bo'sh xabar qaytarishi kerak")
    void renderGlobalTop_noVotes_shouldReturnEmptyMessage() {
        when(votingService.getGlobalTopDishes()).thenReturn(Collections.emptyList());

        String result = statisticsService.renderGlobalTop(10, "en");

        assertThat(result).containsIgnoringCase("no votes");
    }

    @Test
    @DisplayName("renderGlobalTop — o'zbek tili — o'zbek xabar qaytarishi kerak")
    void renderGlobalTop_noVotesUzbek_shouldReturnUzbekMessage() {
        when(votingService.getGlobalTopDishes()).thenReturn(Collections.emptyList());

        String result = statisticsService.renderGlobalTop(10, "uz");

        assertThat(result).containsIgnoringCase("ovoz");
    }

    @Test
    @DisplayName("renderPersonalHistory — tarix mavjud — ovozlar ro'yxati ko'rsatilishi kerak")
    void renderPersonalHistory_withHistory_shouldReturnVoteList() {
        long userId = 100L;
        Vote vote = new Vote();
        vote.setUserId(userId);
        vote.setDish(dish1);
        vote.setVoteDate(LocalDate.now());
        vote.setCategory("LUNCH");

        when(votingService.getUserHistory(userId, 30)).thenReturn(List.of(vote));

        String result = statisticsService.renderPersonalHistory(userId, 30, "en");

        assertThat(result).contains("Lag'mon");
        assertThat(result).contains("LUNCH");
    }

    @Test
    @DisplayName("renderPersonalHistory — tarix yo'q — bo'sh xabar (ingliz)")
    void renderPersonalHistory_noHistory_shouldReturnEmptyMessage_english() {
        long userId = 200L;
        when(votingService.getUserHistory(userId, 30)).thenReturn(Collections.emptyList());

        String result = statisticsService.renderPersonalHistory(userId, 30, "en");

        assertThat(result).containsIgnoringCase("not voted");
    }

    @Test
    @DisplayName("renderPersonalHistory — tarix yo'q — bo'sh xabar (o'zbek)")
    void renderPersonalHistory_noHistory_shouldReturnEmptyMessage_uzbek() {
        long userId = 201L;
        when(votingService.getUserHistory(userId, 30)).thenReturn(Collections.emptyList());

        String result = statisticsService.renderPersonalHistory(userId, 30, "uz");

        assertThat(result).containsIgnoringCase("ovoz");
    }

    @Test
    @DisplayName("renderDailySummary — bugungi ovozlar soni ko'rsatilishi kerak")
    void renderDailySummary_shouldShowTodayCount() {
        when(votingService.countTodayVotes()).thenReturn(42L);

        String resultEn = statisticsService.renderDailySummary("en");
        String resultUz = statisticsService.renderDailySummary("uz");

        assertThat(resultEn).contains("42");
        assertThat(resultUz).contains("42");
        assertThat(resultEn).containsIgnoringCase("total");
        assertThat(resultUz).containsIgnoringCase("bugungi");
    }

    @Test
    @DisplayName("renderGlobalTop — limit ishlashi kerak (5 ta so'rasak 5 tadan oshmasin)")
    void renderGlobalTop_shouldRespectLimit() {
        List<Object[]> many = List.of(
                new Object[] { dish1, 10L },
                new Object[] { dish2, 7L },
                new Object[] { dish1, 5L },
                new Object[] { dish2, 4L },
                new Object[] { dish1, 3L },
                new Object[] { dish2, 2L });
        when(votingService.getGlobalTopDishes()).thenReturn(many);

        String result = statisticsService.renderGlobalTop(3, "en");

        // 3 ta medal emoji bo'lishi kerak, 4-chi bo'lmasin
        assertThat(result).contains("🥇");
        assertThat(result).contains("🥈");
        assertThat(result).contains("🥉");
        // 4. band bo'lmasin
        assertThat(result).doesNotContain("4.");
    }
}
