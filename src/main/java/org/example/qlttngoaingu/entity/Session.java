package org.example.qlttngoaingu.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

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