package org.example.qlttngoaingu.mapper;

import org.example.qlttngoaingu.dto.request.ScheduleCheckRequest;
import org.example.qlttngoaingu.dto.response.ClassResponse;
import org.example.qlttngoaingu.entity.CourseClass;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CourseClassMapper {

    CourseClassMapper INSTANCE = Mappers.getMapper(CourseClassMapper.class);

    @Mapping(source = "course.courseId", target = "courseId")
    @Mapping(source = "startDate", target = "startDate")
    @Mapping(source = "schedule", target = "schedulePattern") // Ánh xạ 'schedule' trong Entity sang 'schedulePattern' trong Request
    @Mapping(source = "startTime", target = "startTime")
    @Mapping(source = "room.roomId", target = "preferredRoomId") // Lấy ID từ đối tượng Room
    @Mapping(source = "lecturer.lecturerId", target = "preferredLecturerId") // Lấy ID từ đối tượng Lecturer
    @Mapping(source = "minutesPerSession", target = "durationMinutes") // Ánh xạ 'minutesPerSession' sang 'durationMinutes'
    ScheduleCheckRequest toScheduleCheckRequest(CourseClass courseClass);


    @Mapping(source = "course.courseName", target = "courseName")
    @Mapping(source = "room.roomName", target = "roomName")
    @Mapping(source = "lecturer.fullName", target = "instructorName")
    @Mapping(source = "schedule", target = "schedulePattern")
    ClassResponse.ClassInfo toDto(CourseClass courseClass);
}