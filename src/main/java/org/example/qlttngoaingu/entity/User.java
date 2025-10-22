package org.example.qlttngoaingu.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.qlttngoaingu.service.enums.RoleEnum;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter @Setter
@Table(name = "nguoidung")
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
    private String role;

    @Column(name = "ngaytao", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "daxacthuc", nullable = false)
    private Boolean isVerified = false;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VerificationCode> verificationCodes;



}
