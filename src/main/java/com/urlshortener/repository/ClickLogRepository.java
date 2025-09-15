package com.urlshortener.repository;

import com.urlshortener.model.ClickLog;
import com.urlshortener.model.ShortenUrl;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClickLogRepository extends JpaRepository<ClickLog, UUID> {
    Optional<ClickLog> findByShortenUrlAndDate(ShortenUrl shortenUrl, LocalDate localDate);
    List<ClickLog> findAllByShortenUrlOrderByDateDesc(ShortenUrl shortenUrl);

}
