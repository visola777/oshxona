package com.example.demo;

import com.example.demo.Service.AdminService;
import com.example.demo.Service.BotUserService;
import com.example.demo.entity.Dish;
import com.example.demo.entity.TelegramUser;
import com.example.demo.entity.Vote;
import com.example.demo.repository.VoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminService — admin amallar testlar")
class AdminServiceTest {

    @Mock
    private BotUserService userService;

    @Mock
    private VoteRepository voteRepository;

    @InjectMocks
    private AdminService adminService;

    private Dish dish;
    private Vote vote1;
    private Vote vote2;

    @BeforeEach
    void setUp() {
        dish = new Dish();
        dish.setId(1L);
        dish.setName("Lag'mon");
        dish.setCategory("LUNCH");
        dish.setTotalVotes(2);
        dish.setActive(true);

        vote1 = new Vote();
        vote1.setId(1L);
        vote1.setUserId(100L);
        vote1.setDish(dish);
        vote1.setVoteDate(LocalDate.now());
        vote1.setCategory("LUNCH");
        vote1.setFoodName("Lag'mon");

        vote2 = new Vote();
        vote2.setId(2L);
        vote2.setUserId(101L);
        vote2.setDish(dish);
        vote2.setVoteDate(LocalDate.now());
        vote2.setCategory("LUNCH");
        vote2.setFoodName("Lag'mon");
    }

    @Test
    @DisplayName("countVotesToday — bugungi ovozlar sonini to'g'ri qaytarishi kerak")
    void countVotesToday_shouldReturnCorrectCount() {
        when(voteRepository.findAllByVoteDate(LocalDate.now())).thenReturn(List.of(vote1, vote2));

        long count = adminService.countVotesToday();

        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("exportVotesCsv — CSV fayl header va qatorlarni o'z ichiga olishi kerak")
    void exportVotesCsv_shouldContainHeaderAndRows() {
        when(voteRepository.findAll()).thenReturn(List.of(vote1, vote2));

        byte[] csv = adminService.exportVotesCsv();
        String content = new String(csv);

        assertThat(content).startsWith("userId,username,dish,category,voteDate");
        assertThat(content).contains("100");
        assertThat(content).contains("101");
        assertThat(content).contains("Lag'mon");
        assertThat(content).contains("LUNCH");
    }

    @Test
    @DisplayName("exportVotesCsv — bo'sh holatda faqat header bo'lishi kerak")
    void exportVotesCsv_empty_shouldReturnOnlyHeader() {
        when(voteRepository.findAll()).thenReturn(List.of());

        byte[] csv = adminService.exportVotesCsv();
        String content = new String(csv);

        assertThat(content.trim()).isEqualTo("userId,username,dish,category,voteDate");
    }

    @Test
    @DisplayName("resetTodayVotes — bugungi ovozlar o'chirilishi va totalVotes kamayishi kerak")
    void resetTodayVotes_shouldDeleteVotesAndDecrementDishCounts() {
        dish.setTotalVotes(2);
        when(voteRepository.findAllByVoteDate(LocalDate.now())).thenReturn(List.of(vote1, vote2));

        adminService.resetTodayVotes();

        assertThat(dish.getTotalVotes()).isEqualTo(0);
        verify(voteRepository).deleteAll(List.of(vote1, vote2));
    }

    @Test
    @DisplayName("resetTodayVotes — ovoz yo'q — xato chiqmasligi kerak")
    void resetTodayVotes_noVotes_shouldNotThrow() {
        when(voteRepository.findAllByVoteDate(LocalDate.now())).thenReturn(List.of());

        adminService.resetTodayVotes();

        verify(voteRepository).deleteAll(List.of());
    }

    @Test
    @DisplayName("allUsers — barcha foydalanuvchilar ro'yxati qaytarishi kerak")
    void allUsers_shouldReturnUserList() {
        TelegramUser u = new TelegramUser();
        u.setTelegramId(100L);
        when(userService.getAllUsers()).thenReturn(List.of(u));

        List<TelegramUser> users = adminService.allUsers();

        assertThat(users).hasSize(1);
        assertThat(users.get(0).getTelegramId()).isEqualTo(100L);
    }
}
