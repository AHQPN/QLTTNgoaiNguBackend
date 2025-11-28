package org.example.qlttngoaingu.repository;

import org.example.qlttngoaingu.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, Integer> {
    List<Session> findByCourseClass_ClassIdOrderBySessionDate(Integer classId);

    List<Session> findBySessionDateBetween(LocalDate weekStart, LocalDate weekEnd);

    List<Session> findByCourseClass_ClassIdInAndSessionDateBetween(
            List<Integer> classIds,
            LocalDate start,
            LocalDate end
    );

    List<Session> findByCourseClass_ClassIdInAndSessionDateBetweenAndStatusNot(List<Integer> classIds, LocalDate weekStart, LocalDate weekEnd, String canceled);

    @Query("SELECT s FROM Session s JOIN FETCH s.courseClass WHERE s.sessionId = :sessionId")
    Optional<Session> getSessionBySessionId(@Param("sessionId") Integer sessionId);

}
