package org.example.qlttngoaingu.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.example.qlttngoaingu.entity.Invoice;
import org.example.qlttngoaingu.repository.InvoiceRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class InvoiceExpiryScheduler {

    private final InvoiceRepository invoiceRepository;
    
    private static final int PAYMENT_TIMEOUT_MINUTES = 15;

    /**
     * Tự động xóa các invoice chưa thanh toán và đã hết hạn
     * Chạy mỗi 5 phút
     */
    @Scheduled(fixedRate = 300000) // 5 phút = 300,000 ms
    @Transactional
    public void deleteExpiredInvoices() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(PAYMENT_TIMEOUT_MINUTES);
        
        // Tìm các invoice chưa thanh toán (status = false) và đã hết hạn
        List<Invoice> expiredInvoices = invoiceRepository.findByStatusFalseAndDateCreatedBefore(cutoffTime);
        
        if (!expiredInvoices.isEmpty()) {
            log.info("Found {} expired invoices to delete", expiredInvoices.size());
            
            for (Invoice invoice : expiredInvoices) {
                log.info("Deleting expired invoice: {} (created at: {}, student: {})", 
                        invoice.getInvoiceId(), 
                        invoice.getDateCreated(),
                        invoice.getStudent().getId());
            }
            
            // Xóa invoice (cascade sẽ xóa luôn InvoiceDetail và InvoiceDetailPromotion)
            invoiceRepository.deleteAll(expiredInvoices);
            
            log.info("Successfully deleted {} expired invoices", expiredInvoices.size());
        }
    }
}
