package org.example.qlttngoaingu.repository;

import java.util.List;
import java.util.Optional;

import org.example.qlttngoaingu.entity.Attendance;
import org.example.qlttngoaingu.entity.InvoiceDetail;
import org.example.qlttngoaingu.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AttendanceRepository extends JpaRepository<Attendance,Integer> {
	List<Attendance> findBySessionSessionId(Integer sessionId);
	Optional<Attendance> findBySessionAndInvoiceDetail(Session session, InvoiceDetail invoiceDetail);
	Optional<Attendance> findBySessionSessionIdAndInvoiceDetailDetailId(Integer sessionId, Integer invoiceDetailId);

	/**
	 * Đếm tổng số lần điểm danh của các lớp do giảng viên dạy
	 */
	@Query("SELECT COUNT(a) FROM Attendance a " +
	       "WHERE a.session.courseClass.lecturer.lecturerId = :lecturerId")
	long countTotalAttendancesByLecturer(@Param("lecturerId") Integer lecturerId);

	/**
	 * Đếm số lần có mặt (absent = false) của các lớp do giảng viên dạy
	 */
	@Query("SELECT COUNT(a) FROM Attendance a " +
	       "WHERE a.session.courseClass.lecturer.lecturerId = :lecturerId " +
	       "AND a.absent = false")
	long countPresentAttendancesByLecturer(@Param("lecturerId") Integer lecturerId);

	/**
	 * Lấy điểm danh theo danh sách session IDs
	 */
	@Query("SELECT a FROM Attendance a WHERE a.session.sessionId IN :sessionIds")
	List<Attendance> findBySessionSessionIdIn(@Param("sessionIds") List<Integer> sessionIds);
}
