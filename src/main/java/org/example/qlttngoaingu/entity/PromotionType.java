package org.example.qlttngoaingu.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "loaikhuyenmai")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromotionType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "maloaikm")
    private Integer id;

    @Column(name = "ten")
    private String name;

    @Column(name = "mota")
    private String description;
}
