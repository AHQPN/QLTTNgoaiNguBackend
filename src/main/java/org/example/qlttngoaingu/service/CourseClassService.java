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
    private final CourseClassMapper  courseClassMapper;
    private final InvoiceDetailRepository invoiceDetailRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;

    private final List<String> periods = List.of("Sáng", "Chiều", "Tối");
//    @Transactional
//    public ScheduleSuggestionResponse changeStatus(Integer classId){
//
//        CourseClass courseClass = classRepository.getCourseClassByClassId((classId));
//        if(courseClass.getStatus() ==  ClassStatusEnum.InProgress.name())
//        {
//            courseClass.setStatus(false);
//            return null;
//        }
//        ScheduleCheckRequest createScheduleCheckRequest =  courseClassMapper.toScheduleCheckRequest(courseClass);
//        ScheduleSuggestionResponse scheduleSuggestionResponse =
//                smartScheduleSuggestionService .checkAndSuggest(createScheduleCheckRequest);
//        if(Objects.equals(scheduleSuggestionResponse.getStatus(), "AVAILABLE"))
//        {
//            courseClass.setStatus(true);
//            return null;
//        }
//
//        return scheduleSuggestionResponse;
//    }

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
                null
        );
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
        cls.setStatus(ClassStatusEnum.InProgress.name());
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
            info.setMaxCapacity(cls.getRoom().getCapacity());
            Integer enrollmentCount = invoiceDetailRepository.countByClassIdAndActiveInvoice(cls.getClassId());


            info.setCurrentEnrollment(enrollmentCount);

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

    public ClassScheduleResponse getScheduleOfAllClassByCourseId(int courseId) {
        Set<CourseClass> courseClasses =
                classRepository.findByCourse_CourseIdAndStatus(courseId,ClassStatusEnum.InProgress.name());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        Set<String> times =  courseClasses.stream()
                .map(c -> c.getStartTime().format(formatter))
                .collect(Collectors.toSet());

        Set<String> schedules =  courseClasses.stream().
                map(CourseClass::getSchedule).collect(Collectors.toSet());
        ClassScheduleResponse response = new ClassScheduleResponse();
        response.setSchedulePatterns(schedules);
        response.setScheduleTimes(times);
        return response;
    }

    public List<ClassResponse.ClassInfo> filterClasses(
            Integer lecturerId,
            Integer roomId,
            Integer courseId,
            String className
    ) {

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
                    List<Session> sessions =
                            sessionRepository.findByCourseClass_ClassIdOrderBySessionDate(cls.getClassId());

                    if (!sessions.isEmpty()) {
                        info.setEndDate(sessions.get(sessions.size() - 1).getSessionDate());
                    }
                    info.setMaxCapacity(cls.getRoom().getCapacity());
                    Integer enrollmentCount =
                            invoiceDetailRepository.countByClassIdAndActiveInvoice(cls.getClassId());
                    if (!sessions.isEmpty()) {
                        info.setEndDate(sessions.get(sessions.size() - 1).getSessionDate());
                    }
                    info.setEndTime(cls.getStartTime().plusMinutes(cls.getMinutesPerSession()));

                    info.setCurrentEnrollment(enrollmentCount);

                    // Set thời gian
                    info.setStartTime(cls.getStartTime());
                    info.setEndTime(cls.getStartTime().plusMinutes(cls.getMinutesPerSession()));

                    return info;
                })
                .toList();
    }


    public WeeklyScheduleResponse getWeeklySchedule(
            Integer lecturerId,
            Integer roomId,
            Integer courseId,
            LocalDate dateInWeek
    ) {
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
            Map<String, List<WeeklyScheduleResponse.SessionInfo>> daySessions = tempSchedule.getOrDefault(currentDay, new HashMap<>());

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
                    classId
            );
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
                request.getMinutesPerSession()
        );

        sessionRepository.saveAll(newSessions);

        // ======== RETURN RESPONSE ========
        return buildResponse(cls, course, room, lecturer, newSessions);
    }

    public WeeklyScheduleResponse getWeeklyScheduleByUser(Integer id, LocalDate dateInWeek) {
        LocalDate weekStart = dateInWeek.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = weekStart.plusDays(6);
        User user = userRepository.getUserByUserId(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        List<Session> sessions = new ArrayList<>();
        if(Objects.equals(user.getRole(), RoleEnum.STUDENT.name()))
        {
            Student student = studentRepository.getStudentByAccount_UserId(id);
            List<Integer> classIds = classRepository.findRegisteredClassIds(student.getId());
            sessions = sessionRepository.findByCourseClass_ClassIdInAndSessionDateBetweenAndStatusNot(
                    classIds, weekStart, weekEnd, "Canceled"
            );

        }
        if(Objects.equals(user.getRole(), RoleEnum.TEACHER.name()))
        {
            Lecturer lecturer = lecturerRepository.getByUser_UserId(id);
            List<Integer> classIds = classRepository
                    .findIdsByLecturer_LecturerIdAndStatusNot(lecturer.getLecturerId(), "Closed");

            sessions = sessionRepository.findByCourseClass_ClassIdInAndSessionDateBetweenAndStatusNot(
                    classIds, weekStart, weekEnd, "Canceled"
            );

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
            Map<String, List<WeeklyScheduleResponse.SessionInfo>> daySessions = tempSchedule.getOrDefault(currentDay, new HashMap<>());

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

        User user = userRepository.getUserByUserId(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if (Objects.equals(user.getRole(), RoleEnum.STUDENT.name())) {

            Student student = studentRepository.getStudentByAccount_UserId(userId);
            List<CourseClass> classes = classRepository.findRegisteredClasses(student.getId());

            return classes.stream()
                    .map(cls -> {
                        ClassResponse.ClassInfo info = courseClassMapper.toDto(cls);

                        info.setMaxCapacity(cls.getRoom().getCapacity());
                        Integer enrollmentCount =
                                invoiceDetailRepository.countByClassIdAndActiveInvoice(cls.getClassId());
                        List<Session> sessions = sessionRepository.findByCourseClass_ClassIdOrderBySessionDate(cls.getClassId());
                        if (!sessions.isEmpty()) {
                            info.setEndDate(sessions.get(sessions.size() - 1).getSessionDate());
                        }
                        info.setEndTime(cls.getStartTime().plusMinutes(cls.getMinutesPerSession()));

                        info.setCurrentEnrollment(enrollmentCount);
                        return info;
                    })
                    .collect(Collectors.toList());
        }

        else if(Objects.equals(user.getRole(), RoleEnum.TEACHER.name()))
        {
            Lecturer lecturer = lecturerRepository.getByUser_UserId(userId);
            List<CourseClass> classes = classRepository
                    .findByLecturer_LecturerIdAndStatusNot(lecturer.getLecturerId(),ClassStatusEnum.Closed.name());
            return classes.stream()
                    .map(cls -> {
                        ClassResponse.ClassInfo info = courseClassMapper.toDto(cls);

                        info.setMaxCapacity(cls.getRoom().getCapacity());
                        Integer enrollmentCount =
                                invoiceDetailRepository.countByClassIdAndActiveInvoice(cls.getClassId());
                        List<Session> sessions = sessionRepository.findByCourseClass_ClassIdOrderBySessionDate(cls.getClassId());
                        if (!sessions.isEmpty()) {
                            info.setEndDate(sessions.get(sessions.size() - 1).getSessionDate());
                        }
                        info.setEndTime(cls.getStartTime().plusMinutes(cls.getMinutesPerSession()));

                        info.setCurrentEnrollment(enrollmentCount);
                        return info;
                    })
                    .collect(Collectors.toList());
        }
        return null;
    }

//    public ClassResponse findByStudent(Integer userId) {
//        Student student = studentRepository.findByAccount_UserId(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND);
//        List<CourseClass> courseClasses = invoiceDetailRepository.findAllByHocVienId(student.getId());
//        courseClassMapper.
//    }
}