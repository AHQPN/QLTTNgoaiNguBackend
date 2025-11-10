package org.example.qlttngoaingu.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "khoahoc")
@Getter @Setter
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "makhoahoc")
    private Integer courseId;

    @Column(name = "tenkhoahoc", length = 200)
    private String courseName;

    @Column(name = "sogiohoc")
    private Integer studyHours;

    @Column(name = "hocphi")
    private Double tuitionFee;


    @Column(name = "video", length = 255)
    private String video;

    @Column(name = "trangthai")
    private Boolean status;

    @Column(name = "ngaytao")
    private LocalDateTime createdDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "madm")
    @JsonBackReference
    private CourseCategory courseCategory;

    @Column(name = "nguoitao", length = 100)
    private String createdBy;

    @Column(name = "mota",columnDefinition = "TEXT")
    private String description;

    @Column(name = "dauvao")
    private String entryLevel;

    @Column(name = "daura")
    private String targetLevel;

    @Column(name = "hinhanh")
    private String image;



    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Objective> objectives = new ArrayList<>();

}