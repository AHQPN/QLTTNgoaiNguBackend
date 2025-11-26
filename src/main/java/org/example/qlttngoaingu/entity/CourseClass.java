package org.example.qlttngoaingu.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Entity
@Data
@Table(name = "lop")
public class CourseClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "malop")
    private Integer classId;

    @Column(name = "tenlop")
    private String className;

    @Column(name = "ngaylap")
    private LocalDateTime dateCreated;

    @Column(name = "ghichu", columnDefinition = "TEXT")
    private String note;

    @Column(name = "trangthai")
    private String status;

    @Column(name = "ngaybatdau")
    private LocalDate startDate;

    @Column(name = "sogiohocmoibuoi")
    private Integer minutesPerSession;

    @Column(name = "giobatdau")
    private LocalTime startTime;

    @Column(name = "lich", length = 100)
    private String schedule;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "magiangvien", referencedColumnName = "magv")
    private Lecturer lecturer;

    // --- Many-to-One Relationship with Room (maphong) ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maphong", referencedColumnName = "maphong")
    private Room room;

    // --- Many-to-One Relationship with Course (makhoahoc - Assumed Entity) ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "makhoahoc")
    private Course course; // Maps to makhoahoc

    // --- One-to-Many Relationship with Session ---
    @OneToMany(mappedBy = "courseClass", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Session> sessions; // Maps to buoihoc

}