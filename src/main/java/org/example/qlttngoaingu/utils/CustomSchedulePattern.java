package org.example.qlttngoaingu.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public class CustomSchedulePattern {

    private final String pattern;
    private final List<DayOfWeek> daysOfWeek;

    public CustomSchedulePattern(String pattern) {
        this.pattern = pattern;
        this.daysOfWeek = parsePattern(pattern);
    }

    private List<DayOfWeek> parsePattern(String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            throw new IllegalArgumentException("Pattern cannot be null or empty");
        }

        return Arrays.stream(pattern.split("-"))
                .map(String::trim)
                .mapToInt(Integer::parseInt)
                .sorted()
                .mapToObj(CustomSchedulePattern::dayOfWeekFromNumber)
                .collect(Collectors.toList());
    }

    private static DayOfWeek dayOfWeekFromNumber(int number) {
        return switch (number) {
            case 1 -> DayOfWeek.SUNDAY;
            case 2 -> DayOfWeek.MONDAY;
            case 3 -> DayOfWeek.TUESDAY;
            case 4 -> DayOfWeek.WEDNESDAY;
            case 5 -> DayOfWeek.THURSDAY;
            case 6 -> DayOfWeek.FRIDAY;
            case 7 -> DayOfWeek.SATURDAY;
            default -> throw new IllegalArgumentException("Invalid day number: " + number);
        };
    }
}
