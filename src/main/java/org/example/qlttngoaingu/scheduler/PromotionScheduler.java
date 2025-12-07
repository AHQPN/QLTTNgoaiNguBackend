package org.example.qlttngoaingu.scheduler;

import java.time.LocalDate;
import java.util.List;

import org.example.qlttngoaingu.entity.Promotion;
import org.example.qlttngoaingu.repository.PromotionRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Scheduler tự động cập nhật trạng thái khuyến mãi hết hạn
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PromotionScheduler {

    private final PromotionRepository promotionRepository;

    /**
     * Chạy mỗi ngày lúc 00:01 để kiểm tra và tắt các khuyến mãi đã hết hạn
     * Cron format: second minute hour day month weekday
     * Logic: Chỉ tắt các promotion đang ACTIVE và ĐÃ HẾT HẠN
     */
    @Scheduled(cron = "0 1 0 * * *")
    @Transactional
    public void disableExpiredPromotions() {
        log.info("Running scheduled task: disable expired promotions");
        
        LocalDate today = LocalDate.now();
        List<Promotion> activePromotions = promotionRepository.findAll().stream()
                .filter(p -> Boolean.TRUE.equals(p.getActive()))
                .filter(p -> today.isAfter(p.getEndDate())) // Chỉ lấy những cái HẾT HẠN
                .toList();
        
        int disabledCount = 0;
        for (Promotion promotion : activePromotions) {
            promotion.setActive(false);
            promotionRepository.save(promotion);
            disabledCount++;
            log.info("Auto-disabled expired promotion: {} - {} (expired on {})", 
                    promotion.getId(), promotion.getName(), promotion.getEndDate());
        }
        
        log.info("Completed scheduled task: disabled {} expired promotions", disabledCount);
    }

    /**
     * Chạy mỗi ngày lúc 00:01 để tự động kích hoạt các khuyến mãi đến hạn
     * Logic thông minh: CHỈ bật các promotion có startDate = hôm nay
     * → Nếu promotion đã qua startDate mà vẫn inactive → là do người dùng TẮT THỦ CÔNG
     * → KHÔNG tự động bật lại!
     */
    @Scheduled(cron = "0 1 0 * * *")
    @Transactional
    public void activateScheduledPromotions() {
        log.info("Running scheduled task: activate scheduled promotions");
        
        LocalDate today = LocalDate.now();
        List<Promotion> promotionsToActivate = promotionRepository.findAll().stream()
                .filter(p -> Boolean.FALSE.equals(p.getActive()) || p.getActive() == null)
                .filter(p -> today.equals(p.getStartDate())) // CHỈ bật vào đúng ngày bắt đầu
                .filter(p -> !today.isAfter(p.getEndDate())) // Và chưa hết hạn
                .toList();
        
        int activatedCount = 0;
        for (Promotion promotion : promotionsToActivate) {
            promotion.setActive(true);
            promotionRepository.save(promotion);
            activatedCount++;
            log.info("Auto-activated scheduled promotion: {} - {} (started on {})", 
                    promotion.getId(), promotion.getName(), promotion.getStartDate());
        }
        
        log.info("Completed scheduled task: activated {} scheduled promotions", activatedCount);
    }
}
