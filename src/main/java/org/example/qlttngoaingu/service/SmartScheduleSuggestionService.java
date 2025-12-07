package org.example.qlttngoaingu.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.example.qlttngoaingu.dto.request.ScheduleCheckRequest;
import org.example.qlttngoaingu.dto.response.AvailabilityResult;
import org.example.qlttngoaingu.dto.response.ConflictInfo;
import org.example.qlttngoaingu.dto.response.ScheduleAlternative;
import org.example.qlttngoaingu.dto.response.ScheduleSuggestionResponse;
import org.example.qlttngoaingu.entity.Course;
import org.example.qlttngoaingu.entity.CourseClass;
import org.example.qlttngoaingu.entity.Lecturer;
import org.example.qlttngoaingu.entity.Room;
import org.example.qlttngoaingu.repository.LecturerRepository;
import org.example.qlttngoaingu.repository.RoomRepository;
import org.example.qlttngoaingu.utils.CustomSchedulePattern;
import org.example.qlttngoaingu.utils.ResourceConverter;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SmartScheduleSuggestionService {

    private final RoomRepository roomRepository;
    private final LecturerRepository lecturerRepository;
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

        // Chiến lược 1: Thử giờ khác (±1 giờ)
        alternatives.addAll(suggestAlternativeTimesInSameDay(request));

        // Chiến lược 2: Thử ngày bắt đầu khác (±7 ngày)
        alternatives.addAll(suggestAlternativeStartDates(request));

        // Chiến lược 3: Gợi ý phòng thay thế (nếu user đã chọn phòng cụ thể)
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

        // Gom chi tiết xung đột room + teacher thành 1 description
        List<ConflictInfo> allConflicts = new ArrayList<>();
        
        if (request.getPreferredRoomId() != null) {
            // User đã chọn phòng cụ thể → check phòng đó
            List<ConflictInfo> roomConflicts = conflictCheckService.checkRoomConflicts(
                    request.getPreferredRoomId(),
                    request.getSchedulePattern(),
                    request.getStartTime(),
                    request.getDurationMinutes(),
                    request.getStartDate(),
                    request.getExcludeClassId()
            );

            // Tạo description chi tiết cho mỗi room conflict
            for (ConflictInfo conflict : roomConflicts) {
                enrichConflictDescription(conflict, request.getPreferredRoomId(), null);
                allConflicts.add(conflict);
            }
        } else {
            // User chưa chọn phòng → check tất cả phòng có status "Sẵn sàng" để tìm conflict
            List<Room> allRooms = roomRepository.findAll().stream()
                    .filter(room -> "Sẵn sàng".equals(room.getStatus()))
                    .toList();
            for (Room room : allRooms) {
                List<ConflictInfo> roomConflicts = conflictCheckService.checkRoomConflicts(
                        room.getRoomId(),
                        request.getSchedulePattern(),
                        request.getStartTime(),
                        request.getDurationMinutes(),
                        request.getStartDate(),
                        request.getExcludeClassId()
                );
                
                for (ConflictInfo conflict : roomConflicts) {
                    enrichConflictDescription(conflict, room.getRoomId(), null);
                    allConflicts.add(conflict);
                }
            }
        }

        if (request.getPreferredLecturerId() != null) {
            // User đã chọn GV cụ thể → check GV đó
            List<ConflictInfo> lecturerConflicts = conflictCheckService.checkTeacherConflicts(
                    request.getPreferredLecturerId(),
                    request.getSchedulePattern(),
                    request.getStartTime(),
                    request.getDurationMinutes(),
                    request.getStartDate(),
                    request.getExcludeClassId()
            );

            // Tạo description chi tiết cho mỗi lecturer conflict
            for (ConflictInfo conflict : lecturerConflicts) {
                enrichConflictDescription(conflict, null, request.getPreferredLecturerId());
                allConflicts.add(conflict);
            }
        } else {
            // User chưa chọn GV → check tất cả GV để tìm conflict
            List<Lecturer> allLecturers = lecturerRepository.findAll();
            for (Lecturer lecturer : allLecturers) {
                List<ConflictInfo> lecturerConflicts = conflictCheckService.checkTeacherConflicts(
                        lecturer.getLecturerId(),
                        request.getSchedulePattern(),
                        request.getStartTime(),
                        request.getDurationMinutes(),
                        request.getStartDate(),
                        request.getExcludeClassId()
                );
                
                for (ConflictInfo conflict : lecturerConflicts) {
                    enrichConflictDescription(conflict, null, lecturer.getLecturerId());
                    allConflicts.add(conflict);
                }
            }
        }
        
        result.setRoomConflicts(allConflicts);
        result.setLecturerConflicts(new ArrayList<>()); // Để trống vì đã gom vào roomConflicts

        return result;
    }

    /**
     * Chiến lược 1: Gợi ý giờ khác trong khoảng ±1 giờ
     */
    private List<ScheduleAlternative> suggestAlternativeTimesInSameDay(ScheduleCheckRequest request) {
        List<ScheduleAlternative> alternatives = new ArrayList<>();

        // Tạo các khung giờ trong khoảng ±1 giờ (mỗi 30 phút)
        List<LocalTime> timeSlots = new ArrayList<>();
        LocalTime baseTime = request.getStartTime();
        
        // -60, -30, +30, +60 phút
        timeSlots.add(baseTime.minusMinutes(60));
        timeSlots.add(baseTime.minusMinutes(30));
        timeSlots.add(baseTime.plusMinutes(30));
        timeSlots.add(baseTime.plusMinutes(60));

        for (LocalTime altTime : timeSlots) {
            // Bỏ qua giờ không hợp lý (quá sớm hoặc quá muộn)
            if (altTime.isBefore(LocalTime.of(7, 0)) || altTime.isAfter(LocalTime.of(21, 0))) {
                continue;
            }

            long diffMinutes = Math.abs(Duration.between(request.getStartTime(), altTime).toMinutes());

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
                int diff = (int) diffMinutes;

                alt.setPriority(calculatePriority(
                        "TIME",
                        rooms.size(),
                        lecturers.size(),
                        diff
                ));


                alternatives.add(alt);
            }
        }


        return alternatives;
    }

    /**
     * Chiến lược 2: Gợi ý ngày bắt đầu khác (±7 ngày)
     */
    private List<ScheduleAlternative> suggestAlternativeStartDates(ScheduleCheckRequest request) {
        List<ScheduleAlternative> alternatives = new ArrayList<>();
        CustomSchedulePattern pattern = new CustomSchedulePattern(request.getSchedulePattern());

        // Kiểm tra cả trước và sau ngày gốc
        LocalDate startSearch = request.getStartDate().minusDays(7);
        LocalDate endSearch = request.getStartDate().plusDays(7);
        LocalDate currentDate = startSearch;
        int daysChecked = 0;

        while (currentDate.isBefore(endSearch) && alternatives.size() < 5) {
            // Bỏ qua ngày gốc
            if (currentDate.equals(request.getStartDate())) {
                currentDate = currentDate.plusDays(1);
                continue;
            }
            
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
                    
                    int daysDiff = Math.abs((int) java.time.temporal.ChronoUnit.DAYS.between(request.getStartDate(), currentDate));
                    alt.setPriority(calculatePriority("START_DATE", rooms.size(), lecturers.size(), daysDiff));

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
        // Chỉ lấy phòng có status = "Sẵn sàng"
        List<Room> allRooms = roomRepository.findAll().stream()
                .filter(room -> "Sẵn sàng".equals(room.getStatus()))
                .toList();
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

    /**
     * Enrich conflict description với thông tin chi tiết
     */
    private void enrichConflictDescription(ConflictInfo conflict, Integer roomId, Integer lecturerId) {
        StringBuilder desc = new StringBuilder();
        
        // Lấy thông tin lớp xung đột từ existingClass đã có sẵn trong conflict
        CourseClass conflictClass = conflict.getExistingClass();
        
        if (conflictClass == null) {
            conflict.setDescription("Không tìm thấy thông tin lớp xung đột");
            return;
        }
        
        // Thông tin cơ bản
        desc.append("Lớp ").append(conflictClass.getClassName())
            .append(" (").append(conflictClass.getCourse().getCourseName()).append(") ");
        
        // Thông tin phòng
        if (roomId != null && conflictClass.getRoom() != null) {
            desc.append("đang học tại phòng ")
                .append(conflictClass.getRoom().getRoomName()).append(" ");
        }
        
        // Thông tin giảng viên
        if (lecturerId != null && conflictClass.getLecturer() != null) {
            desc.append("với GV ")
                .append(conflictClass.getLecturer().getFullName()).append(" ");
        }
        
        // Thông tin thời gian
        if (conflictClass.getSchedule() != null && !conflictClass.getSchedule().isEmpty()) {
            desc.append("vào ")
                .append(formatPatternToVietnamese(conflictClass.getSchedule()))
                .append(", ");
        }
        
        if (conflictClass.getStartTime() != null) {
            desc.append(conflictClass.getStartTime());
            
            if (conflictClass.getMinutesPerSession() != null) {
                LocalTime endTime = conflictClass.getStartTime().plusMinutes(conflictClass.getMinutesPerSession());
                desc.append(" - ").append(endTime);
            }
        }
        
        // Ngày bắt đầu
        if (conflictClass.getStartDate() != null) {
            desc.append(", bắt đầu từ ").append(conflictClass.getStartDate());
            
            // Tính và hiển thị dự kiến ngày kết thúc
            LocalDate estimatedEndDate = calculateEstimatedEndDate(
                    conflictClass.getStartDate(),
                    conflictClass.getSchedule(),
                    conflictClass.getCourse()
            );
            
            if (estimatedEndDate != null) {
                desc.append(", dự kiến kết thúc ").append(estimatedEndDate);
            }
        }
        
        conflict.setDescription(desc.toString());
    }

    /**
     * Tính ngày kết thúc dự kiến dựa trên startDate, schedule pattern và số giờ học
     */
    private LocalDate calculateEstimatedEndDate(LocalDate startDate, String schedulePattern, Course course) {
        if (startDate == null || schedulePattern == null || course == null) {
            return null;
        }
        
        // Lấy số giờ học từ khóa học
        Integer totalHours = course.getStudyHours();
        if (totalHours == null || totalHours <= 0) {
            return null;
        }
        
        // Giả sử mỗi buổi học 1.5 giờ (90 phút)
        int hoursPerSession = 2; // Có thể lấy từ minutesPerSession nếu có
        int totalSessions = (int) Math.ceil((double) totalHours / hoursPerSession);
        
        // Parse schedule pattern để biết học mấy ngày/tuần
        String[] days = schedulePattern.split("-");
        int sessionsPerWeek = days.length;
        
        if (sessionsPerWeek == 0) {
            return null;
        }
        
        // Tính số tuần cần học
        int weeksNeeded = (int) Math.ceil((double) totalSessions / sessionsPerWeek);
        
        // Cộng thêm số tuần vào startDate
        return startDate.plusWeeks(weeksNeeded);
    }
}