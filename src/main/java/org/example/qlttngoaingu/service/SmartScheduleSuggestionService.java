package org.example.qlttngoaingu.service;

import lombok.RequiredArgsConstructor;
import org.example.qlttngoaingu.dto.request.ScheduleCheckRequest;
import org.example.qlttngoaingu.dto.response.*;
import org.example.qlttngoaingu.entity.*;
import org.example.qlttngoaingu.repository.*;
import org.example.qlttngoaingu.utils.CustomSchedulePattern;
import org.example.qlttngoaingu.utils.ResourceConverter;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SmartScheduleSuggestionService {

    private final CourseRepository courseRepository;
    private final RoomRepository roomRepository;
    private final LecturerRepository lecturerRepository;
    private final CourseClassRepository classRepository;
    private final ConflictCheckService conflictCheckService;

    /**
     * API chính: Check và gợi ý resources
     */
    public ScheduleSuggestionResponse checkAndSuggest(ScheduleCheckRequest request) {
        ScheduleSuggestionResponse response = new ScheduleSuggestionResponse();
        // 1. Kiểm tra với thông tin user nhập
        AvailabilityResult initialCheck = checkInitialAvailability(request);
        response.setInitialCheck(initialCheck);

        if (initialCheck.getLecturerConflicts().isEmpty() && initialCheck.getRoomConflicts().isEmpty() && initialCheck.isFullyAvailable()) {
            // TH1: Cả phòng và GV đều trống → OK
            response.setStatus("AVAILABLE");
            response.setMessage("Lịch học khả dụng! Vui lòng chọn phòng và giảng viên.");

            List<Room> rooms = getAvailableRooms(request);
            List<Lecturer> lecturers = getAvailableLecturers(request);

            response.setAvailableRooms(ResourceConverter.fromRooms(rooms));
            response.setAvailableLecturers(ResourceConverter.fromLecturers(lecturers));
            return response;
        }

        // 2. Có xung đột → Tạo các gợi ý
        response.setStatus("CONFLICT");
        response.setMessage("Lịch học bị xung đột. Dưới đây là các gợi ý thay thế:");

        List<ScheduleAlternative> alternatives = new ArrayList<>();

        // Chiến lược 1: Thử giờ khác trong cùng ngày
        alternatives.addAll(suggestAlternativeTimesInSameDay(request));

        // Chiến lược 2: Thử ngày bắt đầu khác (trong vòng 2 tuần)
        alternatives.addAll(suggestAlternativeStartDates(request));

        // Chiến lược 3: Thử pattern khác
        alternatives.addAll(suggestAlternativePatterns(request));

        // Chiến lược 4: Gợi ý phòng thay thế (nếu user đã chọn phòng cụ thể)
        if (request.getPreferredRoomId() != null && !initialCheck.getRoomConflicts().isEmpty()) {
            alternatives.addAll(suggestAlternativeRooms(request));
        }

        // Chiến lược 5: Gợi ý giảng viên thay thế (nếu user đã chọn GV cụ thể)
        if (request.getPreferredLecturerId() != null && !initialCheck.getLecturerConflicts().isEmpty()) {
            alternatives.addAll(suggestAlternativeLecturers(request));
        }

        // Sắp xếp theo độ ưu tiên
        alternatives.sort(Comparator.comparingInt(ScheduleAlternative::getPriority).reversed());

        response.setAlternatives(alternatives);

        return response;
    }

    /**
     * Kiểm tra ban đầu với input của user
     */
    private AvailabilityResult checkInitialAvailability(ScheduleCheckRequest request) {
        AvailabilityResult result = new AvailabilityResult();

        List<Room> availableRooms = getAvailableRooms(request);
        List<Lecturer> availableLecturers = getAvailableLecturers(request);

        result.setHasAvailableRooms(!availableRooms.isEmpty());
        result.setHasAvailableLecturers(!availableLecturers.isEmpty());
        result.setAvailableRoomCount(availableRooms.size());
        result.setAvailableLecturerCount(availableLecturers.size());

        // Chi tiết xung đột
        if (request.getPreferredRoomId() != null) {
            List<ConflictInfo> roomConflicts = conflictCheckService.checkRoomConflicts(
                    request.getPreferredRoomId(),
                    request.getSchedulePattern(),
                    request.getStartTime(),
                    request.getDurationMinutes(),
                    request.getStartDate(),
                    request.getExcludeClassId()
            );
            result.setRoomConflicts(roomConflicts);
        }

        if (request.getPreferredLecturerId() != null) {
            List<ConflictInfo> lecturerConflicts = conflictCheckService.checkTeacherConflicts(
                    request.getPreferredLecturerId(),
                    request.getSchedulePattern(),
                    request.getStartTime(),
                    request.getDurationMinutes(),
                    request.getStartDate(),
                    request.getExcludeClassId()
            );
            result.setLecturerConflicts(lecturerConflicts);
        }

        return result;
    }

    /**
     * Chiến lược 1: Gợi ý giờ khác trong cùng ngày
     */
    private List<ScheduleAlternative> suggestAlternativeTimesInSameDay(ScheduleCheckRequest request) {
        List<ScheduleAlternative> alternatives = new ArrayList<>();

        // Danh sách các khung giờ phổ biến
        List<LocalTime> timeSlots = Arrays.asList(
                LocalTime.of(7, 0),
                LocalTime.of(7, 30),
                LocalTime.of(8, 0),
                LocalTime.of(8, 30),
                LocalTime.of(9, 0),
                LocalTime.of(9, 30),
                LocalTime.of(13, 0),
                LocalTime.of(13, 30),
                LocalTime.of(14, 0),
                LocalTime.of(14, 30),
                LocalTime.of(15, 0),
                LocalTime.of(15, 30),
                LocalTime.of(16, 0),
                LocalTime.of(18, 0),
                LocalTime.of(18, 30),
                LocalTime.of(19, 0),
                LocalTime.of(19, 30)
        );

        for (LocalTime altTime : timeSlots) {
            if (altTime.equals(request.getStartTime())) continue;

            ScheduleCheckRequest altRequest = request.copy();
            altRequest.setStartTime(altTime);

            List<Room> rooms = getAvailableRooms(altRequest);
            List<Lecturer> lecturers = getAvailableLecturers(altRequest);

            if (!rooms.isEmpty() && !lecturers.isEmpty()) {
                ScheduleAlternative alt = new ScheduleAlternative();
                alt.setType("ALTERNATIVE_TIME");
                alt.setStartDate(request.getStartDate());
                alt.setStartTime(altTime);
                alt.setEndTime(altTime.plusMinutes(request.getDurationMinutes()));
                alt.setSchedulePattern(request.getSchedulePattern());
                alt.setAvailableRooms(ResourceConverter.fromRooms(rooms));
                alt.setAvailableLecturers(ResourceConverter.fromLecturers(lecturers));
                alt.setReason(String.format("Đổi giờ từ %s sang %s",
                        request.getStartTime(), altTime));
                alt.setPriority(calculatePriority("TIME", rooms.size(), lecturers.size(),
                        Math.abs(altTime.toSecondOfDay() - request.getStartTime().toSecondOfDay())));

                alternatives.add(alt);
            }
        }

        return alternatives;
    }

    /**
     * Chiến lược 2: Gợi ý ngày bắt đầu khác
     */
    private List<ScheduleAlternative> suggestAlternativeStartDates(ScheduleCheckRequest request) {
        List<ScheduleAlternative> alternatives = new ArrayList<>();
        CustomSchedulePattern pattern = new CustomSchedulePattern(request.getSchedulePattern());

        LocalDate currentDate = request.getStartDate().plusDays(1);
        int daysChecked = 0;
        int maxDays = 14; // Tìm trong vòng 2 tuần

        while (daysChecked < maxDays && alternatives.size() < 5) {
            if (pattern.getDaysOfWeek().contains(currentDate.getDayOfWeek())) {
                ScheduleCheckRequest altRequest = request.copy();
                altRequest.setStartDate(currentDate);

                List<Room> rooms = getAvailableRooms(altRequest);
                List<Lecturer> lecturers = getAvailableLecturers(altRequest);

                if (!rooms.isEmpty() && !lecturers.isEmpty()) {
                    ScheduleAlternative alt = new ScheduleAlternative();
                    alt.setType("ALTERNATIVE_START_DATE");
                    alt.setStartDate(currentDate);
                    alt.setStartTime(request.getStartTime());
                    alt.setEndTime(request.getStartTime().plusMinutes(request.getDurationMinutes()));
                    alt.setSchedulePattern(request.getSchedulePattern());
                    alt.setAvailableRooms(ResourceConverter.fromRooms(rooms));
                    alt.setAvailableLecturers(ResourceConverter.fromLecturers(lecturers));
                    alt.setReason(String.format("Bắt đầu từ %s thay vì %s",
                            currentDate, request.getStartDate()));
                    alt.setPriority(calculatePriority("START_DATE", rooms.size(), lecturers.size(),
                            (int) java.time.temporal.ChronoUnit.DAYS.between(request.getStartDate(), currentDate)));

                    alternatives.add(alt);
                }
            }
            currentDate = currentDate.plusDays(1);
            daysChecked++;
        }

        return alternatives;
    }

    /**
     * Chiến lược 3: Gợi ý pattern khác
     */
    private List<ScheduleAlternative> suggestAlternativePatterns(ScheduleCheckRequest request) {
        List<ScheduleAlternative> alternatives = new ArrayList<>();

        // Tạo danh sách các pattern phổ biến
        List<String> alternativePatterns = generateCommonPatterns();

        for (String pattern : alternativePatterns) {
            if (pattern.equals(request.getSchedulePattern())) continue;

            ScheduleCheckRequest altRequest = request.copy();
            altRequest.setSchedulePattern(pattern);

            // Điều chỉnh ngày bắt đầu để khớp với pattern mới
            LocalDate adjustedStartDate = findNextDateMatchingPattern(
                    request.getStartDate(), pattern);
            altRequest.setStartDate(adjustedStartDate);

            List<Room> rooms = getAvailableRooms(altRequest);
            List<Lecturer> lecturers = getAvailableLecturers(altRequest);

            if (!rooms.isEmpty() && !lecturers.isEmpty()) {
                ScheduleAlternative alt = new ScheduleAlternative();
                alt.setType("ALTERNATIVE_PATTERN");
                alt.setStartDate(adjustedStartDate);
                alt.setStartTime(request.getStartTime());
                alt.setEndTime(request.getStartTime().plusMinutes(request.getDurationMinutes()));
                alt.setSchedulePattern(pattern);
                alt.setAvailableRooms(ResourceConverter.fromRooms(rooms));
                alt.setAvailableLecturers(ResourceConverter.fromLecturers(lecturers));
                alt.setReason(String.format("Đổi lịch từ %s sang %s",
                        formatPatternToVietnamese(request.getSchedulePattern()),
                        formatPatternToVietnamese(pattern)));
                alt.setPriority(calculatePriority("PATTERN", rooms.size(), lecturers.size(), 0));

                alternatives.add(alt);
            }
        }

        return alternatives;
    }

    /**
     * Chiến lược 4: Gợi ý phòng thay thế
     */
    private List<ScheduleAlternative> suggestAlternativeRooms(ScheduleCheckRequest request) {
        List<ScheduleAlternative> alternatives = new ArrayList<>();

        List<Room> allRooms = roomRepository.findAll();

        for (Room room : allRooms) {
            if (room.getRoomId().equals(request.getPreferredRoomId())) continue;

            ScheduleCheckRequest altRequest = request.copy();
            altRequest.setPreferredRoomId(room.getRoomId());

            List<ConflictInfo> roomConflicts = conflictCheckService.checkRoomConflicts(
                    room.getRoomId(),
                    request.getSchedulePattern(),
                    request.getStartTime(),
                    request.getDurationMinutes(),
                    request.getStartDate(),
                    request.getExcludeClassId()
            );

            if (roomConflicts.isEmpty()) {
                List<Lecturer> lecturers = getAvailableLecturers(request);

                if (!lecturers.isEmpty()) {
                    ScheduleAlternative alt = new ScheduleAlternative();
                    alt.setType("ALTERNATIVE_ROOM");
                    alt.setStartDate(request.getStartDate());
                    alt.setStartTime(request.getStartTime());
                    alt.setEndTime(request.getStartTime().plusMinutes(request.getDurationMinutes()));
                    alt.setSchedulePattern(request.getSchedulePattern());
                    alt.setAvailableRooms(List.of(ResourceConverter.fromRoom(room)));
                    alt.setAvailableLecturers(ResourceConverter.fromLecturers(lecturers));
                    alt.setReason(String.format("Đổi phòng sang %s", room.getRoomName()));
                    alt.setPriority(calculatePriority("ROOM", 1, lecturers.size(), 0));

                    alternatives.add(alt);
                }
            }
        }

        return alternatives;
    }

    /**
     * Chiến lược 5: Gợi ý giảng viên thay thế
     */
    private List<ScheduleAlternative> suggestAlternativeLecturers(ScheduleCheckRequest request) {
        List<ScheduleAlternative> alternatives = new ArrayList<>();

        List<Lecturer> allLecturers = lecturerRepository.findAll();

        for (Lecturer lecturer : allLecturers) {
            if (lecturer.getLecturerId().equals(request.getPreferredLecturerId())) continue;

            ScheduleCheckRequest altRequest = request.copy();
            altRequest.setPreferredLecturerId(lecturer.getLecturerId());

            List<ConflictInfo> lecturerConflicts = conflictCheckService.checkTeacherConflicts(
                    lecturer.getLecturerId(),
                    request.getSchedulePattern(),
                    request.getStartTime(),
                    request.getDurationMinutes(),
                    request.getStartDate(),
                    request.getExcludeClassId()
            );

            if (lecturerConflicts.isEmpty()) {
                List<Room> rooms = getAvailableRooms(request);

                if (!rooms.isEmpty()) {
                    ScheduleAlternative alt = new ScheduleAlternative();
                    alt.setType("ALTERNATIVE_LECTURER");
                    alt.setStartDate(request.getStartDate());
                    alt.setStartTime(request.getStartTime());
                    alt.setEndTime(request.getStartTime().plusMinutes(request.getDurationMinutes()));
                    alt.setSchedulePattern(request.getSchedulePattern());
                    alt.setAvailableRooms(ResourceConverter.fromRooms(rooms));
                    alt.setAvailableLecturers(List.of(ResourceConverter.fromLecturer(lecturer)));
                    alt.setReason(String.format("Đổi giảng viên sang %s", lecturer.getFullName()));
                    alt.setPriority(calculatePriority("LECTURER", rooms.size(), 1, 0));

                    alternatives.add(alt);
                }
            }
        }

        return alternatives;
    }

    /**
     * Lấy danh sách phòng available
     */
    private List<Room> getAvailableRooms(ScheduleCheckRequest request) {
        List<Room> allRooms = roomRepository.findAll();
        List<Room> available = new ArrayList<>();

        for (Room room : allRooms) {
            List<ConflictInfo> conflicts = conflictCheckService.checkRoomConflicts(
                    room.getRoomId(),
                    request.getSchedulePattern(),
                    request.getStartTime(),
                    request.getDurationMinutes(),
                    request.getStartDate(),
                    request.getExcludeClassId()
            );

            if (conflicts.isEmpty()) {
                available.add(room);
            }
        }

        return available;
    }

    /**
     * Lấy danh sách giảng viên available
     */
    private List<Lecturer> getAvailableLecturers(ScheduleCheckRequest request) {
        List<Lecturer> allLecturers = lecturerRepository.findAll();
        List<Lecturer> available = new ArrayList<>();

        for (Lecturer lecturer : allLecturers) {
            List<ConflictInfo> conflicts = conflictCheckService.checkTeacherConflicts(
                    lecturer.getLecturerId(),
                    request.getSchedulePattern(),
                    request.getStartTime(),
                    request.getDurationMinutes(),
                    request.getStartDate(),
                    request.getExcludeClassId()
            );

            if (conflicts.isEmpty()) {
                available.add(lecturer);
            }
        }

        return available;
    }

    /**
     * Tính độ ưu tiên
     */
    private int calculatePriority(String type, int roomCount, int lecturerCount, int penalty) {
        int basePriority = switch (type) {
            case "TIME" -> 100;           // Ưu tiên cao nhất: Chỉ đổi giờ
            case "ROOM" -> 90;            // Chỉ đổi phòng
            case "LECTURER" -> 85;        // Chỉ đổi GV
            case "START_DATE" -> 80;      // Đổi ngày bắt đầu
            case "PATTERN" -> 60;         // Đổi pattern
            default -> 50;
        };

        // Bonus cho số lượng lựa chọn
        int bonus = Math.min(roomCount * 2 + lecturerCount * 2, 20);

        // Trừ điểm dựa trên penalty
        int penaltyScore = Math.min(penalty / 3600, 10);

        return basePriority + bonus - penaltyScore;
    }

    /**
     * Tìm ngày tiếp theo khớp với pattern
     */
    private LocalDate findNextDateMatchingPattern(LocalDate startDate, String patternStr) {
        CustomSchedulePattern pattern = new CustomSchedulePattern(patternStr);
        LocalDate date = startDate;

        while (!pattern.getDaysOfWeek().contains(date.getDayOfWeek())) {
            date = date.plusDays(1);
        }

        return date;
    }

    /**
     * Tạo danh sách các pattern phổ biến
     */
    private List<String> generateCommonPatterns() {
        return Arrays.asList(
                "2-4-6",      // T2, T4, T6
                "3-5-7",      // T3, T5, T7
                "2-4",        // T2, T4
                "3-5",        // T3, T5
                "2-6",        // T2, T6
                "4-6",        // T4, T6
                "7-1",        //T7,CN
                "1",          // Chủ nhật
                "7",          // Thứ 7
                "2-3-4-5-6",  // T2-T6
                "2-3-4",      // T2, T3, T4
                "4-5-6",      // T4, T5, T6
                "3-7",        // T3, T7
                "2-5",        // T2, T5
                "3-6"         // T3, T6
        );
    }

    /**
     * Format pattern sang tiếng Việt
     */
    private String formatPatternToVietnamese(String pattern) {
        Map<String, String> dayNames = Map.of(
                "1", "CN",
                "2", "T2",
                "3", "T3",
                "4", "T4",
                "5", "T5",
                "6", "T6",
                "7", "T7"
        );

        return Arrays.stream(pattern.split("-"))
                .map(dayNames::get)
                .collect(Collectors.joining(", "));
    }
}