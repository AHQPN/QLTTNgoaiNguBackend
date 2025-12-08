package org.example.qlttngoaingu.entity;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "phong")
@Data
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "maphong")
    private Integer roomId;

    @Column(name = "tenphong", length = 255)
    private String roomName;

    @Column(name = "succhua")
    private Integer capacity;

    /**
     * Trạng thái phòng - chỉ có 2 giá trị:
     * - "Sẵn sàng": Phòng có thể xếp lớp học
     * - "Bảo trì": Phòng đang sửa chữa, không thể sử dụng
     */
    @Column(name = "trangthai", length = 50)
    private String status;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CourseClass> classes;

    // Getters and Setters (omitted for brevity)
}