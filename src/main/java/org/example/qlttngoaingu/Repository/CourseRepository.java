package org.example.qlttngoaingu.Repository;

import org.example.qlttngoaingu.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Integer> {
    List<Course> findByCourseName(String name);
    Page<Course> findAll(Pageable pageable);
    List<Course> findByStatusTrue();

    List<Course> findTop3ByCourseIdNotAndStatusTrue(Integer id);

}
