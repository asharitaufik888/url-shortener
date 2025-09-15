package com.urlshortener.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShortenUrlResponse {
    private String originalUrl;
    private String shortCode;
    private long clickCount;
    private LocalDateTime createdAt;
}
