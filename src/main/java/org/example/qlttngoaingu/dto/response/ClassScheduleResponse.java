package org.example.qlttngoaingu.dto.response;

import lombok.Data;

import java.time.LocalTime;
import java.util.List;
import java.util.Set;

@Data
public class ClassScheduleResponse {
    Set<String> schedulePatterns;
    Set<String> scheduleTimes;
}
