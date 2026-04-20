package com.example.demo;

import com.example.demo.Service.VotingService;
import com.example.demo.entity.Dish;
import com.example.demo.entity.Vote;
import com.example.demo.entity.VoteCategory;
import com.example.demo.repository.DishRepository;
import com.example.demo.repository.VoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VotingService — ovoz berish mantiqiy testlar")
class VotingServiceTest {

    @Mock
    private VoteRepository voteRepository;

    @Mock
    private DishRepository dishRepository;

    @InjectMocks
    private VotingService votingService;

    private Dish breakfastDish;
    private Dish lunchDish;

    @BeforeEach
    void setUp() {
        breakfastDish = new Dish();
        breakfastDish.setId(1L);
        breakfastDish.setName("Shirguruch");
        breakfastDish.setCategory("BREAKFAST");
        breakfastDish.setPhotoUrl("https://example.com/shirguruch.jpg");
        breakfastDish.setActive(true);
        breakfastDish.setTotalVotes(0);

        lunchDish = new Dish();
        lunchDish.setId(2L);
        lunchDish.setName("Lag'mon");
        lunchDish.setCategory("LUNCH");
        lunchDish.setPhotoUrl("https://example.com/lagmon.jpg");
        lunchDish.setActive(true);
        lunchDish.setTotalVotes(5);
    }

