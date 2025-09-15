package com.urlshortener.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "click_log", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"shorten_url_id", "date"})
})
@Data
public class ClickLog {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "shorten_url_id", nullable = false)
    private ShortenUrl shortenUrl;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private long count = 0L;
}