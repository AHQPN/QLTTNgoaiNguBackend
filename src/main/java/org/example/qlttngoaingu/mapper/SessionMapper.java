package org.example.qlttngoaingu.mapper;

import org.example.qlttngoaingu.dto.response.WeeklyScheduleResponse;
import org.example.qlttngoaingu.entity.Session;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SessionMapper {

    @Mapping(source = "courseClass.classId", target = "classId")
    @Mapping(source = "courseClass.className", target = "className")
    @Mapping(source = "courseClass.course.courseName", target = "courseName")
    @Mapping(source = "courseClass.room.roomName", target = "roomName")
    @Mapping(source = "courseClass.lecturer.fullName", target = "instructorName")
    @Mapping(source = "courseClass.schedule", target = "schedulePattern")
    @Mapping(source = "sessionDate", target = "sessionDate")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "note", target = "note")
    @Mapping(target = "startTime", expression = "java(session.getCourseClass().getStartTime() != null ? session.getCourseClass().getStartTime().toString() : null)")
    @Mapping(source = "courseClass.minutesPerSession", target = "durationMinutes")
    WeeklyScheduleResponse.SessionInfo toDto(Session session);
}
