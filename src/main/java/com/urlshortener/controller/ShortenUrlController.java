package com.urlshortener.controller;

import com.urlshortener.dto.request.ShortenUrlRequest;
import com.urlshortener.dto.response.ClickLogResponse;
import com.urlshortener.dto.response.ShortenUrlResponse;
import com.urlshortener.service.ShortenUrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/shortenUrl")
@RequiredArgsConstructor
public class ShortenUrlController {
    private final ShortenUrlService shortenUrlService;

    /*
    Endpoint to create short URL
     */
    @PostMapping
    public ResponseEntity<ShortenUrlResponse> shorten(@RequestHeader("Authorization") String token,
                                          @RequestBody ShortenUrlRequest shortenUrlRequest) {
        return ResponseEntity.ok(shortenUrlService.shortenUrl(shortenUrlRequest, token));
    }

    /*
    Endpoint to redirect from short URL
     */
    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
        ShortenUrlResponse url = shortenUrlService.getShortenUrl(shortCode);
        return ResponseEntity.status(HttpStatus.PERMANENT_REDIRECT)
                .location(URI.create(url.getOriginalUrl()))
                .build();
    }

    /*
    Endpoint to get short URL detail
     */
    @GetMapping("/info/{shortCode}")
    public ResponseEntity<ShortenUrlResponse> info(@PathVariable String shortCode) {
        return ResponseEntity.ok(shortenUrlService.getShortenUrl(shortCode));
    }

    /*
    Endpoint to get short URL click statistic
     */
    @GetMapping("/stats/{shortCode}")
    public ResponseEntity<List<ClickLogResponse>> stats(@RequestHeader("Authorization") String token, @PathVariable String shortCode) {
        return ResponseEntity.ok(shortenUrlService.getStats(shortCode, token));
    }
}

