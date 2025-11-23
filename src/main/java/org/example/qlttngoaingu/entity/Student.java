package org.example.qlttngoaingu.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
@Table(name = "hocvien")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mahocvien")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manguoidung", referencedColumnName = "manguoidung", nullable = false)
    private User account;

    @Column(name = "hoten", length = 100)
    private String name;

    @Column(name = "ngaysinh")
    private LocalDate ngaySinh;

    @Column(name = "gioitinh")
    private Boolean gender;

    @Column(name = "diachi", length = 255)
    private String address;

    @Column(name = "nghenghiep", length = 100)
    private String job;

    @Column(name = "trinhdo", length = 50)
    private String level;

    @Column(name = "anhdaidien", length = 255)
    private String avatar;

}
