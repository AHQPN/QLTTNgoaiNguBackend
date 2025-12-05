package org.example.qlttngoaingu.utils;

import java.time.DayOfWeek;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class CustomSchedulePatternTest {

    @Test
    void testParse_CN() {
        CustomSchedulePattern p = new CustomSchedulePattern("CN");
        List<DayOfWeek> days = p.getDaysOfWeek();
        assertEquals(1, days.size());
        assertEquals(DayOfWeek.SUNDAY, days.get(0));
    }

    @Test
    void testParse_T7() {
        CustomSchedulePattern p = new CustomSchedulePattern("T7");
        List<DayOfWeek> days = p.getDaysOfWeek();
        assertEquals(1, days.size());
        assertEquals(DayOfWeek.SATURDAY, days.get(0));
    }

    @Test
    void testParse_T7_CN() {
        CustomSchedulePattern p = new CustomSchedulePattern("T7-CN");
        List<DayOfWeek> days = p.getDaysOfWeek();
        assertEquals(2, days.size());
        assertTrue(days.contains(DayOfWeek.SATURDAY));
        assertTrue(days.contains(DayOfWeek.SUNDAY));
    }

    @Test
    void testParse_numbers() {
        CustomSchedulePattern p = new CustomSchedulePattern("2-4-6");
        List<DayOfWeek> days = p.getDaysOfWeek();
        assertEquals(3, days.size());
        assertEquals(DayOfWeek.MONDAY, days.get(0));
        assertEquals(DayOfWeek.WEDNESDAY, days.get(1));
        assertEquals(DayOfWeek.FRIDAY, days.get(2));
    }

    @Test
    void testParse_1_vs_CN() {
        CustomSchedulePattern p1 = new CustomSchedulePattern("1");
        CustomSchedulePattern p2 = new CustomSchedulePattern("CN");
        assertEquals(p1.getDaysOfWeek(), p2.getDaysOfWeek());
    }

}
