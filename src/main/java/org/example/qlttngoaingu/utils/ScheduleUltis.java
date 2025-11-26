package org.example.qlttngoaingu.utils;

import java.time.LocalTime;

public class ScheduleUltis {
    public static String getSessionPeriod(LocalTime startTime) {
        if (startTime == null) return "Unknown";

        if (!startTime.isBefore(LocalTime.of(7, 0)) && startTime.isBefore(LocalTime.of(11, 31))) {
            return "Sáng";
        } else if (!startTime.isBefore(LocalTime.of(13, 30)) && startTime.isBefore(LocalTime.of(17, 1))) {
            return "Chiều";
        } else if (!startTime.isBefore(LocalTime.of(18, 0)) && startTime.isBefore(LocalTime.of(21, 1))) {
            return "Tối";
        } else {
            return "Khác";
        }
    }

}
