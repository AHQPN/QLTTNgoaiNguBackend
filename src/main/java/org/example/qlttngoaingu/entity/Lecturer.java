package org.example.qlttngoaingu.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "giangvien") // Maps to Vietnamese table name
public class Lecturer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "magv")
    private Integer lecturerId;

    @Column(name = "hoten", length = 200)
    private String fullName; // Maps to hoten

    @Column(name = "ngaysinh")
    private LocalDate dateOfBirth;

    @Column(name = "sodienthoai", length = 10)
    private String phoneNumber;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "hinhanh", length = 255)
    private String imagePath;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manguoidung", referencedColumnName = "manguoidung")
    private User user;

    @OneToMany(mappedBy = "lecturer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CourseClass> classes;

    // Getters and Setters (omitted for brevity)
}