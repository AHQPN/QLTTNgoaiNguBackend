package org.example.qlttngoaingu.repository;


import org.example.qlttngoaingu.entity.Objective;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ObjectiveRepository extends JpaRepository<Objective, Integer> {

    Optional<Objective> getObjectiveById(Integer id);
}
