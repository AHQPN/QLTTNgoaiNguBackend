package org.example.qlttngoaingu.mapper;

import org.example.qlttngoaingu.dto.request.CourseCreateRequest;
import org.example.qlttngoaingu.dto.request.CourseUpdateRequest;
import org.example.qlttngoaingu.dto.response.ActiveCourseNameResponse;
import org.example.qlttngoaingu.dto.response.ActiveCourseResponse;
import org.example.qlttngoaingu.dto.response.CourseDetailResponse;
import org.example.qlttngoaingu.entity.Course;
import org.example.qlttngoaingu.entity.CourseCategory;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CourseMapper {

    @Mapping(target = "courseCategory", source = "courseCategoryId", qualifiedByName = "mapCategoryIdToEntity")
    Course toNewCourse(CourseCreateRequest request);

    @Named("mapCategoryIdToEntity")
    default CourseCategory mapCategoryIdToEntity(Integer id) {
        if (id == null) return null;
        CourseCategory c = new CourseCategory();
        c.setId(id);
        return c;
    }

    void toExistingCourse(@MappingTarget Course course, CourseUpdateRequest request);

    CourseDetailResponse toResponse(Course course);

    ActiveCourseResponse toActiveResponse(Course course);

    ActiveCourseNameResponse toActiveNameResponse(Course course);
}
