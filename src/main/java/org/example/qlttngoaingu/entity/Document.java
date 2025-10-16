package org.example.qlttngoaingu.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tailieu")
@Getter @Setter
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "matailieu")
    private Integer documentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mamodule", nullable = false)
    @JsonIgnore
    private Module module;

    @Column(name = "tenfile", length = 200)
    private String fileName;

    @Column(name = "link", length = 255)
    private String link;

    @Column(name = "mota", columnDefinition = "TEXT")
    private String description;

    @Column(name = "hinh", length = 255)
    private String image;
}