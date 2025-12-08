package org.example.qlttngoaingu.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.example.qlttngoaingu.entity.Invoice;
import org.example.qlttngoaingu.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    
    /**
     * Tìm hóa đơn theo student ID với phân trang
     */
    Page<Invoice> findByStudent_IdOrderByDateCreatedDesc(Integer studentId, Pageable pageable);
    
    /**
     * Tìm tất cả hóa đơn với phân trang (cho admin)
     */
    @Query("SELECT i FROM Invoice i ORDER BY i.dateCreated DESC")
    Page<Invoice> findAllOrderByDateCreatedDesc(Pageable pageable);
    
    /**
     * Tìm hóa đơn theo trạng thái với phân trang
     */
    Page<Invoice> findByStatusOrderByDateCreatedDesc(Boolean status, Pageable pageable);
    
    /**
     * Tìm kiếm hóa đơn theo tên học viên hoặc số điện thoại
     */
    @Query("SELECT i FROM Invoice i JOIN i.student s JOIN s.account a " +
           "WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR a.phoneNumber LIKE CONCAT('%', :keyword, '%') " +
           "ORDER BY i.dateCreated DESC")
    Page<Invoice> searchInvoices(@Param("keyword") String keyword, Pageable pageable);
}