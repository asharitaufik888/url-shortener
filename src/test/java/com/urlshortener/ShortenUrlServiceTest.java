package com.urlshortener;

import com.urlshortener.dto.request.ShortenUrlRequest;
import com.urlshortener.dto.response.ClickLogResponse;
import com.urlshortener.dto.response.ShortenUrlResponse;
import com.urlshortener.model.ClickLog;
import com.urlshortener.model.ShortenUrl;
import com.urlshortener.model.User;
import com.urlshortener.repository.ClickLogRepository;
import com.urlshortener.repository.ShortenUrlRepository;
import com.urlshortener.repository.UserRepository;
import com.urlshortener.security.JwtUtil;
import com.urlshortener.service.ShortenUrlService;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShortenUrlServiceTest {

    @Mock
    private ShortenUrlRepository shortenUrlRepository;
    @Mock private ClickLogRepository clickLogRepository;
    @Mock private RedisTemplate<String, ShortenUrl> redisTemplate;
    @Mock private UserRepository userRepository;
    @Mock private JwtUtil jwtUtil;
    @Mock private ValueOperations<String, ShortenUrl> valueOperations;

    @InjectMocks
    private ShortenUrlService shortenUrlService;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(shortenUrlService, "expirationDays", 30L);
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void shortenUrlShouldReturnResponseForValidUser() {
        String username = "user";
        String customCode = "custom123";
        ShortenUrlRequest request = new ShortenUrlRequest("https://lalala.com", customCode);

        User user = new User();
        user.setUsername(username);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(shortenUrlRepository.existsByShortCode(customCode)).thenReturn(false);
        when(shortenUrlRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ShortenUrlResponse response = shortenUrlService.shortenUrl(request, username);

        assertEquals("https://lalala.com", response.getOriginalUrl());
        assertEquals(customCode, response.getShortCode());
        assertNotNull(response.getCreatedAt());
    }

    @Test
    void getShortenUrlShouldReturnResponseWhenShortCodeExists() {
        String shortCode = "abc123";
        String key = "url:" + shortCode;

        ShortenUrl url = new ShortenUrl();
        url.setShortCode(shortCode);
        url.setOriginalUrl("https://lalala.com");
        url.setClickCount(0);
        url.setCreatedAt(LocalDateTime.now());

        when(valueOperations.get(key)).thenReturn(null);
        when(shortenUrlRepository.findByShortCode(shortCode)).thenReturn(Optional.of(url));
        when(clickLogRepository.findByShortenUrlAndDate(eq(url), any())).thenReturn(Optional.empty());
        when(shortenUrlRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(clickLogRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ShortenUrlResponse response = shortenUrlService.getShortenUrl(shortCode);

        assertEquals("https://lalala.com", response.getOriginalUrl());
        assertEquals(shortCode, response.getShortCode());
        assertEquals(1, response.getClickCount());
    }

    @Test
    void getStatsShouldReturnClickLogResponsesWhenTokenIsValid() {
        String shortCode = "abc123";
        String username = "user";

        ShortenUrl url = new ShortenUrl();
        url.setShortCode(shortCode);

        ClickLog log1 = new ClickLog();
        log1.setShortenUrl(url);
        log1.setDate(LocalDate.now());
        log1.setCount(5);

        ClickLog log2 = new ClickLog();
        log2.setShortenUrl(url);
        log2.setDate(LocalDate.now().minusDays(1));
        log2.setCount(3);

        when(shortenUrlRepository.findByShortCodeAndUserUsername(shortCode, username)).thenReturn(Optional.of(url));

        when(clickLogRepository.findAllByShortenUrlOrderByDateDesc(url)).thenReturn(List.of(log1, log2));

        List<ClickLogResponse> stats = shortenUrlService.getStats(shortCode, username);

        assertEquals(2, stats.size());
        assertEquals(shortCode, stats.get(0).getShortCode());
        assertEquals(5, stats.get(0).getClickCount());
    }

    @Test
    void shortenUrlShouldThrowIllegalArgumentExceptionWhenTokenIsInvalid() {
        String token = "invalidToken";
        ShortenUrlRequest request = new ShortenUrlRequest("https://lalala.com", null);

        assertThrows(IllegalArgumentException.class, () -> shortenUrlService.shortenUrl(request, token));
    }

    @Test
    void getStatsShouldReturnClickLogResponsesForUserOwnedUrl() {
        String shortCode = "abc123";
        String username = "user";

        ShortenUrl url = new ShortenUrl();
        url.setId(UUID.randomUUID());
        url.setShortCode(shortCode);

        ClickLog log1 = new ClickLog(url, LocalDate.now(), 5L);
        ClickLog log2 = new ClickLog(url, LocalDate.now().minusDays(1), 3L);

        when(shortenUrlRepository.findByShortCodeAndUserUsername(shortCode, username)).thenReturn(Optional.of(url));
        when(clickLogRepository.findAllByShortenUrlOrderByDateDesc(url)).thenReturn(List.of(log1, log2));

        List<ClickLogResponse> stats = shortenUrlService.getStats(shortCode, username);

        assertEquals(2, stats.size());
        assertEquals(5L, stats.get(0).getClickCount());
    }

}

