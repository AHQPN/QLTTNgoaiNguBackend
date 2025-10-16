package org.example.qlttngoaingu.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "noidung")
@Getter @Setter
public class Content {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "tennoidung", columnDefinition = "TEXT")
    private String contentName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mamodule", nullable = false)
    @JsonIgnore
    private Module module;
}