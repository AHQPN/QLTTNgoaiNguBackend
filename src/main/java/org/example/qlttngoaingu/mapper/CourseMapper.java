package org.example.qlttngoaingu.mapper;

import org.example.qlttngoaingu.dto.request.CourseCreateRequest;
import org.example.qlttngoaingu.dto.request.CourseUpdateRequest;
import org.example.qlttngoaingu.dto.response.ActiveCourseResponse;
import org.example.qlttngoaingu.dto.response.CourseDetailResponse;
import org.example.qlttngoaingu.entity.Course;
import org.mapstruct.Mapper;

import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CourseMapper {

    Course toNewCourse(CourseCreateRequest request);

    void toExistingCourse(@MappingTarget Course course, CourseUpdateRequest request);

    CourseDetailResponse toResponse(Course course);

    ActiveCourseResponse toActiveResponse(Course course);
}
