package org.example.qlttngoaingu.repository;

import org.example.qlttngoaingu.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Integer> {

    // Tìm tất cả khuyến mãi đang hoạt động trong thời gian hiện tại
    @Query("SELECT p FROM Promotion p WHERE p.active = true AND :today BETWEEN p.startDate AND p.endDate")
    List<Promotion> findAllActivePromotions(@Param("today") LocalDate today);

    @Query("SELECT pd.promotion FROM PromotionDetail pd " +
            "WHERE pd.course.courseId = :courseId " +
            "AND pd.promotion.active = true " +
            "AND pd.promotion.promotionType.id = 1 " +
            "AND :today >= pd.promotion.startDate " +
            "AND :today <= pd.promotion.endDate")
    List<Promotion> findValidPromotionsByCourseAndType1(
            @Param("courseId") Integer courseId,
            @Param("today") LocalDate today
    );
}