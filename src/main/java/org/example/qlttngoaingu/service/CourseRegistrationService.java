package org.example.qlttngoaingu.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.example.qlttngoaingu.dto.request.CourseRegistrationRequest;
import org.example.qlttngoaingu.dto.response.InvoiceResponse;
import org.example.qlttngoaingu.entity.Course;
import org.example.qlttngoaingu.entity.CourseClass;
import org.example.qlttngoaingu.entity.Invoice;
import org.example.qlttngoaingu.entity.InvoiceDetail;
import org.example.qlttngoaingu.entity.InvoiceDetailPromotion;
import org.example.qlttngoaingu.entity.PaymentMethod;
import org.example.qlttngoaingu.entity.Promotion;
import org.example.qlttngoaingu.entity.Student;
import org.example.qlttngoaingu.mapper.InvoiceMapper;
import org.example.qlttngoaingu.repository.CourseClassRepository;
import org.example.qlttngoaingu.repository.InvoiceDetailPromotionRepository;
import org.example.qlttngoaingu.repository.InvoiceDetailRepository;
import org.example.qlttngoaingu.repository.InvoiceRepository;
import org.example.qlttngoaingu.repository.PaymentMethodRepository;
import org.example.qlttngoaingu.repository.PromotionDetailRepository;
import org.example.qlttngoaingu.repository.PromotionRepository;
import org.example.qlttngoaingu.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseRegistrationService {

    private final InvoiceRepository invoiceRepository;
    private final StudentRepository studentRepository;
    private final CourseClassRepository courseClassRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final InvoiceDetailRepository invoiceDetailRepository;

    private final PromotionRepository promotionRepository;
    private final PromotionDetailRepository promotionDetailRepository;
    private final InvoiceDetailPromotionRepository invoiceDetailPromotionRepository;
    private final InvoiceMapper invoiceMapper;

    /**
     * Helper class để lưu thông tin combo promotion
     */
    private static class ComboInfo {
        Promotion promotion;
        List<Integer> courseIds;
        BigDecimal totalDiscount; // Không dùng nữa nhưng giữ lại cho tương thích

        ComboInfo(Promotion promotion, List<Integer> courseIds, BigDecimal totalDiscount) {
            this.promotion = promotion;
            this.courseIds = courseIds;
            this.totalDiscount = totalDiscount;
        }
    }

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

        // 2.1. Kiểm tra sĩ số lớp học (so với sức chứa phòng)
        for (CourseClass courseClass : selectedClasses) {
            if (courseClass.getRoom() != null && courseClass.getRoom().getCapacity() != null) {
                Integer currentEnrollment = invoiceDetailRepository.countByClassIdAndActiveInvoice(courseClass.getClassId());
                Integer roomCapacity = courseClass.getRoom().getCapacity();
                
                if (currentEnrollment >= roomCapacity) {
                    throw new RuntimeException(String.format(
                            "Lớp '%s' đã đủ sĩ số (%d/%d học viên). Không thể đăng ký thêm.",
                            courseClass.getClassName(),
                            currentEnrollment,
                            roomCapacity
                    ));
                }
            }
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

        BigDecimal totalOriginalPrice = BigDecimal.ZERO;
        BigDecimal totalAfterCourseDiscount = BigDecimal.ZERO; // Tổng sau khi giảm Type 1

        // CHỈ CỘNG TIỀN GIẢM
        BigDecimal totalCourseDiscount = BigDecimal.ZERO; // Type 1: Giảm từng khóa
        
        // Map lưu các promotion Type 1 đã áp dụng cho từng detail
        Map<InvoiceDetail, List<Promotion>> detailType1Promotions = new HashMap<>();

        // 4. Bước 1: Xử lý Type 1 - Giảm giá từng khóa học
        for (CourseClass courseClass : selectedClasses) {
            Course course = courseClass.getCourse();
            BigDecimal originalPrice = BigDecimal.valueOf(course.getTuitionFee());

            int courseDiscountPercent = 0; // Type 1: Khóa học lẻ
            List<Promotion> appliedType1Promos = new ArrayList<>();

            // Duyệt qua các khuyến mãi Type 1
            for (Promotion promo : activePromotions) {
                if (promo.getPromotionType().getId() == 1) {
                    List<Integer> promoCourseIds = promotionDetailRepository.findByPromotion(promo)
                            .stream()
                            .map(pd -> pd.getCourse().getCourseId())
                            .toList();

                    if (promoCourseIds.contains(course.getCourseId())) {
                        courseDiscountPercent += promo.getDiscountPercent();
                        appliedType1Promos.add(promo);
                    }
                }
            }

            // Giới hạn max 100%
            courseDiscountPercent = Math.min(courseDiscountPercent, 100);

            // Tính tiền giảm Type 1
            BigDecimal courseDiscountAmount = originalPrice
                    .multiply(BigDecimal.valueOf(courseDiscountPercent))
                    .divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);

            BigDecimal priceAfterCourseDiscount = originalPrice.subtract(courseDiscountAmount);

            // Tạo InvoiceDetail
            InvoiceDetail detail = new InvoiceDetail();
            detail.setInvoice(invoice);
            detail.setCourseClass(courseClass);
            detail.setAmount(priceAfterCourseDiscount); // Tạm thời, sẽ cập nhật sau khi tính Type 2 & 3
            details.add(detail);

            // Lưu promotion Type 1 đã áp dụng
            if (!appliedType1Promos.isEmpty()) {
                detailType1Promotions.put(detail, appliedType1Promos);
            }

            // Cộng dồn
            totalOriginalPrice = totalOriginalPrice.add(originalPrice);
            totalCourseDiscount = totalCourseDiscount.add(courseDiscountAmount);
            totalAfterCourseDiscount = totalAfterCourseDiscount.add(priceAfterCourseDiscount);
        }

        // 5. Bước 2: Xử lý Type 2 - Mỗi khóa chọn combo có % giảm cao nhất
        BigDecimal totalComboDiscount = BigDecimal.ZERO;
        List<Promotion> appliedType2Promos = new ArrayList<>();
        
        // Map để lưu combo nào áp dụng cho khóa nào
        Map<Integer, Promotion> courseComboMap = new HashMap<>();
        
        // Map để tra cứu giá sau Type 1 của từng khóa
        Map<Integer, BigDecimal> coursePriceMap = new HashMap<>();
        for (InvoiceDetail detail : details) {
            Integer courseId = detail.getCourseClass().getCourse().getCourseId();
            coursePriceMap.put(courseId, detail.getAmount());
        }

        // Tìm tất cả combo khả dụng
        List<ComboInfo> eligibleCombos = new ArrayList<>();
        
        for (Promotion promo : activePromotions) {
            if (promo.getPromotionType().getId() == 2) {
                List<Integer> promoCourseIds = promotionDetailRepository.findByPromotion(promo)
                        .stream()
                        .map(pd -> pd.getCourse().getCourseId())
                        .toList();

                // Check combo: tất cả khóa trong promo phải có trong selectedCourseIds
                if (selectedCourseIds.containsAll(promoCourseIds) && !promoCourseIds.isEmpty()) {
                    eligibleCombos.add(new ComboInfo(promo, promoCourseIds, BigDecimal.ZERO));
                    
                    log.info("Combo available: {} ({}%) for courses {}", 
                            promo.getName(), 
                            promo.getDiscountPercent(), 
                            promoCourseIds);
                }
            }
        }

        // Map: courseId -> combo có % giảm cao nhất cho khóa đó
        Map<Integer, Promotion> bestComboPerCourse = new HashMap<>();
        
        for (Integer courseId : selectedCourseIds) {
            Promotion bestPromo = null;
            int maxPercent = 0;
            
            // Tìm combo có % giảm cao nhất chứa khóa này
            for (ComboInfo combo : eligibleCombos) {
                if (combo.courseIds.contains(courseId)) {
                    if (combo.promotion.getDiscountPercent() > maxPercent) {
                        maxPercent = combo.promotion.getDiscountPercent();
                        bestPromo = combo.promotion;
                    }
                }
            }
            
            if (bestPromo != null) {
                bestComboPerCourse.put(courseId, bestPromo);
                log.info("Course {} chose combo: {} ({}%)", 
                        courseId, 
                        bestPromo.getName(), 
                        bestPromo.getDiscountPercent());
            }
        }

        // Tính tổng tiền giảm và lưu mapping
        java.util.Set<Promotion> countedCombos = new java.util.HashSet<>();
        
        for (Map.Entry<Integer, Promotion> entry : bestComboPerCourse.entrySet()) {
            Integer courseId = entry.getKey();
            Promotion combo = entry.getValue();
            
            courseComboMap.put(courseId, combo);
            
            // Chỉ tính tiền giảm 1 lần cho mỗi combo (tránh tính trùng)
            if (!countedCombos.contains(combo)) {
                // Lấy danh sách khóa trong combo
                List<Integer> comboCourseIds = eligibleCombos.stream()
                        .filter(c -> c.promotion.equals(combo))
                        .findFirst()
                        .map(c -> c.courseIds)
                        .orElse(new ArrayList<>());
                
                // Tính tiền giảm cho combo
                BigDecimal comboBaseAmount = comboCourseIds.stream()
                        .map(coursePriceMap::get)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                BigDecimal comboDiscount = comboBaseAmount
                        .multiply(BigDecimal.valueOf(combo.getDiscountPercent()))
                        .divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);
                
                totalComboDiscount = totalComboDiscount.add(comboDiscount);
                appliedType2Promos.add(combo);
                countedCombos.add(combo);
                
                log.info("APPLIED COMBO: {} ({}%) for courses: {} → Discount: {}", 
                        combo.getName(), 
                        combo.getDiscountPercent(), 
                        comboCourseIds,
                        comboDiscount);
            }
        }

        BigDecimal totalAfterComboDiscount = totalAfterCourseDiscount.subtract(totalComboDiscount);

        // 6. Bước 3: Xử lý Type 3 - Giảm HV cũ trên tổng bill sau Type 1 + Type 2
        BigDecimal totalReturningDiscount;
        BigDecimal totalAmount;
        List<Promotion> appliedType3Promos = new ArrayList<>();
        int returningDiscountPercent = 0;

        if (isReturningStudent) {
            for (Promotion promo : activePromotions) {
                if (promo.getPromotionType().getId() == 3) {
                    returningDiscountPercent += promo.getDiscountPercent();
                    appliedType3Promos.add(promo);
                }
            }
        }

        returningDiscountPercent = Math.min(returningDiscountPercent, 100);
        totalReturningDiscount = totalAfterComboDiscount
                .multiply(BigDecimal.valueOf(returningDiscountPercent))
                .divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);

        totalAmount = totalAfterComboDiscount.subtract(totalReturningDiscount);

        // 7. Cập nhật lại amount cho từng InvoiceDetail
        // Trừ combo discount (đã tính riêng cho từng khóa) và Type 3 discount (phân bổ đều)
        for (InvoiceDetail detail : details) {
            BigDecimal currentAmount = detail.getAmount(); // Giá sau Type 1
            
            // Trừ combo discount nếu khóa này có combo
            Integer courseId = detail.getCourseClass().getCourse().getCourseId();
            Promotion combo = courseComboMap.get(courseId);
            if (combo != null) {
                BigDecimal comboDiscount = currentAmount
                        .multiply(BigDecimal.valueOf(combo.getDiscountPercent()))
                        .divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);
                currentAmount = currentAmount.subtract(comboDiscount);
            }
            
            // Trừ Type 3 discount (phân bổ theo tỷ lệ)
            if (totalAfterComboDiscount.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal ratio = currentAmount.divide(totalAfterComboDiscount, 10, BigDecimal.ROUND_HALF_UP);
                BigDecimal type3Discount = totalReturningDiscount.multiply(ratio).setScale(2, BigDecimal.ROUND_HALF_UP);
                currentAmount = currentAmount.subtract(type3Discount);
            }
            
            detail.setAmount(currentAmount);
        }

        invoice.setTotalAmount(totalAmount);
        invoice.setDetails(details);
        Invoice savedInvoice = invoiceRepository.save(invoice);

        // 8. Lưu InvoiceDetailPromotion
        List<InvoiceDetailPromotion> allPromotions = new ArrayList<>();

        // 8.1. Lưu Type 1 promotions (cho từng detail)
        for (Map.Entry<InvoiceDetail, List<Promotion>> entry : detailType1Promotions.entrySet()) {
            InvoiceDetail detail = entry.getKey();
            BigDecimal originalPrice = BigDecimal.valueOf(detail.getCourseClass().getCourse().getTuitionFee());
            
            for (Promotion promo : entry.getValue()) {
                BigDecimal discountAmount = originalPrice
                        .multiply(BigDecimal.valueOf(promo.getDiscountPercent()))
                        .divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);
                
                InvoiceDetailPromotion idp = new InvoiceDetailPromotion();
                idp.setInvoiceDetail(detail);
                idp.setPromotion(promo);
                idp.setDiscountValue(discountAmount);
                allPromotions.add(idp);
            }
        }

        // 8.2. Lưu Type 2 promotions (chỉ cho các khóa có áp dụng combo)
        if (!appliedType2Promos.isEmpty()) {
            for (InvoiceDetail detail : details) {
                Integer courseId = detail.getCourseClass().getCourse().getCourseId();
                Promotion combo = courseComboMap.get(courseId);
                
                if (combo != null) {
                    // Tính discount amount cho khóa này
                    BigDecimal priceAfterType1 = detail.getAmount(); // Giá sau khi giảm Type 1
                    BigDecimal discountAmount = priceAfterType1
                            .multiply(BigDecimal.valueOf(combo.getDiscountPercent()))
                            .divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);
                    
                    InvoiceDetailPromotion idp = new InvoiceDetailPromotion();
                    idp.setInvoiceDetail(detail);
                    idp.setPromotion(combo);
                    idp.setDiscountValue(discountAmount);
                    allPromotions.add(idp);
                }
            }
        }

        // 8.3. Lưu Type 3 promotions (phân bổ cho tất cả các detail)
        if (!appliedType3Promos.isEmpty() && totalAfterComboDiscount.compareTo(BigDecimal.ZERO) > 0) {
            for (InvoiceDetail detail : details) {
                BigDecimal ratio = detail.getAmount().divide(totalAfterComboDiscount, 10, BigDecimal.ROUND_HALF_UP);
                
                for (Promotion promo : appliedType3Promos) {
                    BigDecimal discountAmount = totalReturningDiscount
                            .multiply(ratio)
                            .multiply(BigDecimal.valueOf(promo.getDiscountPercent()))
                            .divide(BigDecimal.valueOf(returningDiscountPercent), 2, BigDecimal.ROUND_HALF_UP);
                    
                    InvoiceDetailPromotion idp = new InvoiceDetailPromotion();
                    idp.setInvoiceDetail(detail);
                    idp.setPromotion(promo);
                    idp.setDiscountValue(discountAmount);
                    allPromotions.add(idp);
                }
            }
        }

        if (!allPromotions.isEmpty()) {
            invoiceDetailPromotionRepository.saveAll(allPromotions);
        }

        // 9. Map sang Response (sử dụng mapper có sẵn)
        InvoiceResponse response = invoiceMapper.toInvoiceResponse(savedInvoice);

        // 10. Tính % TRUNG BÌNH trên tổng giá gốc (cho mục đích hiển thị)
        BigDecimal grandTotalDiscountAmount = totalCourseDiscount
                .add(totalComboDiscount)
                .add(totalReturningDiscount);

        // Tính % tổng thể = (Tổng tiền giảm / Tổng giá gốc) × 100
        int totalDiscountPercent = 0;
        int courseDiscountPercentDisplay = 0;
        int comboDiscountPercentDisplay = 0;
        int returningDiscountPercentDisplay = 0;

        if (totalOriginalPrice.compareTo(BigDecimal.ZERO) > 0) {
            totalDiscountPercent = grandTotalDiscountAmount
                    .multiply(BigDecimal.valueOf(100))
                    .divide(totalOriginalPrice, 0, BigDecimal.ROUND_HALF_UP)
                    .intValue();

            courseDiscountPercentDisplay = totalCourseDiscount
                    .multiply(BigDecimal.valueOf(100))
                    .divide(totalOriginalPrice, 0, BigDecimal.ROUND_HALF_UP)
                    .intValue();

            comboDiscountPercentDisplay = totalComboDiscount
                    .multiply(BigDecimal.valueOf(100))
                    .divide(totalOriginalPrice, 0, BigDecimal.ROUND_HALF_UP)
                    .intValue();

            returningDiscountPercentDisplay = totalReturningDiscount
                    .multiply(BigDecimal.valueOf(100))
                    .divide(totalOriginalPrice, 0, BigDecimal.ROUND_HALF_UP)
                    .intValue();
        }

        // 11. Set thông tin giảm giá chi tiết
        response.setTotalOriginalPrice(totalOriginalPrice);

        response.setCourseDiscountPercent(courseDiscountPercentDisplay);
        response.setCourseDiscountAmount(totalCourseDiscount);

        response.setComboDiscountPercent(comboDiscountPercentDisplay);
        response.setComboDiscountAmount(totalComboDiscount);

        response.setReturningDiscountPercent(returningDiscountPercentDisplay);
        response.setReturningDiscountAmount(totalReturningDiscount);

        response.setTotalDiscountPercent(totalDiscountPercent);
        response.setTotalDiscountAmount(grandTotalDiscountAmount);

        return response;
    }

}