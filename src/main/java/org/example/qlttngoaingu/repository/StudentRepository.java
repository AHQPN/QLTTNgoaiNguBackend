package org.example.qlttngoaingu.repository;

import org.example.qlttngoaingu.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student,Integer> {
    Student getStudentByAccount_UserId(Integer userId);

    Optional<Student> findByAccount_UserId(Integer userId);
}
