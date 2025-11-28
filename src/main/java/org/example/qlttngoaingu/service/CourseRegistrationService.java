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
import java.util.HashSet;
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
        BigDecimal totalOriginalPrice = BigDecimal.ZERO;

        // CHỈ CỘNG TIỀN GIẢM, KHÔNG CỘNG %
        BigDecimal totalCourseDiscount = BigDecimal.ZERO;
        BigDecimal totalComboDiscount = BigDecimal.ZERO;
        BigDecimal totalReturningDiscount = BigDecimal.ZERO;

        // 4. Xử lý từng dòng chi tiết (CHỈ TÍNH TYPE 2 VÀ TYPE 3)
        for (CourseClass courseClass : selectedClasses) {
            Course course = courseClass.getCourse();
            BigDecimal originalPrice = BigDecimal.valueOf(course.getTuitionFee());

            // 3 LOẠI % giảm giá
            int courseDiscountPercent = 0;      // Type 1: Khóa học lẻ
            int comboDiscountPercent = 0;       // Type 2: Combo
            int returningDiscountPercent = 0;   // Type 3: HV cũ

            // Duyệt qua các khuyến mãi đang active
            for (Promotion promo : activePromotions) {
                int typeId = promo.getPromotionType().getId();

                List<Integer> promoCourseIds = promotionDetailRepository.findByPromotion(promo)
                        .stream()
                        .map(pd -> pd.getCourse().getCourseId())
                        .toList();

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

                // Cộng dồn vào đúng loại
                if (isApplied) {
                    if (typeId == 1) {
                        courseDiscountPercent += promo.getDiscountPercent();
                    } else if (typeId == 2) {
                        comboDiscountPercent += promo.getDiscountPercent();
                    } else if (typeId == 3) {
                        returningDiscountPercent += promo.getDiscountPercent();
                    }
                }
            }

            // Giới hạn mỗi loại max 100%
            courseDiscountPercent = Math.min(courseDiscountPercent, 100);
            comboDiscountPercent = Math.min(comboDiscountPercent, 100);
            returningDiscountPercent = Math.min(returningDiscountPercent, 100);

            // Tính tổng % (có thể > 100, sẽ giới hạn sau)
            int totalDiscountPercent = courseDiscountPercent + comboDiscountPercent + returningDiscountPercent;
            totalDiscountPercent = Math.min(totalDiscountPercent, 100);

            // Tính từng loại tiền giảm
            BigDecimal courseDiscountAmount = originalPrice
                    .multiply(BigDecimal.valueOf(courseDiscountPercent))
                    .divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);

            BigDecimal comboDiscountAmount = originalPrice
                    .multiply(BigDecimal.valueOf(comboDiscountPercent))
                    .divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);

            BigDecimal returningDiscountAmount = originalPrice
                    .multiply(BigDecimal.valueOf(returningDiscountPercent))
                    .divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);

            BigDecimal totalDiscountAmount = courseDiscountAmount
                    .add(comboDiscountAmount)
                    .add(returningDiscountAmount);

            // Đảm bảo không giảm quá giá gốc
            if (totalDiscountAmount.compareTo(originalPrice) > 0) {
                totalDiscountAmount = originalPrice;
            }

            BigDecimal finalAmount = originalPrice.subtract(totalDiscountAmount);

            // 5. Tạo InvoiceDetail (Entity)
            InvoiceDetail detail = new InvoiceDetail();
            detail.setInvoice(invoice);
            detail.setCourseClass(courseClass);
            detail.setAmount(finalAmount);
            details.add(detail);

            // 6. Cộng dồn tổng
            totalOriginalPrice = totalOriginalPrice.add(originalPrice);

            // CHỈ CỘNG TIỀN GIẢM (không cộng %)
            totalCourseDiscount = totalCourseDiscount.add(courseDiscountAmount);
            totalComboDiscount = totalComboDiscount.add(comboDiscountAmount);
            totalReturningDiscount = totalReturningDiscount.add(returningDiscountAmount);

            totalAmount = totalAmount.add(finalAmount);
        }

        invoice.setTotalAmount(totalAmount);
        invoice.setDetails(details);
        Invoice savedInvoice = invoiceRepository.save(invoice);

        // 7. Map sang Response (sử dụng mapper có sẵn)
        InvoiceResponse response = invoiceMapper.toInvoiceResponse(savedInvoice);

        // 8. Tính % TRUNG BÌNH trên tổng giá gốc (cho mục đích hiển thị)
        BigDecimal grandTotalDiscountAmount = totalCourseDiscount
                .add(totalComboDiscount)
                .add(totalReturningDiscount);

        // Tính % tổng thể = (Tổng tiền giảm / Tổng giá gốc) × 100
        int totalDiscountPercent = 0;
        int courseDiscountPercent = 0;
        int comboDiscountPercent = 0;
        int returningDiscountPercent = 0;

        if (totalOriginalPrice.compareTo(BigDecimal.ZERO) > 0) {
            totalDiscountPercent = grandTotalDiscountAmount
                    .multiply(BigDecimal.valueOf(100))
                    .divide(totalOriginalPrice, 0, BigDecimal.ROUND_HALF_UP)
                    .intValue();

            courseDiscountPercent = totalCourseDiscount
                    .multiply(BigDecimal.valueOf(100))
                    .divide(totalOriginalPrice, 0, BigDecimal.ROUND_HALF_UP)
                    .intValue();

            comboDiscountPercent = totalComboDiscount
                    .multiply(BigDecimal.valueOf(100))
                    .divide(totalOriginalPrice, 0, BigDecimal.ROUND_HALF_UP)
                    .intValue();

            returningDiscountPercent = totalReturningDiscount
                    .multiply(BigDecimal.valueOf(100))
                    .divide(totalOriginalPrice, 0, BigDecimal.ROUND_HALF_UP)
                    .intValue();
        }

        // 9. Set thông tin giảm giá chi tiết
        response.setTotalOriginalPrice(totalOriginalPrice);

        response.setCourseDiscountPercent(courseDiscountPercent);
        response.setCourseDiscountAmount(totalCourseDiscount);

        response.setComboDiscountPercent(comboDiscountPercent);
        response.setComboDiscountAmount(totalComboDiscount);

        response.setReturningDiscountPercent(returningDiscountPercent);
        response.setReturningDiscountAmount(totalReturningDiscount);

        response.setTotalDiscountPercent(totalDiscountPercent);
        response.setTotalDiscountAmount(grandTotalDiscountAmount);

        return response;
    }



}