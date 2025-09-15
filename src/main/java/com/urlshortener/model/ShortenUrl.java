package com.urlshortener.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "shorten_url", indexes = {
        @Index(name = "idx_short_code", columnList = "shortCode"),
        @Index(name = "idx_created_at", columnList = "createdAt")
})
@Data
public class ShortenUrl {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String originalUrl;

    @Column(unique = true, nullable = false, length = 100)
    private String shortCode;

    private long clickCount = 0L;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime expiredAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
