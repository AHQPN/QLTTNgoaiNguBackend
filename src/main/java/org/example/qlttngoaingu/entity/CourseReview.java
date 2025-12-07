package org.example.qlttngoaingu.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * Entity cho bảng danhgia (Course Review / Đánh giá khóa học)
 */
@Entity
@Data
@Table(name = "danhgia")
public class CourseReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "madanhgia")
    private Integer reviewId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "macthd", referencedColumnName = "macthd", nullable = false)
    private InvoiceDetail enrollment;

    @Column(name = "nhanxet", columnDefinition = "NVARCHAR(MAX)")
    private String comment;

    @Column(name = "diemgiangvien")
    private Integer teacherRating;

    @Column(name = "diemcosovatchat")
    private Integer facilityRating;

    @Column(name = "diemhailong")
    private Integer overallRating;

    @Column(name = "ngaylap")
    private LocalDateTime createdAt;
}
