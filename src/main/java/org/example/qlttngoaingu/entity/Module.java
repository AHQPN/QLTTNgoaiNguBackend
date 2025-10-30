package org.example.qlttngoaingu.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "module")
@Getter
@Setter
public class Module {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mamodule")
    private Integer moduleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "makhoahoc", nullable = false)
    @JsonIgnore
    private Course course;

    @Column(name = "tenmodule", length = 200)
    private String moduleName;



    @OneToMany(mappedBy = "module", cascade = CascadeType.ALL, orphanRemoval = true)

    private List<Content> contents = new ArrayList<>();

    @OneToMany(mappedBy = "module", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Document> documents = new ArrayList<>();
}