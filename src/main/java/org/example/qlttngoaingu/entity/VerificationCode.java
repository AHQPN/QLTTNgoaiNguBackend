package org.example.qlttngoaingu.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.qlttngoaingu.service.enums.VerificationCodeEnum;

import java.time.LocalDateTime;

@Entity
@Table(name = "maxacthuc")
@Getter
@Setter
public class VerificationCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manguoidung", nullable = false)
    private User user;

    @Column(name = "maxacthuc", nullable = false)
    private String verificationCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "loaixacthuc", nullable = false, length = 50)
    private VerificationCodeEnum type;

    @Column(name = "ngaytao", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "hethan")
    private LocalDateTime expiresAt;


}