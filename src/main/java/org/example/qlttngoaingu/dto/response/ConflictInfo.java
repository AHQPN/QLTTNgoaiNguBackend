package org.example.qlttngoaingu.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.example.qlttngoaingu.entity.CourseClass;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

@Data
public class ConflictInfo {
    // Thông tin cơ bản
    private String type; // "ROOM_CONFLICT" hoặc "TEACHER_CONFLICT"
    private String description; // Mô tả xung đột cho user đọc

    // Chi tiết xung đột về thời gian
    private LocalDate conflictDate;
    private LocalTime conflictStartTime;
    private LocalTime conflictEndTime;

    // Thông tin về lớp đang conflict (để hiển thị cho user)
    private String conflictingClassName;
    private String conflictingCourseName;

    // Thông tin nội bộ (không serialize ra JSON)
    @JsonIgnore
    private Set<DayOfWeek> commonDays; // Các ngày trùng nhau

    @JsonIgnore
    private CourseClass existingClass; // Lớp đang gây xung đột

    /**
     * Helper method để set existing class và tự động extract info
     */
    public void setExistingClass(CourseClass cls) {
        this.existingClass = cls;
        if (cls != null) {
            this.conflictingClassName = cls.getClassName();
            this.conflictingCourseName = cls.getCourse() != null
                    ? cls.getCourse().getCourseName()
                    : null;
        }
    }
}