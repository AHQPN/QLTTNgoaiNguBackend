package org.example.qlttngoaingu.repository;

import org.example.qlttngoaingu.entity.Invoice;
import org.example.qlttngoaingu.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Integer> {
    Boolean existsByStudentAndStatus(Student student, Boolean status);
}