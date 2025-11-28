package org.example.qlttngoaingu.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "chitiethoadonkhuyenmai")
public class InvoiceDetailPromotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "macthd_km")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "macthd", nullable = false, foreignKey = @ForeignKey(name = "fk_cthdkm_cthd"))
    private InvoiceDetail invoiceDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "makhuyenmai", nullable = false, foreignKey = @ForeignKey(name = "fk_cthdkm_khuyenmai"))
    private Promotion promotion;

    @Column(name = "giatrigiam", nullable = false, precision = 15, scale = 2)
    private BigDecimal discountValue;
}