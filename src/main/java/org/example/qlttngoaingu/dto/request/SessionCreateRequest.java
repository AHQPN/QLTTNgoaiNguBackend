package org.example.qlttngoaingu.dto.request;

import java.time.LocalDate;

import lombok.Data;

@Data
public class SessionCreateRequest {
    private LocalDate sessionDate;
    private String note;
}
