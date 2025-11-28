package org.example.qlttngoaingu.repository;

import java.util.List;

import org.example.qlttngoaingu.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceRepository extends JpaRepository<Attendance,Integer> {
	List<Attendance> findBySessionSessionId(Integer sessionId);
	Attendance findBySessionSessionIdAndStudentId(Integer sessionId, Integer studentId);
}
