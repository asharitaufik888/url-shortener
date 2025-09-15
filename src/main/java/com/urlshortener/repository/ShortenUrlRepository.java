package com.urlshortener.repository;

import com.urlshortener.model.ShortenUrl;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ShortenUrlRepository extends JpaRepository<ShortenUrl, UUID> {
    Optional<ShortenUrl> findByShortCode(String shortCode);
    boolean existsByShortCode(String shortCode);
    Optional<ShortenUrl> findByShortCodeAndUserUsername(String shortCode, String username);

}
