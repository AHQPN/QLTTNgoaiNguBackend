package org.example.qlttngoaingu.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.example.qlttngoaingu.entity.Invoice;
import org.example.qlttngoaingu.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Integer> {
    Boolean existsByStudentAndStatus(Student student, Boolean status);
    
    /**
     * Tìm các invoice chưa thanh toán và đã hết hạn
     * @param dateCreatedBefore Ngày tạo trước thời điểm này
     * @return Danh sách invoice hết hạn
     */
    List<Invoice> findByStatusFalseAndDateCreatedBefore(LocalDateTime dateCreatedBefore);
}