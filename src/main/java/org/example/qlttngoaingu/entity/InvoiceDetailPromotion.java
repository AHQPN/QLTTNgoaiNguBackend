package org.example.qlttngoaingu.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chitiethoadonkhuyenmai")
@Data
@NoArgsConstructor
@AllArgsConstructor
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