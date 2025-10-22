package org.example.qlttngoaingu.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "refresh_token",nullable = false, unique = true)
    private String refreshToken;

    @Column(name = "hethan", nullable = false)
    private Instant expiryDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manguoidung", nullable = false)
    private User user;

    @Column(name = "dathuhoi", nullable = false)
    private Boolean revoked = false;
}
