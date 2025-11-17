package org.example.qlttngoaingu.repository;

import org.example.qlttngoaingu.entity.Lecturer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LecturerRepository extends JpaRepository<Lecturer, Integer> {
    Lecturer getByUser_UserId(Integer id);
}
