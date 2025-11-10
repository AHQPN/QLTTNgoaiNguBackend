package org.example.qlttngoaingu.repository;

import org.example.qlttngoaingu.entity.CourseSkill;
import org.example.qlttngoaingu.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Integer> {
}
