package org.example.qlttngoaingu.specification;
import org.example.qlttngoaingu.entity.CourseClass;
import org.springframework.data.jpa.domain.Specification;

public class CourseClassSpec {

    public static Specification<CourseClass> hasLecturer(Integer lecturerId) {
        return (root, query, builder) ->
                lecturerId == null ? null :
                        builder.equal(root.get("lecturer").get("lecturerId"), lecturerId);
    }

    public static Specification<CourseClass> hasRoom(Integer roomId) {
        return (root, query, builder) ->
                roomId == null ? null :
                        builder.equal(root.get("room").get("roomId"), roomId);
    }

    public static Specification<CourseClass> hasCourse(Integer courseId) {
        return (root, query, builder) ->
                courseId == null ? null :
                        builder.equal(root.get("course").get("courseId"), courseId);
    }

    public static Specification<CourseClass> hasClassName(String className) {
        return (root, query, cb) -> {
            if (className == null || className.trim().isEmpty()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("className")), "%" + className.toLowerCase() + "%");
        };
    }

}
