package org.example.qlttngoaingu.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Table(name = "kynang")
@Data
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "makynang")
    private Integer skillId;

    @Column(name = "tenkynang", nullable = false)
    private String skillName;

    // One skill can belong to many course-skill links
    @OneToMany(mappedBy = "skill", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CourseSkill> courseSkills;
}