    @Test
    @DisplayName("Birinchi ovoz — muvaffaqiyatli saqlanishi kerak")
    void voteForDish_firstVote_shouldSucceed() {
        long userId = 100L;
        when(voteRepository.existsByUserIdAndVoteDateAndCategory(userId, LocalDate.now(), "BREAKFAST"))
                .thenReturn(false);
        when(dishRepository.save(any(Dish.class))).thenReturn(breakfastDish);
        when(voteRepository.save(any(Vote.class))).thenAnswer(inv -> inv.getArgument(0));

        VotingService.VoteResult result = votingService.voteForDish(userId, breakfastDish);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getDish().getName()).isEqualTo("Shirguruch");
        assertThat(breakfastDish.getTotalVotes()).isEqualTo(1);
        verify(dishRepository).save(breakfastDish);
        verify(voteRepository).save(any(Vote.class));
    }

    @Test
    @DisplayName("Ovoz bergandan so'ng totalVotes +1 bo'lishi kerak")
    void voteForDish_shouldIncreaseTotalVotes() {
        long userId = 101L;
        lunchDish.setTotalVotes(5);
        when(voteRepository.existsByUserIdAndVoteDateAndCategory(userId, LocalDate.now(), "LUNCH"))
                .thenReturn(false);
        when(dishRepository.save(any())).thenReturn(lunchDish);
        when(voteRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        votingService.voteForDish(userId, lunchDish);

        assertThat(lunchDish.getTotalVotes()).isEqualTo(6);
    }

    @Test
    @DisplayName("Bir xil taomga ikki marta ovoz — alreadyVoted=true qaytarishi kerak")
    void voteForDish_sameVoteTwice_shouldReturnAlreadyVoted() {
        long userId = 102L;
        Vote existingVote = new Vote();
        existingVote.setDish(breakfastDish);
        existingVote.setUserId(userId);
        existingVote.setVoteDate(LocalDate.now());
        existingVote.setCategory("BREAKFAST");

        when(voteRepository.existsByUserIdAndVoteDateAndCategory(userId, LocalDate.now(), "BREAKFAST"))
                .thenReturn(true);
        when(voteRepository.findByUserIdAndVoteDateAndCategory(userId, LocalDate.now(), "BREAKFAST"))
                .thenReturn(Optional.of(existingVote));

        VotingService.VoteResult result = votingService.voteForDish(userId, breakfastDish);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.isAlreadyVoted()).isTrue();
        verify(dishRepository, never()).save(any());
    }

    @Test
    @DisplayName("Boshqa taomga ovoz bergandan keyin shu kategoriyada qayta — alreadyVotedDifferent")
    void voteForDish_differentDishSameCategory_shouldReturnAlreadyVotedDifferent() {
        long userId = 103L;
        Dish anotherBreakfast = new Dish();
        anotherBreakfast.setId(99L);
        anotherBreakfast.setName("Qovurilgan tuxum");
        anotherBreakfast.setCategory("BREAKFAST");
        anotherBreakfast.setActive(true);
        anotherBreakfast.setTotalVotes(0);

        Vote existingVote = new Vote();
        existingVote.setDish(anotherBreakfast);
        existingVote.setUserId(userId);
        existingVote.setVoteDate(LocalDate.now());
        existingVote.setCategory("BREAKFAST");

        when(voteRepository.existsByUserIdAndVoteDateAndCategory(userId, LocalDate.now(), "BREAKFAST"))
                .thenReturn(true);
        when(voteRepository.findByUserIdAndVoteDateAndCategory(userId, LocalDate.now(), "BREAKFAST"))
                .thenReturn(Optional.of(existingVote));

        VotingService.VoteResult result = votingService.voteForDish(userId, breakfastDish);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.isAlreadyVoted()).isTrue();
    }

    @Test
    @DisplayName("changeVote — avvalgi ovoz o'chirilib yangi saqlanishi kerak")
    void changeVote_shouldUpdateVoteAndAdjustCounts() {
        long userId = 104L;
        Dish oldDish = new Dish();
        oldDish.setId(10L);
        oldDish.setName("Shirguruch");
        oldDish.setCategory("BREAKFAST");
        oldDish.setTotalVotes(3);
        oldDish.setActive(true);

        Dish newDish = new Dish();
        newDish.setId(11L);
        newDish.setName("Mannaya kasha");
        newDish.setCategory("BREAKFAST");
        newDish.setTotalVotes(1);
        newDish.setActive(true);

        Vote existingVote = new Vote();
        existingVote.setId(50L);
        existingVote.setDish(oldDish);
        existingVote.setUserId(userId);
        existingVote.setVoteDate(LocalDate.now());
        existingVote.setCategory("BREAKFAST");

        when(voteRepository.findByUserIdAndVoteDateAndCategory(userId, LocalDate.now(), "BREAKFAST"))
                .thenReturn(Optional.of(existingVote));
        when(dishRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(voteRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        VotingService.VoteResult result = votingService.changeVote(userId, newDish);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.isChanged()).isTrue();
        assertThat(oldDish.getTotalVotes()).isEqualTo(2);
        assertThat(newDish.getTotalVotes()).isEqualTo(2);
        assertThat(existingVote.getDish().getName()).isEqualTo("Mannaya kasha");
        verify(dishRepository, times(2)).save(any());
        verify(voteRepository).save(existingVote);
    }

    @Test
    @DisplayName("changeVote — bir xil taomga o'zgartirish — alreadyVotedSame")
    void changeVote_sameDish_shouldReturnAlreadyVotedSame() {
        long userId = 105L;
        Vote existingVote = new Vote();
        existingVote.setDish(lunchDish);
        existingVote.setUserId(userId);
        existingVote.setVoteDate(LocalDate.now());
        existingVote.setCategory("LUNCH");

        when(voteRepository.findByUserIdAndVoteDateAndCategory(userId, LocalDate.now(), "LUNCH"))
                .thenReturn(Optional.of(existingVote));

        VotingService.VoteResult result = votingService.changeVote(userId, lunchDish);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.isAlreadyVoted()).isTrue();
        verify(dishRepository, never()).save(any());
    }

    @Test
    @DisplayName("Noto'g'ri kategoriyali taom — error qaytarishi kerak")
    void voteForDish_invalidCategory_shouldReturnError() {
        Dish badDish = new Dish();
        badDish.setId(999L);
        badDish.setName("Noma'lum taom");
        badDish.setCategory("INVALID_CATEGORY");
        badDish.setActive(true);

        VotingService.VoteResult result = votingService.voteForDish(200L, badDish);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).containsIgnoringCase("category");
        verify(voteRepository, never()).save(any());
        verify(dishRepository, never()).save(any());
    }

    @Test
    @DisplayName("hasVotedToday — ovoz bergan foydalanuvchi uchun true")
    void hasVotedToday_whenVoted_shouldReturnTrue() {
        long userId = 110L;
        when(voteRepository.existsByUserIdAndVoteDateAndCategory(userId, LocalDate.now(), "LUNCH"))
                .thenReturn(true);

        assertThat(votingService.hasVotedToday(userId, VoteCategory.LUNCH)).isTrue();
    }

    @Test
    @DisplayName("hasVotedToday — ovoz bermagan foydalanuvchi uchun false")
    void hasVotedToday_whenNotVoted_shouldReturnFalse() {
        long userId = 111L;
        when(voteRepository.existsByUserIdAndVoteDateAndCategory(userId, LocalDate.now(), "SNACK"))
                .thenReturn(false);

        assertThat(votingService.hasVotedToday(userId, VoteCategory.SNACK)).isFalse();
    }

    @Test
    @DisplayName("VoteResult.success — to'g'ri holat")
    void voteResult_success_shouldHaveCorrectState() {
        VotingService.VoteResult result = VotingService.VoteResult.success(breakfastDish);
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.isAlreadyVoted()).isFalse();
        assertThat(result.isChanged()).isFalse();
        assertThat(result.getDish()).isEqualTo(breakfastDish);
    }

    @Test
    @DisplayName("VoteResult.changed — isChanged=true, isSuccess=true")
    void voteResult_changed_shouldHaveCorrectState() {
        VotingService.VoteResult result = VotingService.VoteResult.changed(breakfastDish, lunchDish);
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.isChanged()).isTrue();
        assertThat(result.isAlreadyVoted()).isFalse();
        assertThat(result.getPreviousDish()).isEqualTo(breakfastDish);
        assertThat(result.getDish()).isEqualTo(lunchDish);
    }

    @Test
    @DisplayName("VoteResult.error — isSuccess=false, message to'g'ri")
    void voteResult_error_shouldHaveCorrectState() {
        VotingService.VoteResult result = VotingService.VoteResult.error("Test xatosi");
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).isEqualTo("Test xatosi");
    }
}
