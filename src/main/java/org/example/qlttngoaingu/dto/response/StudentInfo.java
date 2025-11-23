package org.example.qlttngoaingu.dto.response;

import lombok.Data;

import java.time.LocalDate;

@Data
public class StudentInfo {
    private Integer studentId;
    private String name ;
    private LocalDate dateOfBirth;
    private Boolean gender;
    private String jobs;

    private String email;
    private String phoneNumber;
    private String address;
    private String image;
}
