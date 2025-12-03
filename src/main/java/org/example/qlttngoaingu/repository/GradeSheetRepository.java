package org.example.qlttngoaingu.repository;

import org.example.qlttngoaingu.entity.GradeSheet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GradeSheetRepository extends JpaRepository<GradeSheet, Integer> {

    /**
     * Lấy tất cả điểm của học viên theo student ID
     */
    @Query("""
        SELECT gs FROM GradeSheet gs
        JOIN FETCH gs.enrollment e
        JOIN FETCH e.courseClass c
        JOIN FETCH c.course
        JOIN e.invoice i
        JOIN i.student s
        WHERE s.id = :studentId
        ORDER BY gs.gradedAt DESC
    """)
    List<GradeSheet> findAllByStudentId(@Param("studentId") Integer studentId);

    /**
     * Lấy điểm của học viên theo lớp cụ thể
     */
    @Query("""
        SELECT gs FROM GradeSheet gs
        JOIN FETCH gs.enrollment e
        JOIN FETCH e.courseClass c
        JOIN e.invoice i
        JOIN i.student s
        WHERE s.id = :studentId AND c.classId = :classId
        ORDER BY gs.gradeType ASC
    """)
    List<GradeSheet> findByStudentIdAndClassId(
            @Param("studentId") Integer studentId,
            @Param("classId") Integer classId
    );

    /**
     * Lấy tất cả điểm của học viên trong một lớp (cho giảng viên xem)
     */
    @Query("""
        SELECT gs FROM GradeSheet gs
        JOIN FETCH gs.enrollment e
        JOIN FETCH e.courseClass c
        JOIN e.invoice i
        JOIN FETCH i.student s
        WHERE c.classId = :classId
        ORDER BY s.name ASC, gs.gradeType ASC
    """)
    List<GradeSheet> findAllByClassId(@Param("classId") Integer classId);

    /**
     * Kiểm tra xem học viên đã có điểm cho loại đánh giá này chưa
     */
    @Query("""
        SELECT gs FROM GradeSheet gs
        WHERE gs.enrollment.detailId = :enrollmentId
        AND gs.gradeType = :gradeType
    """)
    Optional<GradeSheet> findByEnrollmentIdAndGradeType(
            @Param("enrollmentId") Integer enrollmentId,
            @Param("gradeType") String gradeType
    );

    /**
     * Lấy danh sách điểm theo enrollment ID
     */
    List<GradeSheet> findByEnrollment_DetailId(Integer enrollmentId);
}
