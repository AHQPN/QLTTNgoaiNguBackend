package org.example.qlttngoaingu.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.qlttngoaingu.dto.request.CourseRegistrationRequest;
import org.example.qlttngoaingu.dto.response.InvoiceResponse;
import org.example.qlttngoaingu.entity.*;
import org.example.qlttngoaingu.mapper.InvoiceMapper;
import org.example.qlttngoaingu.repository.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseRegistrationService {

    private final InvoiceRepository invoiceRepository;
    private final StudentRepository studentRepository;
    private final CourseClassRepository courseClassRepository;
    private final PaymentMethodRepository paymentMethodRepository;

    private final PromotionRepository promotionRepository;
    private final PromotionDetailRepository promotionDetailRepository;
    private final InvoiceMapper  invoiceMapper;

    @Transactional
    public InvoiceResponse registerCourses(CourseRegistrationRequest request) {

        // 1. Kiểm tra thông tin cơ bản
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy học viên: " + request.getStudentId()));

        PaymentMethod paymentMethod = paymentMethodRepository.findById(request.getPaymentMethodId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy PT thanh toán: " + request.getPaymentMethodId()));

        // 2. Chuẩn bị dữ liệu tính toán
        Boolean isReturningStudent = invoiceRepository.existsByStudentAndStatus(student, true);

        List<CourseClass> selectedClasses = courseClassRepository.findAllById(request.getClassIds());
        if (selectedClasses.size() != request.getClassIds().size()) {
            throw new RuntimeException("Một số lớp học không tồn tại.");
        }

        List<Integer> selectedCourseIds = selectedClasses.stream()
                .map(clazz -> clazz.getCourse().getCourseId())
                .toList();

        List<Promotion> activePromotions = promotionRepository.findAllActivePromotions(LocalDate.now());

        // 3. Khởi tạo Hóa đơn
        Invoice invoice = new Invoice();
        invoice.setStudent(student);
        invoice.setPaymentMethod(paymentMethod);
        invoice.setDateCreated(LocalDateTime.now());
        invoice.setStatus(false);

        List<InvoiceDetail> details = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        // 4. Xử lý từng dòng chi tiết (Cộng dồn khuyến mãi)
        for (CourseClass courseClass : selectedClasses) {
            Course course = courseClass.getCourse();

            BigDecimal originalPrice = BigDecimal.valueOf(course.getTuitionFee());

            // --- THAY ĐỔI LOGIC TẠI ĐÂY: CỘNG DỒN ---
            int totalDiscountPercent = 0; // Đổi tên biến cho rõ nghĩa

            for (Promotion promo : activePromotions) {
                // Lấy danh sách khóa học trong khuyến mãi này
                List<Integer> promoCourseIds = promotionDetailRepository.findByPromotion(promo)
                        .stream()
                        .map(pd -> pd.getCourse().getCourseId())
                        .toList();

                int typeId = promo.getPromotionType().getId();
                boolean isApplied = false;

                // CHECK LOẠI 1: Khuyến mãi khóa học lẻ
                if (typeId == 1) {
                    if (promoCourseIds.contains(course.getCourseId())) {
                        isApplied = true;
                    }
                }

                // CHECK LOẠI 2: Khuyến mãi Combo
                else if (typeId == 2) {
                    if (promoCourseIds.contains(course.getCourseId()) &&
                            selectedCourseIds.containsAll(promoCourseIds)) {
                        isApplied = true;
                    }
                }

                // CHECK LOẠI 3: Khuyến mãi Học viên cũ
                else if (typeId == 3) {
                    if (isReturningStudent) {
                        isApplied = true;
                    }
                }

                // NẾU THỎA MÃN ĐIỀU KIỆN Thì CỘNG DỒN %
                if (isApplied) {
                    totalDiscountPercent += promo.getDiscountPercent();
                }
            }

            //  QUAN TRỌNG: GIỚI HẠN MAX 100%
            // Tránh trường hợp cộng dồn quá 100%(ví dụ: 50% + 50% + 10% = 110% -> Âm tiền)
            totalDiscountPercent = Math.min(totalDiscountPercent, 100);

            // 5. Tính giá sau cùng
            BigDecimal finalAmount = originalPrice;
            if (totalDiscountPercent > 0) {
                BigDecimal discountAmount = originalPrice
                        .multiply(BigDecimal.valueOf(totalDiscountPercent))
                        .divide(BigDecimal.valueOf(100));
                finalAmount = originalPrice.subtract(discountAmount);
            }

            // 6. Tạo chi tiết hóa đơn
            InvoiceDetail detail = new InvoiceDetail();
            detail.setInvoice(invoice);
            detail.setCourseClass(courseClass);
            detail.setAmount(finalAmount);

            details.add(detail);
            totalAmount = totalAmount.add(finalAmount);
        }

        invoice.setTotalAmount(totalAmount);
        invoice.setDetails(details);

        Invoice savedInvoice = invoiceRepository.save(invoice);
        return invoiceMapper.toInvoiceResponse(savedInvoice);
    }
}