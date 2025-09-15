package com.urlshortener.service;

import com.urlshortener.dto.request.ShortenUrlRequest;
import com.urlshortener.dto.response.ClickLogResponse;
import com.urlshortener.dto.response.ShortenUrlResponse;
import com.urlshortener.model.ClickLog;
import com.urlshortener.model.ShortenUrl;
import com.urlshortener.repository.ClickLogRepository;
import com.urlshortener.repository.ShortenUrlRepository;
import com.urlshortener.repository.UserRepository;
import com.urlshortener.security.JwtUtil;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShortenUrlService {
    @Value("${shortUrl.expirationDays}")
    private long expirationDays;

    private final ShortenUrlRepository shortenUrlRepository;
    private final ClickLogRepository clickLogRepository;
    private final RedisTemplate<String, ShortenUrl> redisTemplate;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    private static final String PREFIX = "url:";

    public ShortenUrlResponse shortenUrl(ShortenUrlRequest shortenUrlRequest, String token) {
        if(!jwtUtil.validateToken(token)) {
            throw new JwtException("Invalid token");
        }

        String shortCode = (shortenUrlRequest.getCustomCode() != null && !shortenUrlRepository.existsByShortCode(shortenUrlRequest.getCustomCode()))
                ? shortenUrlRequest.getCustomCode()
                : UUID.randomUUID().toString().substring(0, 8);

        ShortenUrl shortenUrl = new ShortenUrl();
        shortenUrl.setOriginalUrl(shortenUrlRequest.getOriginalUrl());
        shortenUrl.setShortCode(shortCode);
        shortenUrl.setUser(userRepository.findByUsername(jwtUtil.extractUsername(token))
                .orElseThrow(() -> new IllegalArgumentException("User not found")));
        shortenUrl.setExpiredAt(LocalDateTime.now().plusDays(expirationDays));
        ShortenUrl result = shortenUrlRepository.save(shortenUrl);

        redisTemplate.opsForValue().set(PREFIX + shortCode, result);
        return ShortenUrlResponse
                .builder()
                .originalUrl(result.getOriginalUrl())
                .shortCode(result.getShortCode())
                .clickCount(result.getClickCount())
                .createdAt(result.getCreatedAt())
                .build();
    }

    public ShortenUrlResponse getShortenUrl(String shortCode) {
        String key = PREFIX + shortCode;
        ShortenUrl cached = redisTemplate.opsForValue().get(key);

        ShortenUrl shortenUrl = (cached != null) ? cached :
                shortenUrlRepository.findByShortCode(shortCode)
                        .orElseThrow(() -> new IllegalArgumentException("Shorten URL not found"));

        shortenUrl.setClickCount(shortenUrl.getClickCount() + 1);
        shortenUrlRepository.save(shortenUrl);

        LocalDate today = LocalDate.now();
        ClickLog log = clickLogRepository.findByShortenUrlAndDate(shortenUrl, today)
                .orElseGet(() -> {
                    ClickLog newLog = new ClickLog();
                    newLog.setShortenUrl(shortenUrl);
                    newLog.setDate(today);
                    return newLog;
                });

        log.setCount(log.getCount() + 1);
        clickLogRepository.save(log);

        redisTemplate.opsForValue().set(key, shortenUrl);
        return ShortenUrlResponse
                .builder()
                .originalUrl(shortenUrl.getOriginalUrl())
                .shortCode(shortCode)
                .clickCount(shortenUrl.getClickCount())
                .createdAt(shortenUrl.getCreatedAt()).build();
    }

    public List<ClickLogResponse> getStats(String shortCode, String token) {
        //Throw exception when token invalid
        if(!jwtUtil.validateToken(token)) {
            throw new JwtException("Invalid token");
        }
        ShortenUrl shortenUrl = shortenUrlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new IllegalArgumentException("URL not found"));
        List<ClickLog> clickLogs = clickLogRepository.findAllByShortenUrlOrderByDateDesc(shortenUrl);
        List<ClickLogResponse> result = new ArrayList<>();
        //Populate clickLogs
        for(ClickLog clickLog : clickLogs) {
            result.add(ClickLogResponse
                    .builder()
                    .shortCode(clickLog.getShortenUrl().getShortCode())
                    .clickCount(clickLog.getCount())
                    .date(clickLog.getDate())
                    .build());
        }
        return result;
    }
}