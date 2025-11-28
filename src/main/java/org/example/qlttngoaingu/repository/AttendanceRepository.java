package org.example.qlttngoaingu.repository;

import java.util.List;
import java.util.Optional;

import org.example.qlttngoaingu.entity.Attendance;
import org.example.qlttngoaingu.entity.InvoiceDetail;
import org.example.qlttngoaingu.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceRepository extends JpaRepository<Attendance,Integer> {
	List<Attendance> findBySessionSessionId(Integer sessionId);
	Optional<Attendance> findBySessionAndInvoiceDetail(Session session, InvoiceDetail invoiceDetail);
	Optional<Attendance> findBySessionSessionIdAndInvoiceDetailDetailId(Integer sessionId, Integer invoiceDetailId);
}
