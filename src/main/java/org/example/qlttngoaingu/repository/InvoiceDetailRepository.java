package org.example.qlttngoaingu.repository;

import java.util.List;
import java.util.Optional;

import org.example.qlttngoaingu.entity.CourseClass;
import org.example.qlttngoaingu.entity.InvoiceDetail;
import org.example.qlttngoaingu.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceDetailRepository extends JpaRepository<InvoiceDetail, Integer> {

    // Đếm số lượng InvoiceDetail thuộc về lớp học này mà Hóa đơn cha có status = true
    @Query("SELECT COUNT(d) FROM InvoiceDetail d " +
            "WHERE d.courseClass.classId = :classId " +
            "AND d.invoice.status = true")
    Integer countByClassIdAndActiveInvoice(@Param("classId") Integer classId);

    @Query("SELECT DISTINCT s FROM InvoiceDetail d " +
            "JOIN d.invoice i " +
            "JOIN i.student s " +
            "LEFT JOIN FETCH s.account " + // Eager fetch account để lấy email/phone
            "WHERE d.courseClass.classId = :classId " +
            "AND i.status = true")
    List<Student> findStudentsByClassId(@Param("classId") Integer classId);

        @Query("SELECT d FROM InvoiceDetail d JOIN d.invoice i WHERE d.courseClass.classId = :classId AND i.student.id = :studentId AND i.status = true")
        Optional<org.example.qlttngoaingu.entity.InvoiceDetail> findByClassIdAndStudentId(@Param("classId") Integer classId, @Param("studentId") Integer studentId);

    @Query("""
        SELECT cls
        FROM CourseClass cls
        JOIN InvoiceDetail detail ON detail.courseClass = cls
        JOIN Invoice hd ON hd = detail.invoice
        WHERE hd.student.id = :hocVienId
    """)
    List<CourseClass> findAllByHocVienId(@Param("hocVienId") Integer hocVienId);
}
