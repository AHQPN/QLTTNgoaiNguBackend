package org.example.qlttngoaingu.repository;

import org.example.qlttngoaingu.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student,Integer> {
    Student getStudentByAccount_UserId(Integer userId);

    Optional<Student> findByAccount_UserId(Integer userId);
    
    /**
     * Tìm kiếm học viên theo tên, email hoặc số điện thoại
     */
    @Query("SELECT s FROM Student s JOIN s.account a WHERE " +
           "LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "a.phoneNumber LIKE CONCAT('%', :search, '%')")
    Page<Student> searchByNameOrEmailOrPhone(@Param("search") String search, Pageable pageable);
}
