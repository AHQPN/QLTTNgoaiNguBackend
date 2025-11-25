package org.example.qlttngoaingu.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "khuyenmai")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "makm")
    private Integer id;

    @Column(name = "ten")
    private String name;

    @Column(name = "trangthai")
    private Boolean active;

    @Column(name = "mota")
    private String description;

    @Column(name = "phantramgiam")
    private Integer discountPercent;

    @Column(name = "ngaybatdau")
    private LocalDate startDate;

    @Column(name = "ngayketthuc")
    private LocalDate endDate;

    @ManyToOne
    @JoinColumn(name = "loaikhuyenmai", referencedColumnName = "maloaikm")
    private PromotionType promotionType;
}
