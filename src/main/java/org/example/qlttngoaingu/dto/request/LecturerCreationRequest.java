package org.example.qlttngoaingu.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class LecturerCreationRequest {
    private String name;

    private LocalDate dateOfBirth;

    private String phoneNumber;

    private String email;

    private String imageUrl;

}
