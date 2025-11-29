package org.example.qlttngoaingu.repository;

import java.util.List;

import org.example.qlttngoaingu.entity.LecturerDegree;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LecturerDegreeRepository extends JpaRepository<LecturerDegree, Integer> {
    List<LecturerDegree> findByLecturer_LecturerId(Integer lecturerId);
}
