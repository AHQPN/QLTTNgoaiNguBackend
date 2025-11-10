package org.example.qlttngoaingu.repository;

import org.example.qlttngoaingu.entity.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Integer> {

}