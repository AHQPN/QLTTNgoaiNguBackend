package org.example.qlttngoaingu.repository;

import org.example.qlttngoaingu.entity.CourseSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseSkillRepository extends JpaRepository<CourseSkill, Integer> {
    List<CourseSkill> findByCourse_CourseId(Integer courseId);
}
