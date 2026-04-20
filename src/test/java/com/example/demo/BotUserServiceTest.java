package com.example.demo;

import com.example.demo.Service.BotUserService;
import com.example.demo.config.BotConfig;
import com.example.demo.entity.TelegramUser;
import com.example.demo.repository.TelegramUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BotUserService — foydalanuvchi ro'yxatdan o'tish testlar")
class BotUserServiceTest {

    @Mock
    private TelegramUserRepository userRepository;

    @Mock
    private BotConfig botConfig;

    @InjectMocks
    private BotUserService botUserService;

    @BeforeEach
    void setUp() {
        when(botConfig.getDefaultLanguage()).thenReturn("uz");
    }

    @Test
    @DisplayName("Yangi foydalanuvchi — ma'lumotlar bazasiga saqlanishi kerak")
    void registerOrUpdate_newUser_shouldSave() {
        Long telegramId = 12345L;
        when(userRepository.findByTelegramId(telegramId)).thenReturn(Optional.empty());
        when(botConfig.isAdmin(telegramId)).thenReturn(false);
        when(userRepository.save(any(TelegramUser.class))).thenAnswer(inv -> {
            TelegramUser u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        TelegramUser result = botUserService.registerOrUpdate(telegramId, "test_user", "Ali", "Valiyev", "uz");

        assertThat(result.getTelegramId()).isEqualTo(telegramId);
        assertThat(result.getUsername()).isEqualTo("test_user");
        assertThat(result.getFirstName()).isEqualTo("Ali");
        assertThat(result.getLanguageCode()).isEqualTo("uz");
        assertThat(result.isAdmin()).isFalse();
        verify(userRepository).save(any(TelegramUser.class));
    }

    @Test
    @DisplayName("Mavjud foydalanuvchi — yangilangan ma'lumotlar bilan qaytarishi kerak")
    void registerOrUpdate_existingUser_shouldUpdate() {
        Long telegramId = 22222L;
        TelegramUser existing = new TelegramUser();
        existing.setId(5L);
        existing.setTelegramId(telegramId);
        existing.setUsername("old_username");
        existing.setFirstName("Eski Ism");
        existing.setLanguageCode("en");
        existing.setJoinedAt(LocalDateTime.now().minusDays(10));
        existing.setLastSeenAt(LocalDateTime.now().minusDays(1));

        when(userRepository.findByTelegramId(telegramId)).thenReturn(Optional.of(existing));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TelegramUser result = botUserService.registerOrUpdate(telegramId, "new_username", "Yangi Ism", "Familiya", "uz");

        assertThat(result.getUsername()).isEqualTo("new_username");
        assertThat(result.getFirstName()).isEqualTo("Yangi Ism");
        assertThat(result.getLanguageCode()).isEqualTo("uz");
        verify(userRepository).save(existing);
    }

    @Test
    @DisplayName("Til kodi null bo'lsa — default til ishlatilishi kerak")
    void registerOrUpdate_nullLanguage_shouldUseDefault() {
        Long telegramId = 33333L;
        when(userRepository.findByTelegramId(telegramId)).thenReturn(Optional.empty());
        when(botConfig.isAdmin(telegramId)).thenReturn(false);
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TelegramUser result = botUserService.registerOrUpdate(telegramId, "user", "Ism", null, null);

        assertThat(result.getLanguageCode()).isEqualTo("uz");
    }

    @Test
    @DisplayName("Admin ID — isAdmin true qaytarishi kerak")
    void isAdmin_forAdminUser_shouldReturnTrue() {
        Long adminId = 999999L;
        TelegramUser adminUser = new TelegramUser();
        adminUser.setTelegramId(adminId);
        adminUser.setAdmin(true);

        when(userRepository.findByTelegramId(adminId)).thenReturn(Optional.of(adminUser));

        assertThat(botUserService.isAdmin(adminId)).isTrue();
    }

    @Test
    @DisplayName("Oddiy foydalanuvchi — isAdmin false qaytarishi kerak")
    void isAdmin_forRegularUser_shouldReturnFalse() {
        Long userId = 555L;
        TelegramUser user = new TelegramUser();
        user.setTelegramId(userId);
        user.setAdmin(false);

        when(userRepository.findByTelegramId(userId)).thenReturn(Optional.of(user));

        assertThat(botUserService.isAdmin(userId)).isFalse();
    }

    @Test
    @DisplayName("Topilmagan foydalanuvchi — BotConfig dan admin tekshirishi kerak")
    void isAdmin_unknownUser_shouldFallbackToBotConfig() {
        Long unknownId = 777L;
        when(userRepository.findByTelegramId(unknownId)).thenReturn(Optional.empty());
        when(botConfig.isAdmin(unknownId)).thenReturn(true);

        assertThat(botUserService.isAdmin(unknownId)).isTrue();
        verify(botConfig).isAdmin(unknownId);
    }

    @Test
    @DisplayName("getAllUsers — barcha foydalanuvchilarni qaytarishi kerak")
    void getAllUsers_shouldReturnAllUsers() {
        TelegramUser u1 = new TelegramUser();
        u1.setTelegramId(1L);
        TelegramUser u2 = new TelegramUser();
        u2.setTelegramId(2L);

        when(userRepository.findAll()).thenReturn(List.of(u1, u2));

        List<TelegramUser> users = botUserService.getAllUsers();

        assertThat(users).hasSize(2);
        assertThat(users).extracting(TelegramUser::getTelegramId).containsExactly(1L, 2L);
    }
}
