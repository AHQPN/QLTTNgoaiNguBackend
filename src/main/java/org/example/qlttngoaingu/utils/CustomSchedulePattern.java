package org.example.qlttngoaingu.utils;

import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

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
                .mapToInt(CustomSchedulePattern::parseDayTokenToNumber)
                .sorted()
                .mapToObj(CustomSchedulePattern::dayOfWeekFromNumber)
                .collect(Collectors.toList());
    }

    private static int parseDayTokenToNumber(String token) {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Empty day token");
        }

        token = token.trim().toUpperCase();

        // Support Vietnamese short forms: CN, T2..T7
        if (token.equals("CN")) return 1; // Chủ nhật
        if (token.startsWith("T")) {
            String num = token.substring(1);
            try {
                int v = Integer.parseInt(num);
                if (v >= 2 && v <= 7) return v;
            } catch (NumberFormatException e) {
                // fallthrough to exception
            }
            throw new IllegalArgumentException("Invalid token: " + token);
        }

        // Fallback: numeric tokens 1..7 (1 = CN = Sunday)
        try {
            int v = Integer.parseInt(token);
            if (v >= 1 && v <= 7) return v;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid day token: " + token);
        }

        throw new IllegalArgumentException("Invalid day token: " + token);
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
