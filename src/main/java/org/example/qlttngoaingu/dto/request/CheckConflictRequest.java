package org.example.qlttngoaingu.dto.request;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class CheckConflictRequest {
    String schedulePattern;
    LocalTime startTime;
    Integer durationMinutes;
    LocalDate startDate;
}
