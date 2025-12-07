package org.example.qlttngoaingu.repository;


import java.util.List;

import org.example.qlttngoaingu.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Integer> {
    
    @Query("""
    SELECT d
    FROM Document d
    JOIN d.module m
    JOIN m.courseSkill cs
    JOIN cs.course c
    WHERE c.courseId IN (
        SELECT DISTINCT cl.course.courseId
        FROM InvoiceDetail detail
        JOIN detail.courseClass cl
        WHERE detail.invoice.student.id = :studentId
    )
    ORDER BY c.courseName, m.moduleName, d.fileName
    """)
    List<Document> findDocumentsByStudentId(@Param("studentId") Integer studentId);
}