package org.example.qlttngoaingu.entity;

import jakarta.persistence.*;

@Entity
public class Degree {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "tenbangcap")
    private String name;
}
