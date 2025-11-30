package org.example.qlttngoaingu.repository;

import org.example.qlttngoaingu.entity.Lecturer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LecturerRepository extends JpaRepository<Lecturer, Integer> {
    Lecturer getByUser_UserId(Integer id);

    Optional<Lecturer> getLecturersByLecturerId(Integer lecturerId);
}
