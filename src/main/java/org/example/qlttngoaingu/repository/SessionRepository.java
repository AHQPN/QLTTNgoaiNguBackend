package org.example.qlttngoaingu.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.example.qlttngoaingu.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SessionRepository extends JpaRepository<Session, Integer> {
    List<Session> findByCourseClass_ClassIdOrderBySessionDate(Integer classId);

    List<Session> findBySessionDate(LocalDate sessionDate);

    List<Session> findBySessionDateBetween(LocalDate weekStart, LocalDate weekEnd);

    List<Session> findByCourseClass_ClassIdInAndSessionDateBetween(
            List<Integer> classIds,
            LocalDate start,
            LocalDate end
    );

    List<Session> findByCourseClass_ClassIdInAndSessionDateBetweenAndStatusNot(List<Integer> classIds, LocalDate weekStart, LocalDate weekEnd, String canceled);

    @Query("SELECT s FROM Session s JOIN FETCH s.courseClass WHERE s.sessionId = :sessionId")
    Optional<Session> getSessionBySessionId(@Param("sessionId") Integer sessionId);

    /**
     * Đếm tổng số buổi học của lớp
     */
    @Query("SELECT COUNT(s) FROM Session s WHERE s.courseClass.classId = :classId")
    long countTotalSessionsByClassId(@Param("classId") Integer classId);

    /**
     * Đếm số buổi học đã hoàn thành (có sessionDate <= hôm nay)
     */
    @Query("SELECT COUNT(s) FROM Session s WHERE s.courseClass.classId = :classId AND s.sessionDate <= :currentDate")
    long countCompletedSessionsByClassId(@Param("classId") Integer classId, @Param("currentDate") LocalDate currentDate);

}
