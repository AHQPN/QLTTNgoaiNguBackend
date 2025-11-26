package org.example.qlttngoaingu.service;

import lombok.RequiredArgsConstructor;
import org.example.qlttngoaingu.dto.response.ConflictInfo;
import org.example.qlttngoaingu.entity.*;
import org.example.qlttngoaingu.repository.CourseClassRepository;
import org.example.qlttngoaingu.service.enums.ClassStatusEnum;
import org.example.qlttngoaingu.utils.CustomSchedulePattern;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConflictCheckService {

    private final CourseClassRepository classRepository;

    /**
     * Kiểm tra xung đột phòng
     */
    public List<ConflictInfo> checkRoomConflicts(
            Integer roomId,
            String schedule,
            LocalTime startTime,
            Integer minutesPerSession,
            LocalDate startDate,
            Integer excludeClassId) {

        List<ConflictInfo> conflicts = new ArrayList<>();
        CustomSchedulePattern newPattern = new CustomSchedulePattern(schedule);
        LocalTime newEnd = startTime.plusMinutes(minutesPerSession);

        // Lấy tất cả các lớp đang active của phòng này
        List<CourseClass> otherClasses = classRepository.findByRoom_RoomIdAndStatus(roomId,ClassStatusEnum.InProgress.name());

        for (CourseClass other : otherClasses) {
            // Bỏ qua nếu là chính lớp đang edit
            if (excludeClassId != null && Objects.equals(other.getClassId(), excludeClassId)) {
                continue;
            }

            // Kiểm tra xung đột thời gian
            ConflictInfo conflict = checkTimeConflict(
                    other,
                    newPattern,
                    startTime,
                    newEnd,
                    startDate,
                    "ROOM"
            );

            if (conflict != null) {
                conflict.setDescription(String.format(
                        "Phòng '%s' bị trùng với lớp '%s' (%s) vào %s từ %s-%s",
                        Optional.ofNullable(other.getRoom()).map(Room::getRoomName).orElse(""),
                        other.getClassName(),
                        Optional.ofNullable(other.getCourse()).map(Course::getCourseName).orElse(""),
                        formatDays(conflict.getCommonDays()),
                        other.getStartTime(),
                        other.getStartTime().plusMinutes(other.getMinutesPerSession())
                ));
                conflicts.add(conflict);
            }
        }

        return conflicts;
    }

    /**
     * Kiểm tra xung đột giảng viên
     */
    public List<ConflictInfo> checkTeacherConflicts(
            Integer lecturerId,
            String schedule,
            LocalTime startTime,
            Integer minutesPerSession,
            LocalDate startDate,
            Integer excludeClassId) {

        List<ConflictInfo> conflicts = new ArrayList<>();
        CustomSchedulePattern newPattern = new CustomSchedulePattern(schedule);
        LocalTime newEnd = startTime.plusMinutes(minutesPerSession);

        // Lấy tất cả các lớp đang active của giảng viên này
        List<CourseClass> otherClasses = classRepository.findByLecturer_LecturerIdAndStatus(lecturerId, ClassStatusEnum.InProgress.name());

        for (CourseClass other : otherClasses) {
            // Bỏ qua nếu là chính lớp đang edit
            if (excludeClassId != null && Objects.equals(other.getClassId(), excludeClassId)) {
                continue;
            }

            // Kiểm tra xung đột thời gian
            ConflictInfo conflict = checkTimeConflict(
                    other,
                    newPattern,
                    startTime,
                    newEnd,
                    startDate,
                    "LECTURER"
            );

            if (conflict != null) {
                conflict.setDescription(String.format(
                        "GV '%s' bị trùng lịch với lớp '%s' (%s) vào %s từ %s-%s",
                        Optional.ofNullable(other.getLecturer()).map(Lecturer::getFullName).orElse(""),
                        other.getClassName(),
                        Optional.ofNullable(other.getCourse()).map(Course::getCourseName).orElse(""),
                        formatDays(conflict.getCommonDays()),
                        other.getStartTime(),
                        other.getStartTime().plusMinutes(other.getMinutesPerSession())
                ));
                conflicts.add(conflict);
            }
        }

        return conflicts;
    }

    /**
     * Kiểm tra xung đột thời gian giữa 2 lịch học
     */
    private ConflictInfo checkTimeConflict(
            CourseClass existingClass,
            CustomSchedulePattern newPattern,
            LocalTime newStartTime,
            LocalTime newEndTime,
            LocalDate newStartDate,
            String conflictType) {

        // 1. Kiểm tra xung đột về khoảng thời gian (date range)
        LocalDate existingEndDate = calculateEndDate(existingClass);
        if (existingEndDate != null && existingEndDate.isBefore(newStartDate)) {
            return null; // Lớp cũ đã kết thúc trước khi lớp mới bắt đầu
        }

        // Tính end date của lớp mới (ước tính)
        LocalDate newEndDate = calculateEndDate(
                newStartDate,
                existingClass.getCourse().getStudyHours(),
                existingClass.getMinutesPerSession(),
                newPattern
        );
        if (newEndDate.isBefore(existingClass.getStartDate())) {
            return null; // Lớp mới kết thúc trước khi lớp cũ bắt đầu
        }

        // 2. Kiểm tra xung đột về ngày trong tuần
        CustomSchedulePattern existingPattern = new CustomSchedulePattern(existingClass.getSchedule());
        Set<DayOfWeek> commonDays = intersectDays(newPattern, existingPattern);

        if (commonDays.isEmpty()) {
            return null; // Không trùng ngày nào trong tuần
        }

        // 3. Kiểm tra xung đột về giờ trong ngày
        LocalTime existingStartTime = existingClass.getStartTime();
        LocalTime existingEndTime = existingStartTime.plusMinutes(existingClass.getMinutesPerSession());

        boolean timeOverlap = timeOverlaps(newStartTime, newEndTime, existingStartTime, existingEndTime);

        if (!timeOverlap) {
            return null; // Không trùng giờ
        }

        // Có xung đột → Tạo ConflictInfo
        ConflictInfo conflict = new ConflictInfo();
        conflict.setType(conflictType + "_CONFLICT");
        conflict.setCommonDays(commonDays);
        conflict.setConflictStartTime(existingStartTime);
        conflict.setConflictEndTime(existingEndTime);
        conflict.setExistingClass(existingClass);

        return conflict;
    }

    /**
     * Tính ngày kết thúc của một lớp học
     */
    private LocalDate calculateEndDate(CourseClass cls) {
        double totalHours = cls.getCourse().getStudyHours();
        double hoursPerSession = cls.getMinutesPerSession() / 60.0;
        int totalSessions = (int) Math.ceil(totalHours / hoursPerSession);

        LocalDate date = cls.getStartDate();
        CustomSchedulePattern pattern = new CustomSchedulePattern(cls.getSchedule());

        int count = 0;
        while (count < totalSessions) {
            if (pattern.getDaysOfWeek().contains(date.getDayOfWeek())) {
                count++;
            }
            date = date.plusDays(1);
        }

        return date.minusDays(1);
    }

    /**
     * Tính ngày kết thúc dựa trên thông số
     */
    private LocalDate calculateEndDate(
            LocalDate startDate,
            double totalHours,
            int minutesPerSession,
            CustomSchedulePattern pattern) {

        double hoursPerSession = minutesPerSession / 60.0;
        int totalSessions = (int) Math.ceil(totalHours / hoursPerSession);

        LocalDate date = startDate;
        int count = 0;

        while (count < totalSessions) {
            if (pattern.getDaysOfWeek().contains(date.getDayOfWeek())) {
                count++;
            }
            date = date.plusDays(1);
        }

        return date.minusDays(1);
    }

    /**
     * Tìm các ngày trùng nhau giữa 2 pattern
     */
    private Set<DayOfWeek> intersectDays(CustomSchedulePattern pattern1, CustomSchedulePattern pattern2) {
        Set<DayOfWeek> result = new HashSet<>(pattern1.getDaysOfWeek());
        result.retainAll(pattern2.getDaysOfWeek());
        return result;
    }

    /**
     * Kiểm tra 2 khoảng thời gian có overlap không
     */
    private boolean timeOverlaps(LocalTime aStart, LocalTime aEnd, LocalTime bStart, LocalTime bEnd) {
        return aStart.isBefore(bEnd) && aEnd.isAfter(bStart);
    }

    /**
     * Format danh sách ngày theo pattern Việt Nam
     * 1 = CN, 2 = T2, 3 = T3, ..., 7 = T7
     */
    private String formatDays(Set<DayOfWeek> days) {
        Map<DayOfWeek, String> names = Map.of(
                DayOfWeek.SUNDAY, "CN",
                DayOfWeek.MONDAY, "T2",
                DayOfWeek.TUESDAY, "T3",
                DayOfWeek.WEDNESDAY, "T4",
                DayOfWeek.THURSDAY, "T5",
                DayOfWeek.FRIDAY, "T6",
                DayOfWeek.SATURDAY, "T7"
        );

        // Sắp xếp theo thứ tự Việt Nam: CN, T2, T3, ..., T7
        List<DayOfWeek> sortedDays = new ArrayList<>(days);
        sortedDays.sort((a, b) -> {
            int orderA = a == DayOfWeek.SUNDAY ? 0 : a.getValue();
            int orderB = b == DayOfWeek.SUNDAY ? 0 : b.getValue();
            return Integer.compare(orderA, orderB);
        });

        return sortedDays.stream()
                .map(names::get)
                .collect(Collectors.joining(", "));
    }
}