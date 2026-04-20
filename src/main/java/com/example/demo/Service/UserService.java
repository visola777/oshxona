package com.example.demo.Service;

import com.example.demo.config.BotConfig;
import com.example.demo.entity.TelegramUser;
import com.example.demo.repository.TelegramUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final TelegramUserRepository userRepository;
    private final BotConfig botConfig;

    public UserService(TelegramUserRepository userRepository, BotConfig botConfig) {
        this.userRepository = userRepository;
        this.botConfig = botConfig;
    }

    @Transactional
    public TelegramUser registerOrUpdateUser(Long telegramId, String username, String firstName, String lastName,
            String languageCode) {
        TelegramUser user = userRepository.findByTelegramId(telegramId)
                .map(existing -> {
                    existing.setUsername(username);
                    existing.setFirstName(firstName);
                    existing.setLastName(lastName);
                    existing.setLanguageCode(languageCode != null ? languageCode : botConfig.getDefaultLanguage());
                    existing.setLastSeenAt(LocalDateTime.now());
                    return existing;
                })
                .orElseGet(() -> {
                    TelegramUser newUser = new TelegramUser();
                    newUser.setTelegramId(telegramId);
                    newUser.setUsername(username);
                    newUser.setFirstName(firstName);
                    newUser.setLastName(lastName);
                    newUser.setLanguageCode(languageCode != null ? languageCode : botConfig.getDefaultLanguage());
                    newUser.setJoinedAt(LocalDateTime.now());
                    newUser.setLastSeenAt(LocalDateTime.now());
                    newUser.setAdmin(botConfig.isAdmin(telegramId));
                    return newUser;
                });

        TelegramUser saved = userRepository.save(user);
        log.debug("Registered or updated user: {} (admin={})", saved.getTelegramId(), saved.isAdmin());
        return saved;
    }

    public List<TelegramUser> getAllUsers() {
        return userRepository.findAll();
    }

    public boolean isAdmin(Long telegramId) {
        return userRepository.findByTelegramId(telegramId).map(TelegramUser::isAdmin)
                .orElse(botConfig.isAdmin(telegramId));
    }
}
