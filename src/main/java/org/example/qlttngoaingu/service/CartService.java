package org.example.qlttngoaingu.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.qlttngoaingu.dto.request.CartPreviewRequest;
import org.example.qlttngoaingu.dto.response.CartPreviewResponse;
import org.example.qlttngoaingu.entity.*;
import org.example.qlttngoaingu.exception.AppException;
import org.example.qlttngoaingu.exception.ErrorCode;
import org.example.qlttngoaingu.repository.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final CourseClassRepository courseClassRepository;
    private final UserRepository userRepository;
    private final PromotionRepository promotionRepository;
    private final PromotionDetailRepository promotionDetailRepository;
    private final InvoiceRepository invoiceRepository;
    private final StudentRepository studentRepository;

    public CartPreviewResponse previewCart(CartPreviewRequest request, Integer userId) {
        if (request.getCourseClassIds() == null || request.getCourseClassIds().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        // 1. Lấy danh sách lớp học
        List<CourseClass> selectedClasses = courseClassRepository.findAllById(request.getCourseClassIds());
        if (selectedClasses.size() != request.getCourseClassIds().size()) {
            throw new AppException(ErrorCode.CLASS_NOT_FOUND);
        }

        // 2. Check HV cũ (đã từng thanh toán thành công)
        Boolean isReturningStudent = false;
            User user = userRepository.findByUserId(userId).orElse(null);
            if (user != null) {
                Student student = studentRepository.findByAccount_UserId(user.getUserId()).orElse(null);
                if (student != null) {
                    // Check xem có invoice nào đã thanh toán (status = true) chưa
                    isReturningStudent = invoiceRepository.existsByStudentAndStatus(student, true);
                }
            }

        // 3. Lấy tất cả promotion active
        LocalDate today = LocalDate.now();
        List<Promotion> activePromotions = promotionRepository.findAll().stream()
                .filter(p -> Boolean.TRUE.equals(p.getActive()))
                .filter(p -> !today.isBefore(p.getStartDate()) && !today.isAfter(p.getEndDate()))
                .collect(Collectors.toList());

        // 4. Tính toán giá và promotion
        return calculateCartWithPromotions(selectedClasses, activePromotions, isReturningStudent);
    }

    private CartPreviewResponse calculateCartWithPromotions(
            List<CourseClass> selectedClasses,
            List<Promotion> activePromotions,
            boolean isReturningStudent) {

        CartPreviewResponse response = new CartPreviewResponse();
        List<CartPreviewResponse.CartItemResponse> items = new ArrayList<>();
        
        // Map để lưu courseId -> giá sau Type 1
        Map<Integer, BigDecimal> coursePriceMap = new HashMap<>();
        Map<Integer, CourseClass> courseClassMap = new HashMap<>();
        
        BigDecimal totalOriginalPrice = BigDecimal.ZERO;

        // === BƯỚC 1: Tính Type 1 (Khuyến mãi lẻ) cho từng khóa ===
        for (CourseClass courseClass : selectedClasses) {
            Course course = courseClass.getCourse();
            BigDecimal originalPrice = BigDecimal.valueOf(course.getTuitionFee());
            
            int courseDiscountPercent = 0;
            
            // Tìm promotion Type 1 cho khóa này
            for (Promotion promo : activePromotions) {
                if (promo.getPromotionType().getId() == 1) {
                    List<Integer> promoCourseIds = promotionDetailRepository.findByPromotion(promo)
                            .stream()
                            .map(pd -> pd.getCourse().getCourseId())
                            .toList();
                    
                    if (promoCourseIds.contains(course.getCourseId())) {
                        courseDiscountPercent = Math.max(courseDiscountPercent, promo.getDiscountPercent());
                    }
                }
            }
            
            courseDiscountPercent = Math.min(courseDiscountPercent, 100);
            
            BigDecimal discountAmount = originalPrice
                    .multiply(BigDecimal.valueOf(courseDiscountPercent))
                    .divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);
            
            BigDecimal priceAfterType1 = originalPrice.subtract(discountAmount);
            
            // Lưu vào map
            coursePriceMap.put(course.getCourseId(), priceAfterType1);
            courseClassMap.put(course.getCourseId(), courseClass);
            totalOriginalPrice = totalOriginalPrice.add(priceAfterType1);
            
            // Tạo item response
            CartPreviewResponse.CartItemResponse item = new CartPreviewResponse.CartItemResponse();
            item.setCourseClassId(courseClass.getClassId());
            item.setCourseName(course.getCourseName());
            item.setClassName(courseClass.getClassName());
            item.setOriginalPrice(priceAfterType1);
            item.setFinalPrice(priceAfterType1); // Tạm thời, sẽ update sau
            items.add(item);
        }

        // === BƯỚC 2: Tính Type 2 (Combo) ===
        List<Integer> selectedCourseIds = new ArrayList<>(coursePriceMap.keySet());
        
        // Map để lưu combo nào áp dụng cho khóa nào
        Map<Integer, Promotion> courseComboMap = new HashMap<>();
        
        // Tìm tất cả combo khả dụng
        List<ComboInfo> eligibleCombos = new ArrayList<>();
        for (Promotion promo : activePromotions) {
            if (promo.getPromotionType().getId() == 2) {
                List<Integer> promoCourseIds = promotionDetailRepository.findByPromotion(promo)
                        .stream()
                        .map(pd -> pd.getCourse().getCourseId())
                        .toList();
                
                if (selectedCourseIds.containsAll(promoCourseIds) && !promoCourseIds.isEmpty()) {
                    eligibleCombos.add(new ComboInfo(promo, promoCourseIds));
                }
            }
        }
        
        // Với mỗi khóa, tìm combo có % cao nhất
        for (Integer courseId : selectedCourseIds) {
            Promotion bestPromo = null;
            int maxPercent = 0;
            
            for (ComboInfo combo : eligibleCombos) {
                if (combo.courseIds.contains(courseId)) {
                    if (combo.promotion.getDiscountPercent() > maxPercent) {
                        maxPercent = combo.promotion.getDiscountPercent();
                        bestPromo = combo.promotion;
                    }
                }
            }
            
            if (bestPromo != null) {
                courseComboMap.put(courseId, bestPromo);
            }
        }
        
        // Tính tổng giảm combo và tạo danh sách combo đã áp dụng
        Map<Promotion, ComboApplicationInfo> appliedCombos = new HashMap<>();
        
        for (Map.Entry<Integer, Promotion> entry : courseComboMap.entrySet()) {
            Promotion combo = entry.getValue();
            
            if (!appliedCombos.containsKey(combo)) {
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
                
                List<String> courseNames = comboCourseIds.stream()
                        .map(courseClassMap::get)
                        .map(cc -> cc.getCourse().getCourseName())
                        .collect(Collectors.toList());
                
                appliedCombos.put(combo, new ComboApplicationInfo(
                        combo.getName(),
                        combo.getDiscountPercent(),
                        comboDiscount,
                        courseNames
                ));
            }
        }
        
        BigDecimal totalComboDiscount = appliedCombos.values().stream()
                .map(info -> info.discountAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // === BƯỚC 3: Tính Type 3 (HV cũ) ===
        int returningDiscountPercent = 0;
        if (isReturningStudent) {
            for (Promotion promo : activePromotions) {
                if (promo.getPromotionType().getId() == 3) {
                    returningDiscountPercent += promo.getDiscountPercent();
                }
            }
        }
        returningDiscountPercent = Math.min(returningDiscountPercent, 100);
        
        BigDecimal totalAfterCombo = totalOriginalPrice.subtract(totalComboDiscount);
        BigDecimal returningDiscountAmount = totalAfterCombo
                .multiply(BigDecimal.valueOf(returningDiscountPercent))
                .divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);

        // === BƯỚC 4: Tính tổng cuối cùng ===
        BigDecimal totalDiscountAmount = totalComboDiscount.add(returningDiscountAmount);
        BigDecimal finalAmount = totalOriginalPrice.subtract(totalDiscountAmount);

        // === BƯỚC 5: Tạo response ===
        response.setItems(items);
        
        CartPreviewResponse.CartSummaryResponse summary = new CartPreviewResponse.CartSummaryResponse();
        summary.setTotalOriginalPrice(totalOriginalPrice);
        
        List<CartPreviewResponse.ComboDiscountInfo> comboInfoList = appliedCombos.values().stream()
                .map(info -> {
                    CartPreviewResponse.ComboDiscountInfo comboInfo = new CartPreviewResponse.ComboDiscountInfo();
                    comboInfo.setComboName(info.comboName);
                    comboInfo.setDiscountPercent(info.discountPercent);
                    comboInfo.setDiscountAmount(info.discountAmount);
                    comboInfo.setCourseNames(info.courseNames);
                    return comboInfo;
                })
                .collect(Collectors.toList());
        
        summary.setAppliedCombos(comboInfoList);
        summary.setReturningDiscountAmount(returningDiscountAmount);
        summary.setTotalDiscountAmount(totalDiscountAmount);
        summary.setFinalAmount(finalAmount);
        
        response.setSummary(summary);
        
        return response;
    }

    // Helper classes
    private static class ComboInfo {
        Promotion promotion;
        List<Integer> courseIds;
        
        ComboInfo(Promotion promotion, List<Integer> courseIds) {
            this.promotion = promotion;
            this.courseIds = courseIds;
        }
    }
    
    private static class ComboApplicationInfo {
        String comboName;
        Integer discountPercent;
        BigDecimal discountAmount;
        List<String> courseNames;
        
        ComboApplicationInfo(String comboName, Integer discountPercent, 
                           BigDecimal discountAmount, List<String> courseNames) {
            this.comboName = comboName;
            this.discountPercent = discountPercent;
            this.discountAmount = discountAmount;
            this.courseNames = courseNames;
        }
    }
}
