package org.example.qlttngoaingu.service;

import lombok.RequiredArgsConstructor;
import org.example.qlttngoaingu.dto.request.ClassCreationRequest;
import org.example.qlttngoaingu.dto.response.ClassCreationResponse;
import org.example.qlttngoaingu.dto.response.ClassDetailResponse;
import org.example.qlttngoaingu.dto.response.ClassResponse;
import org.example.qlttngoaingu.dto.response.ConflictInfo;
import org.example.qlttngoaingu.entity.*;
import org.example.qlttngoaingu.exception.AppException;
import org.example.qlttngoaingu.exception.ErrorCode;
import org.example.qlttngoaingu.repository.*;
import org.example.qlttngoaingu.utils.CustomSchedulePattern;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseClassService {

    private final CourseRepository courseRepository;
    private final RoomRepository roomRepository;
    private final LecturerRepository lecturerRepository;
    private final CourseClassRepository classRepository;
    private final SessionRepository sessionRepository;

    @Transactional
    public ClassCreationResponse createClass(ClassCreationRequest request) {
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found"));

        Lecturer lecturer = null;
        if (request.getLecturerId() != null) {
            lecturer = lecturerRepository.findById(request.getLecturerId())
                    .orElseThrow(() -> new RuntimeException("Lecturer not found"));
        }

        CustomSchedulePattern pattern = new CustomSchedulePattern(request.getSchedule());

        List<ConflictInfo> roomConflicts = checkRoomConflicts(
                request.getRoomId(),
                request.getSchedule(),
                request.getStartTime(),
                request.getMinutesPerSession(),
                request.getStartDate(),
                null
        );
        if (!roomConflicts.isEmpty()) {
            throw new RuntimeException("Room conflict: " + roomConflicts.get(0).getDescription());
        }

        if (lecturer != null) {
            List<ConflictInfo> teacherConflicts = checkTeacherConflicts(
                    request.getLecturerId(),
                    request.getSchedule(),
                    request.getStartTime(),
                    request.getMinutesPerSession(),
                    request.getStartDate(),
                    null
            );
            if (!teacherConflicts.isEmpty()) {
                throw new RuntimeException("Lecturer conflict: " + teacherConflicts.get(0).getDescription());
            }
        }

        CourseClass cls = new CourseClass();
        cls.setCourse(course);
        cls.setClassName(request.getClassName());
        cls.setRoom(room);
        cls.setLecturer(lecturer);
        cls.setSchedule(request.getSchedule());
        cls.setStartTime(request.getStartTime());
        cls.setMinutesPerSession(request.getMinutesPerSession());
        cls.setStartDate(request.getStartDate());
        cls.setNote(request.getNote());
        cls.setDateCreated(LocalDateTime.now());

        cls = classRepository.save(cls);

        List<Session> sessions = generateScheduleSessions(
                cls,
                course,
                pattern,
                request.getStartDate(),
                request.getStartTime(),
                request.getMinutesPerSession()
        );

        sessionRepository.saveAll(sessions);

        return buildResponse(cls, course, room, lecturer, sessions);
    }

    @Transactional
    protected List<Session> generateScheduleSessions(
            CourseClass cls,
            Course course,
            CustomSchedulePattern pattern,
            LocalDate startDate,
            LocalTime startTime,
            Integer minutesPerSession) {

        List<Session> sessions = new ArrayList<>();

        BigDecimal totalMinutes = BigDecimal.valueOf(course.getStudyHours())
                .multiply(BigDecimal.valueOf(60));

        int totalSessions = totalMinutes
                .divide(BigDecimal.valueOf(minutesPerSession), 0, RoundingMode.CEILING)
                .intValue();

        LocalDate date = startDate;
        int created = 0;
        int seq = 1;

        while (created < totalSessions) {
            if (pattern.getDaysOfWeek().contains(date.getDayOfWeek())) {
                Session s = new Session();
                s.setCourseClass(cls);
                s.setSessionDate(date);
                s.setStatus(false);
                s.setNote("Session " + seq);
                sessions.add(s);
                created++;
                seq++;
            }
            date = date.plusDays(1);
        }

        return sessions;
    }

    private List<ConflictInfo> checkRoomConflicts(
            Integer roomId,
            String schedule,
            LocalTime startTime,
            Integer minutesPerSession,
            LocalDate startDate,
            Integer excludeClassId) {

        List<ConflictInfo> conflicts = new ArrayList<>();
        CustomSchedulePattern newPattern = new CustomSchedulePattern(schedule);
        LocalTime newEnd = startTime.plusMinutes(minutesPerSession);

        List<CourseClass> otherClasses = classRepository.findByRoom_RoomIdAndStatusTrue(roomId);
        for (CourseClass other : otherClasses) {
            if (excludeClassId != null && Objects.equals(other.getClassId(), excludeClassId)) continue;

            LocalDate otherEndDate = calculateEndDate(other);
            if (otherEndDate != null && otherEndDate.isBefore(startDate)) continue;

            LocalDate newClassEndDate = calculateEndDate(startDate, other.getCourse().getStudyHours(),
                    minutesPerSession, newPattern);
            if (newClassEndDate.isBefore(other.getStartDate())) continue;

            Set<DayOfWeek> common = intersectDays(newPattern, other);
            if (common.isEmpty()) continue;

            LocalTime otherStart = other.getStartTime();
            LocalTime otherEnd = otherStart.plusMinutes(other.getMinutesPerSession());
            boolean overlap = timeOverlaps(startTime, newEnd, otherStart, otherEnd);
            if (overlap) {
                ConflictInfo c = new ConflictInfo();
                c.setType("ROOM_CONFLICT");
                c.setDescription(String.format("Room '%s' conflicts with class '%s' on %s (%s-%s).",
                        Optional.ofNullable(other.getRoom()).map(Room::getRoomName).orElse(String.valueOf(roomId)),
                        Optional.ofNullable(other.getCourse()).map(Course::getCourseName).orElse("unknown"),
                        formatDays(common), otherStart, otherEnd));
                conflicts.add(c);
            }
        }

        return conflicts;
    }

    private List<ConflictInfo> checkTeacherConflicts(
            Integer lecturerId,
            String schedule,
            LocalTime startTime,
            Integer minutesPerSession,
            LocalDate startDate,
            Integer excludeClassId) {

        List<ConflictInfo> conflicts = new ArrayList<>();
        CustomSchedulePattern newPattern = new CustomSchedulePattern(schedule);
        LocalTime newEnd = startTime.plusMinutes(minutesPerSession);

        List<CourseClass> otherClasses = classRepository.findByLecturer_LecturerIdAndStatusTrue(lecturerId);
        for (CourseClass other : otherClasses) {
            if (excludeClassId != null && Objects.equals(other.getClassId(), excludeClassId)) continue;

            LocalDate otherEndDate = calculateEndDate(other);
            if (otherEndDate != null && otherEndDate.isBefore(startDate)) continue;

            LocalDate newClassEndDate = calculateEndDate(startDate, other.getCourse().getStudyHours(),
                    minutesPerSession, newPattern);
            if (newClassEndDate.isBefore(other.getStartDate())) continue;

            Set<DayOfWeek> common = intersectDays(newPattern, other);
            if (common.isEmpty()) continue;

            LocalTime otherStart = other.getStartTime();
            LocalTime otherEnd = otherStart.plusMinutes(other.getMinutesPerSession());
            boolean overlap = timeOverlaps(startTime, newEnd, otherStart, otherEnd);
            if (overlap) {
                ConflictInfo c = new ConflictInfo();
                c.setType("TEACHER_CONFLICT");
                c.setDescription(String.format("Lecturer has conflict with class '%s' on %s (%s-%s).",
                        Optional.ofNullable(other.getCourse()).map(Course::getCourseName).orElse("unknown"),
                        formatDays(common), otherStart, otherEnd));
                conflicts.add(c);
            }
        }

        return conflicts;
    }

    private String formatDays(Set<DayOfWeek> days) {
        Map<DayOfWeek, String> names = Map.of(
                DayOfWeek.MONDAY, "MON",
                DayOfWeek.TUESDAY, "TUE",
                DayOfWeek.WEDNESDAY, "WED",
                DayOfWeek.THURSDAY, "THU",
                DayOfWeek.FRIDAY, "FRI",
                DayOfWeek.SATURDAY, "SAT",
                DayOfWeek.SUNDAY, "SUN"
        );
        return days.stream()
                .sorted(Comparator.comparingInt(DayOfWeek::getValue))
                .map(names::get)
                .collect(Collectors.joining(", "));
    }

    private ClassCreationResponse buildResponse(
            CourseClass cls,
            Course course,
            Room room,
            Lecturer lecturer,
            List<Session> sessions) {

        ClassCreationResponse resp = new ClassCreationResponse();
        resp.setClassId(cls.getClassId());
        resp.setCourseName(Optional.ofNullable(course).map(Course::getCourseName).orElse(null));
        resp.setRoomName(Optional.ofNullable(room).map(Room::getRoomName).orElse(null));
        resp.setInstructorName(Optional.ofNullable(lecturer).map(Lecturer::getFullName).orElse(null));
        resp.setSchedulePattern(cls.getSchedule());
        resp.setStartDate(cls.getStartDate());
        resp.setStartTime(cls.getStartTime());
        resp.setEndTime(cls.getStartTime().plusMinutes(cls.getMinutesPerSession()));
        resp.setTotalSessions(sessions.size());

        List<ClassCreationResponse.SessionInfo> infos = sessions.stream().map(s -> {
            ClassCreationResponse.SessionInfo i = new ClassCreationResponse.SessionInfo();
            i.setSessionId(s.getSessionId());
            i.setDate(s.getSessionDate());
            return i;
        }).collect(Collectors.toList());

        resp.setSessions(infos);
        resp.setEndDate(infos.isEmpty() ? null : infos.get(infos.size() - 1).getDate());

        return resp;
    }

    private LocalDate calculateEndDate(CourseClass cls) {
        double totalHours = cls.getCourse().getStudyHours();
        double hoursPerSession = cls.getMinutesPerSession() / 60.0;
        int totalSessions = (int) Math.ceil(totalHours / hoursPerSession);

        LocalDate d = cls.getStartDate();
        CustomSchedulePattern pattern = new CustomSchedulePattern(cls.getSchedule());

        int count = 0;
        while (count < totalSessions) {
            if (pattern.getDaysOfWeek().contains(d.getDayOfWeek())) count++;
            d = d.plusDays(1);
        }
        return d.minusDays(1);
    }

    private LocalDate calculateEndDate(
            LocalDate startDate,
            double totalHours,
            int minutesPerSession,
            CustomSchedulePattern pattern) {

        double hoursPerSession = minutesPerSession / 60.0;
        int totalSessions = (int) Math.ceil(totalHours / hoursPerSession);

        LocalDate d = startDate;
        int count = 0;
        while (count < totalSessions) {
            if (pattern.getDaysOfWeek().contains(d.getDayOfWeek())) count++;
            d = d.plusDays(1);
        }
        return d.minusDays(1);
    }

    private Set<DayOfWeek> intersectDays(CustomSchedulePattern pattern, CourseClass other) {
        CustomSchedulePattern otherPattern = new CustomSchedulePattern(other.getSchedule());
        Set<DayOfWeek> result = new HashSet<>(pattern.getDaysOfWeek());
        result.retainAll(otherPattern.getDaysOfWeek());
        return result;
    }

    private boolean timeOverlaps(LocalTime aStart, LocalTime aEnd, LocalTime bStart, LocalTime bEnd) {
        return aStart.isBefore(bEnd) && aEnd.isAfter(bStart);
    }

    public ClassDetailResponse getClass(Integer classId) {
        CourseClass cls = classRepository.findById(classId)
                .orElseThrow(() -> new AppException(ErrorCode.CLASS_NOT_FOUND));

        ClassDetailResponse response = new ClassDetailResponse();
        response.setClassId(cls.getClassId());
        response.setClassName(cls.getClassName());
        response.setCourseName(cls.getCourse().getCourseName());
        response.setSchedulePattern(cls.getSchedule());
        response.setStartTime(cls.getStartTime());
        response.setEndTime(cls.getStartTime().plusMinutes(cls.getMinutesPerSession()));
        response.setStartDate(cls.getStartDate());

        List<Session> sessions = sessionRepository.findByCourseClass_ClassIdOrderBySessionDate(cls.getClassId());
        if (!sessions.isEmpty()) {
            response.setEndDate(sessions.get(sessions.size() - 1).getSessionDate());
        }

        response.setRoomName(cls.getRoom() != null ? cls.getRoom().getRoomName() : null);
        response.setInstructorName(cls.getLecturer() != null ? cls.getLecturer().getFullName() : null);
        response.setTotalSessions(sessions.size());

        List<ClassDetailResponse.SessionInfoDetail> sessionInfos = sessions.stream()
                .map(s -> {
                    ClassDetailResponse.SessionInfoDetail info = new ClassDetailResponse.SessionInfoDetail();
                    info.setSessionId(s.getSessionId());
                    info.setDate(s.getSessionDate());
                    info.setNote(s.getNote());
                    info.setStatus(s.getStatus());
                    return info;
                })
                .toList();

        response.setSessions(sessionInfos);

        return response;
    }

    public ClassResponse getAllClasses(int page, int size) {
        PageRequest pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Order.asc("startDate"), Sort.Order.desc("status"))
        );

        Page<CourseClass> classPage = classRepository.findAll(pageable);

        List<ClassResponse.ClassInfo> classInfos = classPage.stream().map(cls -> {
            ClassResponse.ClassInfo info = new ClassResponse.ClassInfo();
            info.setClassId(cls.getClassId());
            info.setClassName(cls.getClassName());
            info.setCourseName(cls.getCourse().getCourseName());
            info.setRoomName(cls.getRoom() != null ? cls.getRoom().getRoomName() : null);
            info.setInstructorName(cls.getLecturer() != null ? cls.getLecturer().getFullName() : null);
            info.setStartDate(cls.getStartDate());

            List<Session> sessions = sessionRepository.findByCourseClass_ClassIdOrderBySessionDate(cls.getClassId());
            if (!sessions.isEmpty()) {
                info.setEndDate(sessions.get(sessions.size() - 1).getSessionDate());
            }

            info.setStartTime(cls.getStartTime());
            info.setEndTime(cls.getStartTime().plusMinutes(cls.getMinutesPerSession()));
            info.setSchedulePattern(cls.getSchedule());
            info.setStatus(cls.getStatus());

            return info;
        }).toList();

        ClassResponse response = new ClassResponse();
        response.setCurrentPage(classPage.getNumber());
        response.setTotalPages(classPage.getTotalPages());
        response.setTotalItems(classPage.getTotalElements());
        response.setClasses(classInfos);

        return response;
    }
}
