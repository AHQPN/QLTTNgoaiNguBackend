package org.example.qlttngoaingu.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime; // Dùng cái này thay vì LocalDate
import java.util.List;

@Entity
@Data
@Table(name = "hoadon")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mahoadon")
    private Integer invoiceId;


    @Column(name = "ngaytao")
    private LocalDateTime dateCreated;

    @Column(name = "tongtien", precision = 15, scale = 2)
    private BigDecimal totalAmount;


    @Column(name = "trangthai")
    private Boolean status;


    @ManyToOne
    @JoinColumn(name = "phuongthuc_id")
    private PaymentMethod paymentMethod;

    @ManyToOne
    @JoinColumn(name = "mahocvien")
    private Student student;


    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude // Tránh vòng lặp vô hạn khi log
    private List<InvoiceDetail> details;
}