package org.example.qlttngoaingu.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;
@Entity
@Table(name = "danhmuc")
@Data
public class CourseCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "madanhmuc")
    private Integer id;

    @Column(name = "tendm", nullable = false, unique = true)
    private String name;

    @Column(name = "level")
    private String level;

    @Column(name = "mota")
    private String description;

    @OneToMany(mappedBy = "courseCategory", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Course> courses;

}
