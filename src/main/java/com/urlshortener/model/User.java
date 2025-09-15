package com.urlshortener.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    //cukup 64 karakter untuk menyimpan hashed password
    @Column(nullable = false, length = 64)
    private String password;
}
