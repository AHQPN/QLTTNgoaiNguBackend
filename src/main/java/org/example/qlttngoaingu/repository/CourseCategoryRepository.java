package org.example.qlttngoaingu.repository;

import org.example.qlttngoaingu.entity.CourseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseCategoryRepository extends JpaRepository<CourseCategory,Integer> {

}
