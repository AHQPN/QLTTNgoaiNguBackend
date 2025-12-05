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
     */
    @Scheduled(cron = "0 1 0 * * *")
    @Transactional
    public void disableExpiredPromotions() {
        log.info("Running scheduled task: disable expired promotions");
        
        LocalDate today = LocalDate.now();
        List<Promotion> allPromotions = promotionRepository.findAll();
        
        int disabledCount = 0;
        for (Promotion promotion : allPromotions) {
            // Nếu hết hạn và vẫn đang active → tắt đi
            if (Boolean.TRUE.equals(promotion.getActive())) {
                if (today.isAfter(promotion.getEndDate())) {
                    promotion.setActive(false);
                    promotionRepository.save(promotion);
                    disabledCount++;
                    log.info("Auto-disabled expired promotion: {} - {} (expired on {})", 
                            promotion.getId(), promotion.getName(), promotion.getEndDate());
                }
            }
        }
        
        log.info("Completed scheduled task: disabled {} expired promotions", disabledCount);
    }

    /**
     * Chạy mỗi ngày lúc 00:01 để tự động kích hoạt các khuyến mãi đến hạn
     */
    @Scheduled(cron = "0 1 0 * * *")
    @Transactional
    public void activateScheduledPromotions() {
        log.info("Running scheduled task: activate scheduled promotions");
        
        LocalDate today = LocalDate.now();
        List<Promotion> allPromotions = promotionRepository.findAll();
        
        int activatedCount = 0;
        for (Promotion promotion : allPromotions) {
            // Nếu đã đến ngày bắt đầu, chưa hết hạn, và đang tắt → bật lên
            if (Boolean.FALSE.equals(promotion.getActive())) {
                if (!today.isBefore(promotion.getStartDate()) && !today.isAfter(promotion.getEndDate())) {
                    promotion.setActive(true);
                    promotionRepository.save(promotion);
                    activatedCount++;
                    log.info("Auto-activated scheduled promotion: {} - {} (started on {})", 
                            promotion.getId(), promotion.getName(), promotion.getStartDate());
                }
            }
        }
        
        log.info("Completed scheduled task: activated {} scheduled promotions", activatedCount);
    }
}
