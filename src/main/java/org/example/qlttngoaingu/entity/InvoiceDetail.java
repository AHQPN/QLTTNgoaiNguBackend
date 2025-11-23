package org.example.qlttngoaingu.entity;


import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Data
@Table(name = "chitiethoadon")
public class InvoiceDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "macthd")
    private Integer detailId;

    // ---- Many-to-One: thuộc hóa đơn nào ----
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hoadon_id", referencedColumnName = "mahoadon")
    private Invoice invoice;

    // Số tiền / học phí dòng này
    @Column(name = "giaban", precision = 15, scale = 2)
    private BigDecimal amount;

    // ---- Many-to-One: lớp học mà học viên đăng ký ----
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "malophoc", referencedColumnName = "malop")
    private CourseClass courseClass;
}
