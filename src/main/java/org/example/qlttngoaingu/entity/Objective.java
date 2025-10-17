package org.example.qlttngoaingu.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "muctieukh")
@Getter @Setter
public class Objective {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "tenmuctieu", columnDefinition = "TEXT")
    private String objectiveName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "makhoahoc", nullable = false)
    @JsonIgnore
    private Course course;
}
