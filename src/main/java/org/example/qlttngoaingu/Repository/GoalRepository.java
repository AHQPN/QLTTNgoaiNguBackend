package org.example.qlttngoaingu.Repository;


import org.example.qlttngoaingu.entity.Objective;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GoalRepository extends JpaRepository<Objective, Integer> {
}
