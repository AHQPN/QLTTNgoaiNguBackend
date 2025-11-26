package org.example.qlttngoaingu.repository;

import org.example.qlttngoaingu.entity.CourseCategory;
import org.example.qlttngoaingu.entity.CourseClass;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface CourseClassRepository extends JpaRepository<CourseClass, Integer>, JpaSpecificationExecutor<CourseClass> {

    @Query("""
        SELECT c 
        FROM CourseClass c 
        WHERE c.course.courseId = :courseId
          AND c.startDate >= :startDate
          AND (
                SELECT COUNT(i) FROM InvoiceDetail i 
                WHERE i.courseClass.classId = c.classId
              ) < c.room.capacity
    """)
    List<CourseClass> findAvailableClasses(
            @Param("courseId") Integer courseId,
            @Param("startDate") LocalDate startDate
    );

    List<CourseClass> findByRoom_RoomIdAndStartDateGreaterThanEqual(Integer roomId, LocalDate startDate);

    List<CourseClass> findByLecturer_LecturerIdAndStartDateGreaterThanEqual(Integer lecturerId, LocalDate startDate);

    List<CourseClass> findByRoom_RoomIdAndStatus(Integer roomId,String status);

    // Lấy các lớp theo giảng viên mà đang active (status = true)
    List<CourseClass> findByLecturer_LecturerIdAndStatus(Integer lecturerId, String status);


    // Lấy tất cả lớp có thể phân trang
    Page<CourseClass> findAll(Pageable pageable);


    Set<CourseClass> findByCourse_CourseIdAndStatus(int courseId, String status);
    CourseClass getCourseClassByClassId(Integer classId);


    List<CourseClass> findByCourse_CourseId(Integer courseId);

    @Query("""
    SELECT detail.courseClass.classId
    FROM InvoiceDetail detail
    WHERE detail.invoice.student.id = :studentId
    """)
    List<Integer> findRegisteredClassIds(@Param("studentId") Integer studentId);

    @Query("""
    SELECT detail.courseClass
    FROM InvoiceDetail detail
    WHERE detail.invoice.student.id = :studentId
    """)
    List<CourseClass> findRegisteredClasses(@Param("studentId") Integer studentId);


    List<CourseClass> findByLecturer_LecturerIdAndStatusNot(Integer lecturerId, String status);

    List<Integer> findIdsByLecturer_LecturerIdAndStatusNot(Integer lecturerId, String status);

}

