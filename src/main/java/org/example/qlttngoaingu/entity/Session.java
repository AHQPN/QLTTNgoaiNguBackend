package org.example.qlttngoaingu.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "buoihoc") // Maps to Vietnamese table name
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mabuoihoc")
    private Integer sessionId;

    @Column(name = "ngayhoc")
    private LocalDate sessionDate;

    @Column(name = "trangthai")
    private String status;

    @Column(name = "ghichu", columnDefinition = "TEXT")
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "malop", referencedColumnName = "malop")
    private CourseClass courseClass;

}