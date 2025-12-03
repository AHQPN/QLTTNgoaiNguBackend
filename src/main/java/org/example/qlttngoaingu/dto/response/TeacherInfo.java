package org.example.qlttngoaingu.dto.response;

import jakarta.persistence.Column;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class TeacherInfo {

    private Integer lecturerId;
    private String fullName;
    private LocalDate dateOfBirth;
    private String imagePath;
    private String phoneNumber;
    private String email;

    // Thống kê cho Flutter FE
    private Integer totalClasses;
    private Integer totalStudents;
    private Double rating;

    private List<QualificationDTO> qualifications;

    @Data
    public static class QualificationDTO {
        private Integer degreeId;
        private String degreeName;
        private String level;
    }
}
