package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

/**
 * Context loads — Spring konteksti to'g'ri yuklanishini tekshiradi.
 * TelegramBot auto-register bo'lmasligi uchun mock token berilgan.
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
class Demo2ApplicationTests {

    @Test
    void contextLoads() {
        // Spring konteksti muvaffaqiyatli yuklanishi kerak
    }
}
