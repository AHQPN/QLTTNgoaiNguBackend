package org.example.qlttngoaingu.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "diemdanh")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "madiemdanh")
    private Integer id;

    // Liên kết với chi tiết hóa đơn (trong chi tiết hóa đơn có thông tin học viên)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "macthd", referencedColumnName = "macthd", nullable = false)
    private InvoiceDetail invoiceDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mabuoihoc", referencedColumnName = "mabuoihoc", nullable = false)
    private Session session;

    @Column(name = "vang")
    private Boolean absent;

    @Column(name = "ghichu")
    private String note;


}
