package org.example.qlttngoaingu.service;

import lombok.RequiredArgsConstructor;
import org.example.qlttngoaingu.dto.request.ClassCreationRequest;
import org.example.qlttngoaingu.dto.request.ScheduleCheckRequest;
import org.example.qlttngoaingu.dto.response.*;
import org.example.qlttngoaingu.entity.*;
import org.example.qlttngoaingu.exception.AppException;
import org.example.qlttngoaingu.exception.ErrorCode;
import org.example.qlttngoaingu.mapper.CourseClassMapper;
import org.example.qlttngoaingu.mapper.SessionMapper;
import org.example.qlttngoaingu.repository.*;
import org.example.qlttngoaingu.service.enums.ClassStatusEnum;
import org.example.qlttngoaingu.service.enums.RoleEnum;
import org.example.qlttngoaingu.service.enums.SessionStatus;
import org.example.qlttngoaingu.specification.CourseClassSpec;
import org.example.qlttngoaingu.utils.CustomSchedulePattern;
import org.example.qlttngoaingu.utils.ScheduleUltis;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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
    private final ConflictCheckService conflictCheckService;
    private final SmartScheduleSuggestionService smartScheduleSuggestionService;
    private final SessionMapper sessionMapper;
    private final CourseClassMapper courseClassMapper;
    private final InvoiceDetailRepository invoiceDetailRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final GradeSheetRepository gradeSheetRepository;

    private final List<String> periods = List.of("Sáng", "Chiều", "Tối");
    // @Transactional
    // public ScheduleSuggestionResponse changeStatus(Integer classId){
    //
    // CourseClass courseClass = classRepository.getCourseClassByClassId((classId));
    // if(courseClass.getStatus() == ClassStatusEnum.InProgress.name())
    // {
    // courseClass.setStatus(false);
    // return null;
    // }
    // ScheduleCheckRequest createScheduleCheckRequest =
    // courseClassMapper.toScheduleCheckRequest(courseClass);
    // ScheduleSuggestionResponse scheduleSuggestionResponse =
    // smartScheduleSuggestionService .checkAndSuggest(createScheduleCheckRequest);
    // if(Objects.equals(scheduleSuggestionResponse.getStatus(), "AVAILABLE"))
    // {
    // courseClass.setStatus(true);
    // return null;
    // }
    //
    // return scheduleSuggestionResponse;
    // }

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

        // Validate schedule pattern
        CustomSchedulePattern pattern = new CustomSchedulePattern(request.getSchedule());

        // Check conflicts using ConflictCheckService
        List<ConflictInfo> roomConflicts = conflictCheckService.checkRoomConflicts(
                request.getRoomId(),
                request.getSchedule(),
                request.getStartTime(),
                request.getMinutesPerSession(),
                request.getStartDate(),
                null);
        if (!roomConflicts.isEmpty()) {
            throw new RuntimeException("Room conflict: " + roomConflicts.get(0).getDescription());
        }

        if (lecturer != null) {
            List<ConflictInfo> teacherConflicts = conflictCheckService.checkTeacherConflicts(
                    request.getLecturerId(),
                    request.getSchedule(),
                    request.getStartTime(),
                    request.getMinutesPerSession(),
                    request.getStartDate(),
                    null);
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
        cls.setStatus(ClassStatusEnum.InProgress.name());
        cls = classRepository.save(cls);

        List<Session> sessions = generateScheduleSessions(
                cls,
                course,
                pattern,
                request.getStartDate(),
                request.getStartTime(),
                request.getMinutesPerSession());

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
                s.setStatus(SessionStatus.NotCompleted.name());
                s.setNote("Session " + seq);
                sessions.add(s);
                created++;
                seq++;
            }
            date = date.plusDays(1);
        }

        return sessions;
    }

    private ClassCreationResponse buildResponse(
            CourseClass cls,
            Course course,
            Room room,
            Lecturer lecturer,
            List<Session> sessions) {

        ClassCreationResponse resp = new ClassCreationResponse();
        resp.setClassId(cls.getClassId());
        resp.setClassName(cls.getClassName());

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
        response.setMinutePerSession(cls.getMinutesPerSession());

        // Tính endDate dựa trên các buổi học
        List<Session> sessions = sessionRepository.findByCourseClass_ClassIdOrderBySessionDate(cls.getClassId());
        if (!sessions.isEmpty()) {
            response.setEndDate(sessions.get(sessions.size() - 1).getSessionDate());
        }

        response.setRoomName(cls.getRoom() != null ? cls.getRoom().getRoomName() : null);
        response.setInstructorName(cls.getLecturer() != null ? cls.getLecturer().getFullName() : null);
        response.setLecturerId(cls.getLecturer() != null ? cls.getLecturer().getLecturerId() : null);
        response.setCourseId(cls.getCourse() != null ? cls.getCourse().getCourseId() : null);
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
        List<Student> studentEntities = invoiceDetailRepository.findStudentsByClassId(classId);

        // Lấy tất cả điểm của lớp
        List<GradeSheet> allGrades = gradeSheetRepository.findAllByClassId(classId);
        
        // Map grades theo studentId
        Map<Integer, List<GradeSheet>> gradesByStudentId = allGrades.stream()
                .collect(Collectors.groupingBy(g -> 
                    g.getEnrollment().getInvoice().getStudent().getId()
                ));

        List<ClassDetailResponse.StudentInClass> studentList = studentEntities.stream()
                .map(s -> {
                    ClassDetailResponse.StudentInClass dto = new ClassDetailResponse.StudentInClass();

                    // Map từ Entity Student
                    dto.setStudentId(s.getId());
                    dto.setFullName(s.getName());
                    dto.setAvatar(s.getAvatar());
                    dto.setGender(s.getGender());

                    if (s.getAccount() != null) {
                        dto.setEmail(s.getAccount().getEmail());
                        dto.setPhone(s.getAccount().getPhoneNumber());
                    }
                    
                    // Tính điểm trung bình cho học sinh
                    List<GradeSheet> studentGrades = gradesByStudentId.get(s.getId());
                    if (studentGrades != null && !studentGrades.isEmpty()) {
                        dto.setAverageScore(calculateAverageScore(studentGrades));
                    }

                    return dto;
                })
                .toList();
        response.setStudents(studentList);
        response.setSessions(sessionInfos);
        response.setMaxCapacity(cls.getRoom().getCapacity());
        Integer enrollmentCount = invoiceDetailRepository.countByClassIdAndActiveInvoice(classId);

        response.setCurrentEnrollment(enrollmentCount);
        return response;
    }

    public ClassResponse getAllClasses(int page, int size) {
        PageRequest pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Order.asc("startDate"), Sort.Order.desc("status")));

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
            
            // Tính số buổi học chưa bù dựa trên tổng số buổi
            Integer courseStudyHours = cls.getCourse().getStudyHours();
            Integer minutesPerSession = cls.getMinutesPerSession();
            int requiredSessions = (courseStudyHours * 60) / minutesPerSession; // Số buổi lý thuyết
            
            long canceledCount = sessions.stream()
                .filter(session -> "Canceled".equalsIgnoreCase(session.getStatus()))
                .count();
            
            // Số buổi có hiệu lực = Tổng buổi - Buổi đã hủy
            int effectiveSessions = sessions.size() - (int) canceledCount;
            
            // Số buổi chưa bù = Số buổi cần thiết - Số buổi có hiệu lực
            int pendingSessions = Math.max(0, requiredSessions - effectiveSessions);
            
            info.setHasPendingMakeup(pendingSessions > 0);
            info.setCanceledSessionsCount(pendingSessions);
            
            info.setMaxCapacity(cls.getRoom().getCapacity());
            Integer enrollmentCount = invoiceDetailRepository.countByClassIdAndActiveInvoice(cls.getClassId());

            info.setCurrentEnrollment(enrollmentCount);

            info.setStartTime(cls.getStartTime());
            info.setEndTime(cls.getStartTime().plusMinutes(cls.getMinutesPerSession()));
            info.setSchedulePattern(cls.getSchedule());
            info.setStatus(cls.getStatus());

            // Set tuitionFee từ Course
            if (cls.getCourse() != null && cls.getCourse().getTuitionFee() != null) {
                info.setTuitionFee(cls.getCourse().getTuitionFee());
            }

            return info;
        }).toList();

        ClassResponse response = new ClassResponse();
        response.setCurrentPage(classPage.getNumber());
        response.setTotalPages(classPage.getTotalPages());
        response.setTotalItems(classPage.getTotalElements());
        response.setClasses(classInfos);

        return response;
    }

    public ClassScheduleResponse getScheduleOfAllClassByCourseId(int courseId) {
        Set<CourseClass> courseClasses = classRepository.findByCourse_CourseIdAndStatus(courseId,
                ClassStatusEnum.InProgress.name());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        Set<String> times = courseClasses.stream()
                .map(c -> c.getStartTime().format(formatter))
                .collect(Collectors.toSet());

        Set<String> schedules = courseClasses.stream().map(CourseClass::getSchedule).collect(Collectors.toSet());
        ClassScheduleResponse response = new ClassScheduleResponse();
        response.setSchedulePatterns(schedules);
        response.setScheduleTimes(times);
        return response;
    }

    public List<ClassResponse.ClassInfo> filterClasses(
            Integer lecturerId,
            Integer roomId,
            Integer courseId,
            String className) {

        Specification<CourseClass> spec = Specification
                .where(CourseClassSpec.hasLecturer(lecturerId))
                .and(CourseClassSpec.hasRoom(roomId))
                .and(CourseClassSpec.hasCourse(courseId))
                .and(CourseClassSpec.hasClassName(className));
        return classRepository.findAll(spec)
                .stream()
                .map(cls -> {
                    ClassResponse.ClassInfo info = courseClassMapper.toDto(cls);

                    // Lấy danh sách session của lớp
                    List<Session> sessions = sessionRepository
                            .findByCourseClass_ClassIdOrderBySessionDate(cls.getClassId());

                    if (!sessions.isEmpty()) {
                        info.setEndDate(sessions.get(sessions.size() - 1).getSessionDate());
                    }
                    
                    // Tính số buổi học chưa bù dựa trên tổng số buổi
                    Integer courseStudyHours = cls.getCourse().getStudyHours();
                    Integer minutesPerSession = cls.getMinutesPerSession();
                    int requiredSessions = (courseStudyHours * 60) / minutesPerSession; // Số buổi lý thuyết
                    
                    long canceledCount = sessions.stream()
                        .filter(session -> "Canceled".equalsIgnoreCase(session.getStatus()))
                        .count();
                    
                    // Số buổi có hiệu lực = Tổng buổi - Buổi đã hủy
                    int effectiveSessions = sessions.size() - (int) canceledCount;
                    
                    // Số buổi chưa bù = Số buổi cần thiết - Số buổi có hiệu lực
                    int pendingSessions = Math.max(0, requiredSessions - effectiveSessions);
                    
                    info.setHasPendingMakeup(pendingSessions > 0);
                    info.setCanceledSessionsCount(pendingSessions);
                    
                    info.setMaxCapacity(cls.getRoom().getCapacity());
                    Integer enrollmentCount = invoiceDetailRepository.countByClassIdAndActiveInvoice(cls.getClassId());
                    if (!sessions.isEmpty()) {
                        info.setEndDate(sessions.get(sessions.size() - 1).getSessionDate());
                    }
                    info.setEndTime(cls.getStartTime().plusMinutes(cls.getMinutesPerSession()));

                    info.setCurrentEnrollment(enrollmentCount);

                    // Set thời gian
                    info.setStartTime(cls.getStartTime());
                    info.setEndTime(cls.getStartTime().plusMinutes(cls.getMinutesPerSession()));

                    // Set tuitionFee từ Course
                    if (cls.getCourse() != null && cls.getCourse().getTuitionFee() != null) {
                        info.setTuitionFee(cls.getCourse().getTuitionFee());
                    }

                    return info;
                })
                .toList();
    }

    public WeeklyScheduleResponse getWeeklySchedule(
            Integer lecturerId,
            Integer roomId,
            Integer courseId,
            LocalDate dateInWeek) {
        LocalDate weekStart = dateInWeek.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = weekStart.plusDays(6);

        // Lấy tất cả session trong tuần
        List<Session> sessions = sessionRepository.findBySessionDateBetween(weekStart, weekEnd);

        // filter theo giảng viên, phòng, khóa học
        if (lecturerId != null) {
            sessions = sessions.stream()
                    .filter(s -> s.getCourseClass().getLecturer() != null
                            && s.getCourseClass().getLecturer().getLecturerId().equals(lecturerId))
                    .collect(Collectors.toList());
        }
        if (roomId != null) {
            sessions = sessions.stream()
                    .filter(s -> s.getCourseClass().getRoom() != null
                            && s.getCourseClass().getRoom().getRoomId().equals(roomId))
                    .collect(Collectors.toList());
        }
        if (courseId != null) {
            sessions = sessions.stream()
                    .filter(s -> s.getCourseClass().getCourse() != null
                            && s.getCourseClass().getCourse().getCourseId().equals(courseId))
                    .collect(Collectors.toList());
        }

        // Nhóm theo ngày -> ca
        Map<LocalDate, Map<String, List<WeeklyScheduleResponse.SessionInfo>>> tempSchedule = new TreeMap<>();

        sessions.forEach(s -> {
            WeeklyScheduleResponse.SessionInfo info = sessionMapper.toDto(s);
            String period = ScheduleUltis.getSessionPeriod(s.getCourseClass().getStartTime());
            LocalDate day = s.getSessionDate();

            tempSchedule
                    .computeIfAbsent(day, k -> new TreeMap<>())
                    .computeIfAbsent(period, k -> new ArrayList<>())
                    .add(info);
        });

        // Tạo DTO tuần, đảm bảo mỗi ngày có 3 ca
        List<WeeklyScheduleResponse.DaySchedule> days = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate currentDay = weekStart.plusDays(i);
            Map<String, List<WeeklyScheduleResponse.SessionInfo>> daySessions = tempSchedule.getOrDefault(currentDay,
                    new HashMap<>());

            WeeklyScheduleResponse.DaySchedule daySchedule = new WeeklyScheduleResponse.DaySchedule();
            daySchedule.setDate(currentDay);
            daySchedule.setDayName(currentDay.getDayOfWeek().toString());

            List<WeeklyScheduleResponse.PeriodSchedule> periodSchedules = new ArrayList<>();
            for (String period : periods) {
                WeeklyScheduleResponse.PeriodSchedule ps = new WeeklyScheduleResponse.PeriodSchedule();
                ps.setPeriod(period);
                ps.setSessions(daySessions.getOrDefault(period, new ArrayList<>()));
                periodSchedules.add(ps);
            }
            daySchedule.setPeriods(periodSchedules);
            days.add(daySchedule);
        }

        WeeklyScheduleResponse response = new WeeklyScheduleResponse();
        response.setWeekStart(weekStart);
        response.setWeekEnd(weekEnd);
        response.setDays(days);

        return response;
    }

    @Transactional
    public ClassCreationResponse updateClass(Integer classId, ClassCreationRequest request) {

        CourseClass cls = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found"));

        Lecturer lecturer = null;
        if (request.getLecturerId() != null) {
            lecturer = lecturerRepository.findById(request.getLecturerId())
                    .orElseThrow(() -> new RuntimeException("Lecturer not found"));
        }

        // Validate schedule pattern
        CustomSchedulePattern pattern = new CustomSchedulePattern(request.getSchedule());

        // ======== CHECK ROOM CONFLICT EXCEPT THIS CLASS ========
        List<ConflictInfo> roomConflicts = conflictCheckService.checkRoomConflicts(
                request.getRoomId(),
                request.getSchedule(),
                request.getStartTime(),
                request.getMinutesPerSession(),
                request.getStartDate(),
                classId // bỏ qua conflict với chính nó
        );
        if (!roomConflicts.isEmpty()) {
            throw new RuntimeException("Room conflict: " + roomConflicts.get(0).getDescription());
        }

        // ======== CHECK TEACHER CONFLICT EXCEPT THIS CLASS ========
        if (lecturer != null) {
            List<ConflictInfo> teacherConflicts = conflictCheckService.checkTeacherConflicts(
                    request.getLecturerId(),
                    request.getSchedule(),
                    request.getStartTime(),
                    request.getMinutesPerSession(),
                    request.getStartDate(),
                    classId);
            if (!teacherConflicts.isEmpty()) {
                throw new RuntimeException("Lecturer conflict: " + teacherConflicts.get(0).getDescription());
            }
        }

        // ======== UPDATE PROPERTIES ========
        cls.setCourse(course);
        cls.setRoom(room);
        cls.setLecturer(lecturer);
        cls.setClassName(request.getClassName());
        cls.setSchedule(request.getSchedule());
        cls.setStartTime(request.getStartTime());
        cls.setMinutesPerSession(request.getMinutesPerSession());
        cls.setStartDate(request.getStartDate());
        cls.setNote(request.getNote());

        // ======== REMOVE OLD SESSIONS ========
        List<Session> oldSessions = sessionRepository.findByCourseClass_ClassIdOrderBySessionDate(classId);
        sessionRepository.deleteAll(oldSessions);

        // ======== GENERATE NEW SESSIONS ========
        List<Session> newSessions = generateScheduleSessions(
                cls,
                course,
                pattern,
                request.getStartDate(),
                request.getStartTime(),
                request.getMinutesPerSession());

        sessionRepository.saveAll(newSessions);

        // ======== RETURN RESPONSE ========
        return buildResponse(cls, course, room, lecturer, newSessions);
    }

    public WeeklyScheduleResponse getWeeklyScheduleByUser(Integer id, LocalDate dateInWeek) {
        LocalDate weekStart = dateInWeek.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = weekStart.plusDays(6);
        User user = userRepository.getUserByUserId(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        List<Session> sessions = new ArrayList<>();
        if (Objects.equals(user.getRole(), RoleEnum.STUDENT.name())) {
            Student student = studentRepository.getStudentByAccount_UserId(id);
            List<Integer> classIds = classRepository.findRegisteredClassIds(student.getId());
            sessions = sessionRepository.findByCourseClass_ClassIdInAndSessionDateBetweenAndStatusNot(
                    classIds, weekStart, weekEnd, "Canceled");

        }
        if (Objects.equals(user.getRole(), RoleEnum.TEACHER.name())) {
            Lecturer lecturer = lecturerRepository.getByUser_UserId(id);
            List<Integer> classIds = classRepository
                    .findIdsByLecturer_LecturerIdAndStatusNot(lecturer.getLecturerId(), "Closed");

            sessions = sessionRepository.findByCourseClass_ClassIdInAndSessionDateBetweenAndStatusNot(
                    classIds, weekStart, weekEnd, "Canceled");

        }

        // Nhóm theo ngày -> ca
        Map<LocalDate, Map<String, List<WeeklyScheduleResponse.SessionInfo>>> tempSchedule = new TreeMap<>();

        sessions.forEach(s -> {
            WeeklyScheduleResponse.SessionInfo info = sessionMapper.toDto(s);
            String period = ScheduleUltis.getSessionPeriod(s.getCourseClass().getStartTime());
            LocalDate day = s.getSessionDate();

            tempSchedule
                    .computeIfAbsent(day, k -> new TreeMap<>())
                    .computeIfAbsent(period, k -> new ArrayList<>())
                    .add(info);
        });

        // Tạo DTO tuần, đảm bảo mỗi ngày có 3 ca
        List<WeeklyScheduleResponse.DaySchedule> days = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate currentDay = weekStart.plusDays(i);
            Map<String, List<WeeklyScheduleResponse.SessionInfo>> daySessions = tempSchedule.getOrDefault(currentDay,
                    new HashMap<>());

            WeeklyScheduleResponse.DaySchedule daySchedule = new WeeklyScheduleResponse.DaySchedule();
            daySchedule.setDate(currentDay);
            daySchedule.setDayName(currentDay.getDayOfWeek().toString());

            List<WeeklyScheduleResponse.PeriodSchedule> periodSchedules = new ArrayList<>();
            for (String period : periods) {
                WeeklyScheduleResponse.PeriodSchedule ps = new WeeklyScheduleResponse.PeriodSchedule();
                ps.setPeriod(period);
                ps.setSessions(daySessions.getOrDefault(period, new ArrayList<>()));
                periodSchedules.add(ps);
            }
            daySchedule.setPeriods(periodSchedules);
            days.add(daySchedule);
        }

        WeeklyScheduleResponse response = new WeeklyScheduleResponse();
        response.setWeekStart(weekStart);
        response.setWeekEnd(weekEnd);
        response.setDays(days);
        return response;
    }

    public List<ClassResponse.ClassInfo> getClassByUser(Integer userId) {

        User user = userRepository.getUserByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if (Objects.equals(user.getRole(), RoleEnum.STUDENT.name())) {

            Student student = studentRepository.getStudentByAccount_UserId(userId);
            List<CourseClass> classes = classRepository.findRegisteredClasses(student.getId());

            return classes.stream()
                    .map(cls -> {
                        ClassResponse.ClassInfo info = courseClassMapper.toDto(cls);

                        info.setMaxCapacity(cls.getRoom().getCapacity());
                        Integer enrollmentCount = invoiceDetailRepository
                                .countByClassIdAndActiveInvoice(cls.getClassId());
                        List<Session> sessions = sessionRepository
                                .findByCourseClass_ClassIdOrderBySessionDate(cls.getClassId());
                        if (!sessions.isEmpty()) {
                            info.setEndDate(sessions.get(sessions.size() - 1).getSessionDate());
                        }
                        info.setEndTime(cls.getStartTime().plusMinutes(cls.getMinutesPerSession()));

                        info.setCurrentEnrollment(enrollmentCount);

                        // Set tuitionFee từ Course
                        if (cls.getCourse() != null && cls.getCourse().getTuitionFee() != null) {
                            info.setTuitionFee(cls.getCourse().getTuitionFee());
                        }
                        return info;
                    })
                    .collect(Collectors.toList());
        }

        else if (Objects.equals(user.getRole(), RoleEnum.TEACHER.name())) {
            Lecturer lecturer = lecturerRepository.getByUser_UserId(userId);
            List<CourseClass> classes = classRepository
                    .findByLecturer_LecturerIdAndStatusNot(lecturer.getLecturerId(), ClassStatusEnum.Closed.name());
            return classes.stream()
                    .map(cls -> {
                        ClassResponse.ClassInfo info = courseClassMapper.toDto(cls);

                        info.setMaxCapacity(cls.getRoom().getCapacity());
                        Integer enrollmentCount = invoiceDetailRepository
                                .countByClassIdAndActiveInvoice(cls.getClassId());
                        List<Session> sessions = sessionRepository
                                .findByCourseClass_ClassIdOrderBySessionDate(cls.getClassId());
                        if (!sessions.isEmpty()) {
                            info.setEndDate(sessions.get(sessions.size() - 1).getSessionDate());
                        }
                        info.setEndTime(cls.getStartTime().plusMinutes(cls.getMinutesPerSession()));

                        info.setCurrentEnrollment(enrollmentCount);

                        // Set tuitionFee từ Course
                        if (cls.getCourse() != null && cls.getCourse().getTuitionFee() != null) {
                            info.setTuitionFee(cls.getCourse().getTuitionFee());
                        }
                        return info;
                    })
                    .collect(Collectors.toList());
        }
        return null;
    }

    /**
     * Lấy danh sách lớp học của giảng viên
     */
    public List<ClassResponse.ClassInfo> getClassesByLecturer(Integer userId) {
        Lecturer lecturer = lecturerRepository.getByUser_UserId(userId);
        if (lecturer == null) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        List<CourseClass> classes = classRepository
                .findByLecturer_LecturerIdAndStatusNot(lecturer.getLecturerId(), ClassStatusEnum.Closed.name());

        return classes.stream()
                .map(cls -> {
                    ClassResponse.ClassInfo info = courseClassMapper.toDto(cls);
                    info.setMaxCapacity(cls.getRoom().getCapacity());
                    Integer enrollmentCount = invoiceDetailRepository.countByClassIdAndActiveInvoice(cls.getClassId());
                    List<Session> sessions = sessionRepository
                            .findByCourseClass_ClassIdOrderBySessionDate(cls.getClassId());
                    if (!sessions.isEmpty()) {
                        info.setEndDate(sessions.get(sessions.size() - 1).getSessionDate());
                    }
                    info.setEndTime(cls.getStartTime().plusMinutes(cls.getMinutesPerSession()));
                    info.setCurrentEnrollment(enrollmentCount);

                    // Set tuitionFee từ Course
                    if (cls.getCourse() != null && cls.getCourse().getTuitionFee() != null) {
                        info.setTuitionFee(cls.getCourse().getTuitionFee());
                    }
                    return info;
                })
                .collect(Collectors.toList());
    }

    /**
     * Lấy chi tiết lớp học cho giảng viên
     */
    public ClassDetailResponse getClassDetailForTeacher(Integer userId, Integer classId) {
        Lecturer lecturer = lecturerRepository.getByUser_UserId(userId);
        if (lecturer == null) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        CourseClass cls = classRepository.findById(classId)
                .orElseThrow(() -> new AppException(ErrorCode.CLASS_NOT_FOUND));

        // Verify teacher owns this class
        if (!cls.getLecturer().getLecturerId().equals(lecturer.getLecturerId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        return getClass(classId);
    }

    /**
     * Lấy danh sách học viên trong lớp (cho giảng viên)
     */
    public List<ClassDetailResponse.StudentInClass> getStudentsByClassForTeacher(Integer userId, Integer classId) {
        Lecturer lecturer = lecturerRepository.getByUser_UserId(userId);
        if (lecturer == null) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        CourseClass cls = classRepository.findById(classId)
                .orElseThrow(() -> new AppException(ErrorCode.CLASS_NOT_FOUND));

        // Verify teacher owns this class
        if (!cls.getLecturer().getLecturerId().equals(lecturer.getLecturerId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        ClassDetailResponse detail = getClass(classId);
        return detail.getStudents() != null ? detail.getStudents() : new ArrayList<>();
    }

    // public ClassResponse findByStudent(Integer userId) {
    // Student student =
    // studentRepository.findByAccount_UserId(userId).orElseThrow(() -> new
    // AppException(ErrorCode.USER_NOT_FOUND);
    // List<CourseClass> courseClasses =
    // invoiceDetailRepository.findAllByHocVienId(student.getId());
    // courseClassMapper.
    // }

    /**
     * Tính điểm trung bình từ danh sách điểm
     * Công thức: attendance 10% + midterm 30% + final 60%
     */
    private Double calculateAverageScore(List<GradeSheet> grades) {
        if (grades == null || grades.isEmpty()) return null;
        
        BigDecimal attendance = null, midterm = null, finalScore = null;
        
        for (GradeSheet grade : grades) {
            String gradeType = grade.getGradeType();
            if (gradeType == null || grade.getScore() == null) continue;
            
            // Match by grade type name: "Chuyên cần", "Giữa kỳ", "Cuối kỳ"
            if (gradeType.contains("Chuyên cần") || gradeType.equalsIgnoreCase("Attendance")) {
                attendance = grade.getScore();
            } else if (gradeType.contains("Giữa kỳ") || gradeType.equalsIgnoreCase("Midterm")) {
                midterm = grade.getScore();
            } else if (gradeType.contains("Cuối kỳ") || gradeType.equalsIgnoreCase("Final")) {
                finalScore = grade.getScore();
            }
        }
        
        // Tính điểm tổng kết theo trọng số
        if (attendance == null && midterm == null && finalScore == null) {
            return null;
        }
        
        // Nếu có đầy đủ điểm thì tính theo công thức
        if (attendance != null && midterm != null && finalScore != null) {
            BigDecimal total = attendance.multiply(new BigDecimal("0.1"))
                    .add(midterm.multiply(new BigDecimal("0.3")))
                    .add(finalScore.multiply(new BigDecimal("0.6")));
            return total.setScale(2, RoundingMode.HALF_UP).doubleValue();
        }
        
        // Nếu thiếu điểm thì tính trung bình các điểm có
        int count = 0;
        BigDecimal sum = BigDecimal.ZERO;
        if (attendance != null) { sum = sum.add(attendance); count++; }
        if (midterm != null) { sum = sum.add(midterm); count++; }
        if (finalScore != null) { sum = sum.add(finalScore); count++; }
        
        return count > 0 ? sum.divide(new BigDecimal(count), 2, RoundingMode.HALF_UP).doubleValue() : null;
    }

    /**
     * Cập nhật trạng thái và ghi chú của buổi học
     */
    @Transactional
    public ClassDetailResponse.SessionInfoDetail updateSession(
            Integer sessionId, 
            org.example.qlttngoaingu.dto.request.SessionUpdateRequest request) {
        
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new AppException(ErrorCode.CLASS_NOT_FOUND));
        
        // Cập nhật trạng thái nếu có
        if (request.getStatus() != null && !request.getStatus().isEmpty()) {
            // Validate status
            try {
                SessionStatus.valueOf(request.getStatus());
                session.setStatus(request.getStatus());
            } catch (IllegalArgumentException e) {
                throw new AppException(ErrorCode.INVALID_REQUEST);
            }
        }
        
        // Cập nhật ghi chú nếu có
        if (request.getNote() != null) {
            session.setNote(request.getNote());
        }
        
        sessionRepository.save(session);
        
        // Trả về thông tin session đã cập nhật
        ClassDetailResponse.SessionInfoDetail info = new ClassDetailResponse.SessionInfoDetail();
        info.setSessionId(session.getSessionId());
        info.setDate(session.getSessionDate());
        info.setNote(session.getNote());
        info.setStatus(session.getStatus());
        
        return info;
    }

    /**
     * Hủy buổi học - đổi status thành "Đã hủy"
     */
    @Transactional
    public ClassDetailResponse.SessionInfoDetail cancelSession(Integer sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new AppException(ErrorCode.CLASS_NOT_FOUND));
        
        // Kiểm tra buổi học đã bị hủy chưa
        if (SessionStatus.Canceled.name().equals(session.getStatus())) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        
        session.setStatus(SessionStatus.Canceled.name());
        sessionRepository.save(session);
        
        // Trả về thông tin session đã hủy
        ClassDetailResponse.SessionInfoDetail info = new ClassDetailResponse.SessionInfoDetail();
        info.setSessionId(session.getSessionId());
        info.setDate(session.getSessionDate());
        info.setNote(session.getNote());
        info.setStatus(session.getStatus());
        
        return info;
    }

    /**
     * Thêm buổi học mới vào lớp
     * Chỉ được thêm khi có buổi học đã bị hủy
     * Logic: Mỗi lần thêm buổi, kiểm tra có còn "slot" từ buổi đã hủy không
     */
    @Transactional
    public ClassDetailResponse.SessionInfoDetail addSession(
            Integer classId,
            org.example.qlttngoaingu.dto.request.SessionCreateRequest request) {
        
        // Kiểm tra lớp học có tồn tại không
        CourseClass courseClass = classRepository.findById(classId)
                .orElseThrow(() -> new AppException(ErrorCode.CLASS_NOT_FOUND));
        
        // Lấy tất cả buổi học của lớp
        List<Session> allSessions = sessionRepository.findByCourseClass_ClassIdOrderBySessionDate(classId);
        
        // Đếm số buổi đã hủy
        long canceledCount = allSessions.stream()
                .filter(s -> SessionStatus.Canceled.name().equals(s.getStatus()))
                .count();
        
        // Nếu không có buổi nào bị hủy, không cho phép thêm
        if (canceledCount == 0) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        
        // Lấy số giờ học từ khóa học
        Integer courseStudyHours = courseClass.getCourse().getStudyHours();
        Integer minutesPerSession = courseClass.getMinutesPerSession();
        
        // Tính số buổi học ban đầu dựa trên giờ học
        // Giả sử: sobuoihoc = (sogiohoc * 60) / sogiohocmoibuoi
        int originalSessionCount = (courseStudyHours * 60) / minutesPerSession;
        
        // Số buổi đã thêm = tổng số buổi hiện tại - số buổi ban đầu
        long addedSessions = allSessions.size() - originalSessionCount;
        
        if (addedSessions >= canceledCount) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        
        // Kiểm tra trùng ngày học (không cho phép thêm buổi học cùng ngày)
        boolean isDuplicateDate = allSessions.stream()
                .anyMatch(s -> s.getSessionDate().equals(request.getSessionDate()));
        
        if (isDuplicateDate) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        
        // Kiểm tra xung đột với lớp khác (cùng phòng hoặc cùng giảng viên)
        LocalDate sessionDate = request.getSessionDate();
        DayOfWeek dayOfWeek = sessionDate.getDayOfWeek();
        
        // Tạo schedule pattern cho ngày này (ví dụ: "2" cho Thứ 2)
        String singleDaySchedule = String.valueOf(dayOfWeek.getValue());
        
        // Kiểm tra xung đột phòng
        if (courseClass.getRoom() != null) {
            List<ConflictInfo> roomConflicts = conflictCheckService.checkRoomConflicts(
                    courseClass.getRoom().getRoomId(),
                    singleDaySchedule,
                    courseClass.getStartTime(),
                    courseClass.getMinutesPerSession(),
                    sessionDate,
                    courseClass.getClassId()
            );
            
            if (!roomConflicts.isEmpty()) {
                throw new AppException(ErrorCode.INVALID_REQUEST);
            }
        }
        
        // Kiểm tra xung đột giảng viên
        if (courseClass.getLecturer() != null) {
            List<ConflictInfo> lecturerConflicts = conflictCheckService.checkTeacherConflicts(
                    courseClass.getLecturer().getLecturerId(),
                    singleDaySchedule,
                    courseClass.getStartTime(),
                    courseClass.getMinutesPerSession(),
                    sessionDate,
                    courseClass.getClassId()
            );
            
            if (!lecturerConflicts.isEmpty()) {
                throw new AppException(ErrorCode.INVALID_REQUEST);
            }
        }
        
        // Tạo buổi học mới
        Session newSession = new Session();
        newSession.setCourseClass(courseClass);
        newSession.setSessionDate(request.getSessionDate());
        newSession.setStatus(SessionStatus.NotCompleted.name());
        newSession.setNote(request.getNote());
        
        sessionRepository.save(newSession);
        
        // Trả về thông tin buổi học mới
        ClassDetailResponse.SessionInfoDetail info = new ClassDetailResponse.SessionInfoDetail();
        info.setSessionId(newSession.getSessionId());
        info.setDate(newSession.getSessionDate());
        info.setNote(newSession.getNote());
        info.setStatus(newSession.getStatus());
        
        return info;
    }

    /**
     * Gợi ý các ngày phù hợp để thêm buổi học bù
     * Kiểm tra không trùng lịch phòng và giảng viên
     */
    public List<LocalDate> suggestMakeupDates(Integer classId, Integer daysAhead) {
        // Lấy thông tin lớp học
        CourseClass courseClass = classRepository.findById(classId)
                .orElseThrow(() -> new AppException(ErrorCode.CLASS_NOT_FOUND));
        
        // Lấy tất cả buổi học hiện tại
        List<Session> allSessions = sessionRepository.findByCourseClass_ClassIdOrderBySessionDate(classId);
        
        // Kiểm tra có thể thêm buổi học không
        long canceledCount = allSessions.stream()
                .filter(s -> SessionStatus.Canceled.name().equals(s.getStatus()))
                .count();
        
        if (canceledCount == 0) {
            return Collections.emptyList(); // Không có buổi nào bị hủy
        }
        
        Integer courseStudyHours = courseClass.getCourse().getStudyHours();
        Integer minutesPerSession = courseClass.getMinutesPerSession();
        int originalSessionCount = (courseStudyHours * 60) / minutesPerSession;
        long addedSessions = allSessions.size() - originalSessionCount;
        
        if (addedSessions >= canceledCount) {
            return Collections.emptyList(); // Đã thêm đủ số buổi
        }
        
        // Tạo set các ngày đã có buổi học
        Set<LocalDate> existingDates = allSessions.stream()
                .map(Session::getSessionDate)
                .collect(Collectors.toSet());
        
        // Gợi ý các ngày trong khoảng thời gian
        List<LocalDate> suggestions = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(daysAhead);
        
        for (LocalDate date = today; !date.isAfter(endDate); date = date.plusDays(1)) {
            // Bỏ qua ngày đã có buổi học
            if (existingDates.contains(date)) {
                continue;
            }
            
            // Bỏ qua Chủ nhật (hoặc theo quy định của trung tâm)
            if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
                continue;
            }
            
            // Kiểm tra xung đột
            boolean hasConflict = false;
            String singleDaySchedule = String.valueOf(date.getDayOfWeek().getValue());
            
            // Check phòng
            if (courseClass.getRoom() != null) {
                List<ConflictInfo> roomConflicts = conflictCheckService.checkRoomConflicts(
                        courseClass.getRoom().getRoomId(),
                        singleDaySchedule,
                        courseClass.getStartTime(),
                        courseClass.getMinutesPerSession(),
                        date,
                        courseClass.getClassId()
                );
                if (!roomConflicts.isEmpty()) {
                    hasConflict = true;
                }
            }
            
            // Check giảng viên
            if (!hasConflict && courseClass.getLecturer() != null) {
                List<ConflictInfo> lecturerConflicts = conflictCheckService.checkTeacherConflicts(
                        courseClass.getLecturer().getLecturerId(),
                        singleDaySchedule,
                        courseClass.getStartTime(),
                        courseClass.getMinutesPerSession(),
                        date,
                        courseClass.getClassId()
                );
                if (!lecturerConflicts.isEmpty()) {
                    hasConflict = true;
                }
            }
            
            // Thêm vào danh sách nếu không có xung đột
            if (!hasConflict) {
                suggestions.add(date);
            }
            
            // Giới hạn số lượng gợi ý
            if (suggestions.size() >= 10) {
                break;
            }
        }
        
        return suggestions;
    }
}