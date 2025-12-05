package org.example.qlttngoaingu.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Entity cho bảng danhgia (Course Review / Đánh giá khóa học)
 * Học viên đánh giá khóa học sau khi hoàn thành
 */
@Entity
@Data
@Table(name = "danhgia")
public class CourseReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "madanhgia")
    private Integer reviewId;

    /**
     * Link tới chi tiết hóa đơn (enrollment của học viên vào lớp)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "macthd", referencedColumnName = "macthd", nullable = false)
    private InvoiceDetail enrollment;

    /**
     * Nhận xét văn bản
     */
    @Column(name = "nhanxet", columnDefinition = "NVARCHAR(MAX)")
    private String comment;

    /**
     * Điểm đánh giá giảng viên (1-5 sao)
     */
    @Column(name = "diemgiangvien")
    private Integer teacherRating;

    /**
     * Điểm đánh giá cơ sở vật chất (1-5 sao)
     */
    @Column(name = "diemcosovatchat")
    private Integer facilityRating;

    /**
     * Điểm hài lòng tổng thể (1-5 sao)
     */
    @Column(name = "diemhailong")
    private Integer overallRating;

    /**
     * Ngày lập đánh giá
     */
    @Column(name = "ngaylap")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
