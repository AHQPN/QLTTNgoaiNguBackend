package org.example.qlttngoaingu.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity cho bảng phieudiem (Grade Sheet / Phiếu điểm)
 * Lưu điểm số của học viên theo từng loại đánh giá (chuyên cần, giữa kỳ, cuối kỳ)
 */
@Entity
@Data
@Table(name = "phieudiem")
public class GradeSheet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "maphieu")
    private Integer gradeSheetId;

    /**
     * Link tới chi tiết hóa đơn (enrollment của học viên vào lớp)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "macthd", referencedColumnName = "macthd", nullable = false)
    private InvoiceDetail enrollment;

    /**
     * Điểm số (0-10)
     */
    @Column(name = "diem", precision = 5, scale = 2)
    private BigDecimal score;

    /**
     * Nhận xét / ghi chú
     */
    @Column(name = "ghichu", length = 255)
    private String comment;

    /**
     * Ngày lập phiếu điểm
     */
    @Column(name = "ngaylap")
    private LocalDateTime gradedAt;

    /**
     * Loại điểm: "Chuyên cần", "Giữa kỳ", "Cuối kỳ"
     */
    @Column(name = "loaidiem", length = 50)
    private String gradeType;
}
