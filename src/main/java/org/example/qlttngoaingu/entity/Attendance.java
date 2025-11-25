package org.example.qlttngoaingu.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "diemdanh")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "madiemdanh")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "mahocvien", referencedColumnName = "mahocvien")
    private Student student;

    @ManyToOne
    @JoinColumn(name = "mabuoihoc", referencedColumnName = "mabuoihoc")
    private Session session;

    @Column(name = "vang")
    private Boolean absent;

    @Column(name = "ghichu")
    private String note;
}
