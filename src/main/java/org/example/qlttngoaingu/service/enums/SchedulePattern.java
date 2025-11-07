package org.example.qlttngoaingu.service.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.List;

@Getter
@AllArgsConstructor
public enum SchedulePattern {
    MON_WED_FRI("2-4-6", Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)),
    TUE_THU_SAT("3-5-7", Arrays.asList(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY, DayOfWeek.SATURDAY)),
    SAT_SUN("T7-CN", Arrays.asList(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)),
    SUNDAY("CN", Arrays.asList(DayOfWeek.SUNDAY)),
    SATURDAY("T7", Arrays.asList(DayOfWeek.SATURDAY)),
    WEEKDAYS("2-3-4-5-6", Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)),
    MON_WED("2-4", Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY)),
    TUE_THU("3-5", Arrays.asList(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY));


    private final String pattern;
    private final List<DayOfWeek> daysOfWeek;



    public static SchedulePattern fromPattern(String pattern) {
        return Arrays.stream(values())
                .filter(sp -> sp.pattern.equals(pattern))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid schedule pattern: " + pattern));
    }
}
