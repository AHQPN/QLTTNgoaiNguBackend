package org.example.qlttngoaingu.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "phong")
@Data
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "maphong")
    private Integer roomId;

    @Column(name = "tenphong", length = 255)
    private String roomName;

    @Column(name = "succhua")
    private Integer capacity;

    @Column(name = "trangthai", length = 50)
    private String status;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CourseClass> classes;

    // Getters and Setters (omitted for brevity)
}