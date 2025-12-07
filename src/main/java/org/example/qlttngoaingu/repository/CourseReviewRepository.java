package org.example.qlttngoaingu.repository;

import java.util.List;
import java.util.Optional;

import org.example.qlttngoaingu.entity.CourseReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseReviewRepository extends JpaRepository<CourseReview, Integer> {

    /**
     * Lấy tất cả đánh giá của học viên
     */
    @Query("""
        SELECT r FROM CourseReview r
        JOIN FETCH r.enrollment e
        JOIN FETCH e.courseClass c
        JOIN FETCH c.course
        JOIN e.invoice i
        JOIN i.student s
        WHERE s.id = :studentId
    """)
    List<CourseReview> findAllByStudentId(@Param("studentId") Integer studentId);

    /**
     * Kiểm tra xem học viên đã đánh giá lớp này chưa
     */
    @Query("""
        SELECT r FROM CourseReview r
        WHERE r.enrollment.detailId = :enrollmentId
    """)
    Optional<CourseReview> findByEnrollmentId(@Param("enrollmentId") Integer enrollmentId);

    /**
     * Lấy tất cả đánh giá của một lớp học
     */
    @Query("""
        SELECT r FROM CourseReview r
        JOIN FETCH r.enrollment e
        JOIN FETCH e.courseClass c
        JOIN FETCH e.invoice i
        JOIN FETCH i.student s
        WHERE c.classId = :classId
    """)
    List<CourseReview> findAllByClassId(@Param("classId") Integer classId);

    /**
     * Lấy tất cả đánh giá của một khóa học
     */
    @Query("""
        SELECT r FROM CourseReview r
        JOIN FETCH r.enrollment e
        JOIN FETCH e.courseClass c
        JOIN FETCH c.course course
        JOIN e.invoice i
        JOIN FETCH i.student s
        WHERE course.courseId = :courseId
    """)
    List<CourseReview> findAllByCourseId(@Param("courseId") Integer courseId);

    /**
     * Tính điểm đánh giá giảng viên trung bình của giảng viên
     */
    @Query("""
        SELECT AVG(r.teacherRating) FROM CourseReview r
        JOIN r.enrollment e
        JOIN e.courseClass c
        WHERE c.lecturer.lecturerId = :lecturerId
    """)
    Double getAverageTeacherRatingByLecturerId(@Param("lecturerId") Integer lecturerId);

    /**
     * Đếm số đánh giá của một lớp học
     */
    @Query("""
        SELECT COUNT(r) FROM CourseReview r
        JOIN r.enrollment e
        JOIN e.courseClass c
        WHERE c.classId = :classId
    """)
    int countByClassId(@Param("classId") Integer classId);
}
