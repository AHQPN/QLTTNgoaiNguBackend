package org.example.qlttngoaingu.dto.response;

import jakarta.persistence.Column;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TeacherInfo {

    private String fullName;
    private LocalDate dateOfBirth;
    private String imagePath;
}
