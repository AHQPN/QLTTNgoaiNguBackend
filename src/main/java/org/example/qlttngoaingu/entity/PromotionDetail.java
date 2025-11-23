package org.example.qlttngoaingu.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "chitietkhuyenmai")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromotionDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "machitietkm")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "khuyenmai_id", referencedColumnName = "makm")
    private Promotion promotion;

    @ManyToOne
    @JoinColumn(name = "khoahoc_id", referencedColumnName = "makhoahoc")
    private Course course;

}
