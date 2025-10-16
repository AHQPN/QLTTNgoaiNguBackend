package org.example.qlttngoaingu.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.qlttngoaingu.Service.enums.RoleEnum;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "manguoidung")
    private Integer userId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "sdt", unique = true)
    private String phoneNumber;

    @Column(name = "matkhau", nullable = false)
    private String passwordHash;

    @Column(name = "vaitro", nullable = false)
    private RoleEnum role;

    @Column(name = "ngaytao", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "daxacthuc", nullable = false)
    private Boolean isVerified = false;

    @Column(name = "maxacthuc", nullable = true)
    private String verificationCode;

    @Column(name = "mahethan")
    private LocalDateTime codeExpiration;


}
