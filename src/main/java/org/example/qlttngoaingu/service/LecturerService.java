package org.example.qlttngoaingu.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.example.qlttngoaingu.dto.request.LecturerCreationRequest;
import org.example.qlttngoaingu.dto.request.LecturerRequest;
import org.example.qlttngoaingu.dto.request.LecturerUpdateRequest;
import org.example.qlttngoaingu.dto.response.AvailableLecturerResponse;
import org.example.qlttngoaingu.dto.response.LecturerDashboardStatsResponse;
import org.example.qlttngoaingu.dto.response.LecturerResponse;
import org.example.qlttngoaingu.dto.response.TeacherInfo;
import org.example.qlttngoaingu.entity.Course;
import org.example.qlttngoaingu.entity.CourseClass;
import org.example.qlttngoaingu.entity.Degree;
import org.example.qlttngoaingu.entity.Lecturer;
import org.example.qlttngoaingu.entity.LecturerDegree;
import org.example.qlttngoaingu.entity.User;
import org.example.qlttngoaingu.exception.AppException;
import org.example.qlttngoaingu.exception.ErrorCode;
import org.example.qlttngoaingu.repository.AttendanceRepository;
import org.example.qlttngoaingu.repository.CourseClassRepository;
import org.example.qlttngoaingu.repository.CourseReviewRepository;
import org.example.qlttngoaingu.repository.DegreeRepository;
import org.example.qlttngoaingu.repository.InvoiceDetailRepository;
import org.example.qlttngoaingu.repository.LecturerDegreeRepository;
import org.example.qlttngoaingu.repository.LecturerRepository;
import org.example.qlttngoaingu.repository.SessionRepository;
import org.example.qlttngoaingu.repository.UserRepository;
import org.example.qlttngoaingu.service.enums.ClassStatusEnum;
import org.example.qlttngoaingu.service.enums.RoleEnum;
import org.example.qlttngoaingu.service.enums.SchedulePattern;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LecturerService {

    private final LecturerRepository lecturerRepository;
    private final LecturerDegreeRepository lecturerDegreeRepository;
    private final UserRepository userRepository;
    private final CourseClassRepository classRepository;
    private final InvoiceDetailRepository invoiceDetailRepository;
    private final CourseReviewRepository courseReviewRepository;
    private final DegreeRepository degreeRepository;
    private final PasswordEncoder passwordEncoder;
    private final SessionRepository sessionRepository;
    private final AttendanceRepository attendanceRepository;

    // ADD LECTURER BASIC INFO (User đã tồn tại)

    @Transactional
    public void addLecturerInfo(LecturerCreationRequest request, Integer userId) {
        Lecturer lecturer = new Lecturer();
        lecturer.setFullName(request.getName());
        lecturer.setDateOfBirth(request.getDateOfBirth());
        lecturer.setImagePath(request.getImageUrl());
        lecturer.setUser(userRepository.findById(userId).get());
        lecturerRepository.save(lecturer);
    }

    /**
     * Lấy Lecturer theo userId (từ User đang đăng nhập)
     */
    public Lecturer getLecturerByUserId(Integer userId) {
        Lecturer lecturer = lecturerRepository.getByUser_UserId(userId);
        if (lecturer == null) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }
        return lecturer;
    }

    // CHECK AVAILABLE LECTURERS

    public List<AvailableLecturerResponse> getAvailableLecturers(
            String schedulePattern,
            LocalTime startTime,
            Integer durationMinutes,
            LocalDate startDate) {
        List<Lecturer> lecturers = lecturerRepository.findAll();
        SchedulePattern pattern;

        try {
            pattern = SchedulePattern.fromPattern(schedulePattern);
        } catch (Exception e) {
            return Collections.emptyList();
        }

        LocalTime endTime = startTime.plusMinutes(durationMinutes);
        List<AvailableLecturerResponse> result = new ArrayList<>();

        for (Lecturer lecturer : lecturers) {
            boolean available = checkLecturerAvailability(
                    lecturer.getLecturerId(),
                    pattern,
                    startTime,
                    endTime,
                    startDate);

            if (available) {
                AvailableLecturerResponse dto = new AvailableLecturerResponse();
                dto.setLecturerId(lecturer.getLecturerId());
                dto.setLecturerName(lecturer.getFullName());
                result.add(dto);
            }
        }
        return result;
    }

    private boolean checkLecturerAvailability(
            Integer lecturerId,
            SchedulePattern pattern,
            LocalTime startTime,
            LocalTime endTime,
            LocalDate startDate) {
        List<CourseClass> classes = classRepository.findByLecturer_LecturerIdAndStatus(
                lecturerId, ClassStatusEnum.InProgress.name());

        for (CourseClass cls : classes) {

            LocalDate classEndDate = calculateClassEndDate(cls);
            if (classEndDate.isBefore(startDate))
                continue;

            SchedulePattern classPattern = SchedulePattern.fromPattern(cls.getSchedule());

            Set<DayOfWeek> commonDays = new HashSet<>(pattern.getDaysOfWeek());
            commonDays.retainAll(classPattern.getDaysOfWeek());

            if (commonDays.isEmpty())
                continue;

            if (cls.getStartTime() != null) {
                LocalTime classEndTime = cls.getStartTime().plusMinutes(cls.getMinutesPerSession());

                boolean overlap = !(endTime.isBefore(cls.getStartTime())
                        || startTime.isAfter(classEndTime));

                if (overlap)
                    return false;
            }
        }
        return true;
    }

    private LocalDate calculateClassEndDate(CourseClass cls) {
        Course course = cls.getCourse();
        SchedulePattern pattern = SchedulePattern.fromPattern(cls.getSchedule());

        BigDecimal totalMinutes = BigDecimal.valueOf(course.getStudyHours()).multiply(BigDecimal.valueOf(60));

        int totalSessions = totalMinutes
                .divide(BigDecimal.valueOf(cls.getMinutesPerSession()), 0, RoundingMode.CEILING)
                .intValue();

        LocalDate date = cls.getStartDate();
        int created = 0;

        while (created < totalSessions) {
            if (pattern.getDaysOfWeek().contains(date.getDayOfWeek())) {
                created++;
            }
            date = date.plusDays(1);
        }

        return date.minusDays(1);
    }

    public List<AvailableLecturerResponse> getAllLecturers() {
        return lecturerRepository.findAll()
                .stream()
                .map(r -> {
                    AvailableLecturerResponse dto = new AvailableLecturerResponse();
                    dto.setLecturerId(r.getLecturerId());
                    dto.setLecturerName(r.getFullName());
                    return dto;
                })
                .toList();
    }

    // GET LECTURER DETAIL (Role-based)

    @Transactional(readOnly = true)
    public TeacherInfo getLecturerById(Integer userId, Integer lecturerId) {

        User usr = userRepository.getUserByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Lecturer lecturer;
        boolean isAdmin = usr.getRole().equalsIgnoreCase("ADMIN");

        if (isAdmin) {
            if (lecturerId == null)
                throw new AppException(ErrorCode.UNCATEGORIZED);
            lecturer = lecturerRepository.getLecturersByLecturerId(lecturerId)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        } else if (usr.getRole().equalsIgnoreCase(RoleEnum.TEACHER.name())) {
            lecturer = lecturerRepository.getByUser_UserId(userId);
            if (lecturer == null)
                throw new AppException(ErrorCode.USER_NOT_FOUND);

            if (lecturerId != null && !lecturerId.equals(lecturer.getLecturerId()))
                throw new AppException(ErrorCode.UNCATEGORIZED);

        } else {
            throw new AppException(ErrorCode.UNCATEGORIZED);
        }

        return buildTeacherInfo(lecturer, isAdmin);
    }

    private TeacherInfo buildTeacherInfo(Lecturer lecturer, boolean isAdmin) {
        TeacherInfo dto = new TeacherInfo();

        dto.setLecturerId(lecturer.getLecturerId());
        dto.setFullName(lecturer.getFullName());
        dto.setDateOfBirth(lecturer.getDateOfBirth());
        dto.setImagePath(lecturer.getImagePath());

        User u = lecturer.getUser();
        if (u != null) {
            dto.setEmail(u.getEmail());
            dto.setPhoneNumber(u.getPhoneNumber());

            TeacherInfo.AccountInfo acc = new TeacherInfo.AccountInfo();
            acc.setUserId(u.getUserId());
            acc.setUsername(u.getEmail());
            acc.setRole(u.getRole());
            acc.setCreatedAt(u.getCreatedAt());
            acc.setIsVerified(u.getIsVerified());
            if (isAdmin)
                acc.setPassword(u.getPasswordHash());
            dto.setAccountInfo(acc);
        }

        List<CourseClass> teacherClasses = classRepository.findByLecturer_LecturerIdAndStatusNot(
                lecturer.getLecturerId(), ClassStatusEnum.Closed.name());
        dto.setTotalClasses(teacherClasses.size());

        int totalStudents = teacherClasses.stream()
                .mapToInt(cls -> Optional.ofNullable(
                        invoiceDetailRepository.countByClassIdAndActiveInvoice(cls.getClassId()))
                        .orElse(0))
                .sum();
        dto.setTotalStudents(totalStudents);

        Double avgRating = courseReviewRepository.getAverageTeacherRatingByLecturerId(
                lecturer.getLecturerId());
        dto.setRating(avgRating != null ? avgRating : 0.0);

        int reviewCount = classRepository
                .findByLecturer_LecturerId(lecturer.getLecturerId())
                .stream().mapToInt(cls -> courseReviewRepository.countByClassId(cls.getClassId()))
                .sum();
        dto.setTotalReviews(reviewCount);

        List<LecturerDegree> degrees = lecturerDegreeRepository.findByLecturer_LecturerId(lecturer.getLecturerId());

        dto.setQualifications(
                degrees.stream().map(ld -> {
                    TeacherInfo.QualificationDTO q = new TeacherInfo.QualificationDTO();
                    if (ld.getDegree() != null) {
                        q.setDegreeId(ld.getDegree().getId());
                        q.setDegreeName(ld.getDegree().getName());
                    }
                    q.setLevel(ld.getLevel());
                    return q;
                }).toList());

        return dto;
    }

    // PAGINATION CRUD

    public Page<LecturerResponse> getAllLecturers(int page, int size, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Lecturer> lecturers = lecturerRepository.findAll(pageable);

        return lecturers.map(this::convertToResponse);
    }

    public LecturerResponse getLecturerByIdForCRUD(Integer lecturerId) {
        Lecturer lecturer = lecturerRepository.findById(lecturerId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return convertToResponse(lecturer);
    }

    // CREATE LECTURER (Tạo User + Lecturer + Bằng cấp)

    @Transactional
    public LecturerResponse createLecturer(LecturerRequest request) {

        if (userRepository.findByPhoneNumberOrEmail(null, request.getEmail()).isPresent())
            throw new AppException(ErrorCode.USER_EXIST);

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(RoleEnum.TEACHER.name());
        user.setIsVerified(true);
        User savedUser = userRepository.save(user);

        Lecturer lecturer = new Lecturer();
        lecturer.setFullName(request.getFullName());
        lecturer.setDateOfBirth(request.getDateOfBirth());
        lecturer.setImagePath(request.getImagePath());
        lecturer.setUser(savedUser);
        Lecturer savedLecturer = lecturerRepository.save(lecturer);

        // certificates
        if (request.getCertificates() != null) {
            for (LecturerRequest.CertificateRequest c : request.getCertificates()) {
                Degree degree = degreeRepository.findById(c.getDegreeTypeId())
                        .orElseThrow(() -> new AppException(ErrorCode.UNCATEGORIZED));

                LecturerDegree ld = new LecturerDegree();
                ld.setLecturer(savedLecturer);
                ld.setDegree(degree);
                ld.setLevel(c.getLevel());
                lecturerDegreeRepository.save(ld);
            }
        }

        return convertToResponse(savedLecturer);
    }

    // UPDATE LECTURER (CRUD version)

    @Transactional
    public LecturerResponse updateLecturer(Integer lecturerId, LecturerUpdateRequest request) {

        Lecturer lecturer = lecturerRepository.findById(lecturerId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (request.getFullName() != null)
            lecturer.setFullName(request.getFullName());
        if (request.getDateOfBirth() != null)
            lecturer.setDateOfBirth(request.getDateOfBirth());
        if (request.getImagePath() != null)
            lecturer.setImagePath(request.getImagePath());

        User user = lecturer.getUser();
        if (user != null) {

            if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
                if (userRepository.findByPhoneNumberOrEmail(null, request.getEmail()).isPresent()) {
                    throw new AppException(ErrorCode.USER_EXIST);
                }
                user.setEmail(request.getEmail());
            }

            if (request.getPhoneNumber() != null)
                user.setPhoneNumber(request.getPhoneNumber());

            if (request.getPassword() != null && !request.getPassword().isBlank())
                user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

            userRepository.save(user);
        }

        // Update certificates - REPLACE ALL strategy
        // - null: Không thay đổi gì
        // - []: Xóa tất cả bằng cấp
        // - [{...}]: Xóa tất cả cũ, thêm mới theo list
        if (request.getCertificates() != null) {
            // Xóa tất cả bằng cấp hiện có
            List<LecturerDegree> olds = lecturerDegreeRepository.findByLecturer_LecturerId(lecturerId);
            lecturerDegreeRepository.deleteAll(olds);

            // Thêm lại bằng cấp từ request
            for (LecturerUpdateRequest.CertificateRequest c : request.getCertificates()) {
                if (c.getDegreeTypeId() != null) {
                    Degree degree = degreeRepository.findById(c.getDegreeTypeId())
                            .orElseThrow(() -> new AppException(ErrorCode.UNCATEGORIZED));

                    LecturerDegree ld = new LecturerDegree();
                    ld.setLecturer(lecturer);
                    ld.setDegree(degree);
                    ld.setLevel(c.getLevel());
                    lecturerDegreeRepository.save(ld);
                }
            }
        }

        Lecturer updated = lecturerRepository.save(lecturer);
        return convertToResponse(updated);
    }

    // DELETE LECTURER
    @Transactional
    public void deleteLecturer(Integer lecturerId) {

        Lecturer lecturer = lecturerRepository.findById(lecturerId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        List<CourseClass> activeClasses = classRepository.findByLecturer_LecturerIdAndStatusNot(
                lecturerId, ClassStatusEnum.Closed.name());

        if (!activeClasses.isEmpty())
            throw new AppException(ErrorCode.UNCATEGORIZED);

        lecturerRepository.delete(lecturer);
    }

    // Convert Entity -> DTO

    private LecturerResponse convertToResponse(Lecturer lecturer) {

        List<CourseClass> allClasses = classRepository.findByLecturer_LecturerId(lecturer.getLecturerId());

        List<CourseClass> activeClasses = classRepository.findByLecturer_LecturerIdAndStatusNot(
                lecturer.getLecturerId(), ClassStatusEnum.Closed.name());

        List<LecturerDegree> degrees = lecturerDegreeRepository.findByLecturer_LecturerId(lecturer.getLecturerId());

        List<LecturerResponse.CertificateInfo> certs = degrees.stream()
                .filter(ld -> ld.getDegree() != null)
                .map(ld -> LecturerResponse.CertificateInfo.builder()
                        .certificateId(ld.getMa())                 // ID bằng cấp cụ thể (bangcap)
                        .degreeTypeId(ld.getDegree().getId())      // ID loại bằng cấp (loaibangcap)
                        .degreeTypeName(ld.getDegree().getName())  // Tên: IELTS, TOEIC...
                        .level(ld.getLevel())                      // Trình độ: Band 8.0...
                        .build())
                .toList();

        User user = lecturer.getUser();

        return LecturerResponse.builder()
                .lecturerId(lecturer.getLecturerId())
                .fullName(lecturer.getFullName())
                .dateOfBirth(lecturer.getDateOfBirth())
                .imagePath(lecturer.getImagePath())
                .userId(user != null ? user.getUserId() : null)
                .username(user != null ? user.getEmail() : null)
                .email(user != null ? user.getEmail() : null)
                .phoneNumber(user != null ? user.getPhoneNumber() : null)
                .totalClasses(allClasses.size())
                .activeClasses(activeClasses.size())
                .certificates(certs)
                .build();
    }

    /**
     * Lấy thống kê dashboard cho giảng viên
     */
    public LecturerDashboardStatsResponse getDashboardStats(Integer lecturerId) {
        // 1. Đếm số lớp đang hoạt động (InProgress)
        long totalClasses = classRepository.countByLecturer_LecturerIdAndStatus(
            lecturerId, 
            ClassStatusEnum.InProgress.name()
        );

        // 2. Đếm tổng số học viên trong các lớp đang hoạt động
        long totalStudents = invoiceDetailRepository.countDistinctStudentsByLecturerAndClassStatus(
            lecturerId, 
            ClassStatusEnum.InProgress.name()
        );

        // 3. Đếm số buổi học sắp tới (từ hôm nay)
        long upcomingSessions = sessionRepository.countUpcomingSessionsByLecturer(
            lecturerId, 
            LocalDate.now()
        );

        // 4. Tính tỷ lệ điểm danh (attendance rate)
        long totalAttendances = attendanceRepository.countTotalAttendancesByLecturer(lecturerId);
        long presentAttendances = attendanceRepository.countPresentAttendancesByLecturer(lecturerId);
        
        double attendanceRate = 0.0;
        if (totalAttendances > 0) {
            attendanceRate = (double) presentAttendances / totalAttendances * 100;
            // Làm tròn đến 1 chữ số thập phân
            attendanceRate = Math.round(attendanceRate * 10.0) / 10.0;
        }

        return new LecturerDashboardStatsResponse(
            totalClasses,
            totalStudents,
            upcomingSessions,
            attendanceRate
        );
    }
}
