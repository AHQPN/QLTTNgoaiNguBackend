package org.example.qlttngoaingu.repository;

import org.example.qlttngoaingu.entity.CourseSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseSkillRepository extends JpaRepository<CourseSkill, Integer> {
    List<CourseSkill> findByCourse_CourseId(Integer courseId);


    // Tìm CourseSkill theo courseId và skillId (để update/delete)
    Optional<CourseSkill> findByCourse_CourseIdAndSkill_SkillId(Integer courseId, Integer skillId);

    // Kiểm tra xem course đã có skill chưa
    boolean existsByCourse_CourseIdAndSkill_SkillId(Integer courseId, Integer skillId);
}
