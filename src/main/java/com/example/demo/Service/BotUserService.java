package com.example.demo.Service;

import com.example.demo.config.BotConfig;
import com.example.demo.entity.TelegramUser;
import com.example.demo.repository.TelegramUserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BotUserService {
    private static final Logger log = LoggerFactory.getLogger(BotUserService.class);
    private final TelegramUserRepository userRepository;
    private final BotConfig botConfig;

    @Transactional
    public TelegramUser registerOrUpdate(Long telegramId, String username, String firstName, String lastName, String languageCode) {
        return userRepository.findByTelegramId(telegramId)
                .map(existing -> {
                    existing.setUsername(username);
                    existing.setFirstName(firstName);
                    existing.setLastName(lastName);
                    existing.setLanguageCode(languageCode != null ? languageCode : botConfig.getDefaultLanguage());
                    existing.setLastSeenAt(LocalDateTime.now());
                    return existing;
                })
                .orElseGet(() -> TelegramUser.builder()
                        .telegramId(telegramId)
                        .username(username)
                        .firstName(firstName)
                        .lastName(lastName)
                        .languageCode(languageCode != null ? languageCode : botConfig.getDefaultLanguage())
                        .joinedAt(LocalDateTime.now())
                        .lastSeenAt(LocalDateTime.now())
                        .admin(botConfig.isAdmin(telegramId))
                        .build())
                .tap(userRepository::save);
    }

    public List<TelegramUser> getAllUsers() {
        return userRepository.findAll();
    }

    public boolean isAdmin(Long telegramId) {
        return userRepository.findByTelegramId(telegramId)
                .map(TelegramUser::isAdmin)
                .orElse(botConfig.isAdmin(telegramId));
    }
}
