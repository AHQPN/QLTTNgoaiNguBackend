package org.example.qlttngoaingu.entity;


import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@Table(name = "hoadon")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mahoadon")
    private Integer invoiceId;

    @Column(name = "ngaylap")
    private LocalDate dateCreated;

    @Column(name = "tongtiensp", precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "trangthai", length = 50)
    private String status;

    @Column(name = "phuongthuctt", length = 100)
    private String paymentMethod;

    @ManyToOne
    @JoinColumn(name="mahocvien")
    private Student student;

    // ---- One-to-Many: 1 hóa đơn có nhiều dòng chi tiết ----
    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InvoiceDetail> details;
}
