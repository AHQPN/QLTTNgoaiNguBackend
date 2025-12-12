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
 * Scheduler tự động tắt các khuyến mãi đã hết hạn
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
}
