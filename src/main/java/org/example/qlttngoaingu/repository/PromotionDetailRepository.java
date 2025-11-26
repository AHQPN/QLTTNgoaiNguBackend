package org.example.qlttngoaingu.repository;

import org.example.qlttngoaingu.entity.Promotion;
import org.example.qlttngoaingu.entity.PromotionDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromotionDetailRepository extends JpaRepository<PromotionDetail, Integer> {
    // Tìm danh sách chi tiết theo khuyến mãi
    List<PromotionDetail> findByPromotion(Promotion promotion);

    List<PromotionDetail> findByCourse_CourseId(Integer courseId);
}
