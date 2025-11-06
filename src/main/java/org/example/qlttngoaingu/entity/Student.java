package org.example.qlttngoaingu.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "hocvien")
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mahv")
    private Integer studentId;
}
