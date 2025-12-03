package org.example.qlttngoaingu.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.qlttngoaingu.dto.request.ReviewRequest;
import org.example.qlttngoaingu.dto.response.ReviewResponse;
import org.example.qlttngoaingu.entity.*;
import org.example.qlttngoaingu.exception.AppException;
import org.example.qlttngoaingu.exception.ErrorCode;
import org.example.qlttngoaingu.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final CourseReviewRepository reviewRepository;
    private final InvoiceDetailRepository invoiceDetailRepository;
    private final StudentRepository studentRepository;
    private final CourseClassRepository courseClassRepository;

    /**
     * STU-03: Học viên gửi đánh giá khóa học
     */
    @Transactional
    public ReviewResponse submitReview(Integer userId, ReviewRequest request) {
        // Lấy student từ userId
        Student student = studentRepository.findByAccount_UserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Lấy lớp học
        CourseClass courseClass = courseClassRepository.findById(request.getClassId())
                .orElseThrow(() -> new AppException(ErrorCode.CLASS_NOT_FOUND));

        // Lấy enrollment (kiểm tra học viên có đăng ký lớp này không)
        InvoiceDetail enrollment = invoiceDetailRepository
                .findByClassIdAndStudentId(request.getClassId(), student.getId())
                .orElseThrow(() -> new AppException(ErrorCode.ENROLLMENT_NOT_FOUND));

        // Kiểm tra đã đánh giá chưa
        if (reviewRepository.findByEnrollmentId(enrollment.getDetailId()).isPresent()) {
            throw new AppException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }

        // Tạo đánh giá mới
        CourseReview review = new CourseReview();
        review.setEnrollment(enrollment);
        review.setTeacherRating(request.getTeacherRating());
        review.setFacilityRating(request.getFacilityRating());
        review.setOverallRating(request.getOverallRating());
        review.setComment(request.getComment());

        CourseReview savedReview = reviewRepository.save(review);

        return buildReviewResponse(savedReview, courseClass, student);
    }

    /**
     * STU-04: Học viên xem lịch sử đánh giá của mình
     */
    public List<ReviewResponse> getStudentReviews(Integer userId) {
        // Lấy student từ userId
        Student student = studentRepository.findByAccount_UserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Lấy tất cả đánh giá của học viên
        List<CourseReview> reviews = reviewRepository.findAllByStudentId(student.getId());

        return reviews.stream()
                .map(review -> {
                    CourseClass cls = review.getEnrollment().getCourseClass();
                    return buildReviewResponse(review, cls, student);
                })
                .collect(Collectors.toList());
    }

    /**
     * Lấy tất cả đánh giá của một lớp học
     */
    public List<ReviewResponse> getClassReviews(Integer classId) {
        List<CourseReview> reviews = reviewRepository.findAllByClassId(classId);

        return reviews.stream()
                .map(review -> {
                    CourseClass cls = review.getEnrollment().getCourseClass();
                    Student student = review.getEnrollment().getInvoice().getStudent();
                    return buildReviewResponse(review, cls, student);
                })
                .collect(Collectors.toList());
    }

    /**
     * Lấy tất cả đánh giá của một khóa học
     */
    public List<ReviewResponse> getCourseReviews(Integer courseId) {
        List<CourseReview> reviews = reviewRepository.findAllByCourseId(courseId);

        return reviews.stream()
                .map(review -> {
                    CourseClass cls = review.getEnrollment().getCourseClass();
                    Student student = review.getEnrollment().getInvoice().getStudent();
                    return buildReviewResponse(review, cls, student);
                })
                .collect(Collectors.toList());
    }

    // ==================== HELPER METHODS ====================

    private ReviewResponse buildReviewResponse(CourseReview review, CourseClass cls, Student student) {
        Course course = cls.getCourse();

        return ReviewResponse.builder()
                .reviewId(review.getReviewId())
                .classId(cls.getClassId())
                .className(cls.getClassName())
                .courseId(course.getCourseId())
                .courseName(course.getCourseName())
                .courseImage(course.getImage())
                .teacherRating(review.getTeacherRating())
                .facilityRating(review.getFacilityRating())
                .overallRating(review.getOverallRating())
                .averageRating(ReviewResponse.calculateAverageRating(
                        review.getTeacherRating(),
                        review.getFacilityRating(),
                        review.getOverallRating()
                ))
                .comment(review.getComment())
                .studentId(student.getId())
                .studentName(student.getName())
                .studentAvatar(student.getAvatar())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
