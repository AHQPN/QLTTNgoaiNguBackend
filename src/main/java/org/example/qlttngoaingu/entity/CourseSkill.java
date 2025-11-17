package org.example.qlttngoaingu.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Table(name = "khoahockynang")
@Data
public class CourseSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "makhkn")
    private Integer courseSkillId;

    // FK → khoahoc (course)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "makhoahoc", nullable = false)
    private Course course;

    // FK → kynang (skill)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "makynang", nullable = false)
    private Skill skill;

    // One course-skill can have many modules
    @OneToMany(mappedBy = "courseSkill", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Module> modules;


}
