package org.example.qlttngoaingu.repository;

import org.example.qlttngoaingu.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface SessionRepository extends JpaRepository<Session, Integer> {
    List<Session> findByCourseClass_ClassIdOrderBySessionDate(Integer classId);

    List<Session> findBySessionDateBetween(LocalDate weekStart, LocalDate weekEnd);

    List<Session> findByCourseClass_ClassIdInAndSessionDateBetween(
            List<Integer> classIds,
            LocalDate start,
            LocalDate end
    );

    List<Session> findByCourseClass_ClassIdInAndSessionDateBetweenAndStatusNot(List<Integer> classIds, LocalDate weekStart, LocalDate weekEnd, String canceled);
}
