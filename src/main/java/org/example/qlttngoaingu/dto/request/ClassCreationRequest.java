package org.example.qlttngoaingu.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ClassCreationRequest {
    private Integer courseId;
    private String className;
    private Integer lecturerId;
    private Integer roomId;
    private String schedule;
    private LocalTime startTime;
    private Integer minutesPerSession;
    private LocalDate startDate;
    private String note;
}
