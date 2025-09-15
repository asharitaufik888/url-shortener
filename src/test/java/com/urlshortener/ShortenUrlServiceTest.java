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
    void shortenUrlShouldReturnResponseWhenTokenIsValidAndCustomCodeIsAvailable() {
        String token = "validToken";
        String username = "user";
        String customCode = "custom123";
        ShortenUrlRequest request = new ShortenUrlRequest("https://lalala.com", customCode);

        User user = new User();
        user.setUsername(username);

        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.extractUsername(token)).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(shortenUrlRepository.existsByShortCode(customCode)).thenReturn(false);
        when(shortenUrlRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ShortenUrlResponse response = shortenUrlService.shortenUrl(request, token);

        assertEquals("https://lalala.com", response.getOriginalUrl());
        assertEquals(customCode, response.getShortCode());
        assertEquals(0, response.getClickCount());
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
        String token = "validToken";
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

        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(shortenUrlRepository.findByShortCode(shortCode)).thenReturn(Optional.of(url));
        when(clickLogRepository.findAllByShortenUrlOrderByDateDesc(url)).thenReturn(List.of(log1, log2));

        List<ClickLogResponse> stats = shortenUrlService.getStats(shortCode, token);

        assertEquals(2, stats.size());
        assertEquals(shortCode, stats.get(0).getShortCode());
        assertEquals(5, stats.get(0).getClickCount());
    }

    @Test
    void shortenUrlShouldThrowJwtExceptionWhenTokenIsInvalid() {
        String token = "invalidToken";
        ShortenUrlRequest request = new ShortenUrlRequest("https://lalala.com", null);

        when(jwtUtil.validateToken(token)).thenReturn(false);

        assertThrows(JwtException.class, () -> shortenUrlService.shortenUrl(request, token));
    }

    @Test
    void getStatsShouldThrowJwtExceptionWhenTokenIsInvalid() {
        String token = "invalidToken";

        when(jwtUtil.validateToken(token)).thenReturn(false);

        assertThrows(JwtException.class, () -> shortenUrlService.getStats("abc123", token));
    }
}

