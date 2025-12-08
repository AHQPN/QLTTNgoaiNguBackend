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
import java.util.Set;

import org.example.qlttngoaingu.dto.request.LecturerCreationRequest;
import org.example.qlttngoaingu.dto.request.LecturerRequest;
import org.example.qlttngoaingu.dto.request.LecturerUpdateRequest;
import org.example.qlttngoaingu.dto.response.AvailableLecturerResponse;
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
import org.example.qlttngoaingu.repository.CourseClassRepository;
import org.example.qlttngoaingu.repository.CourseReviewRepository;
import org.example.qlttngoaingu.repository.DegreeRepository;
import org.example.qlttngoaingu.repository.InvoiceDetailRepository;
import org.example.qlttngoaingu.repository.LecturerDegreeRepository;
import org.example.qlttngoaingu.repository.LecturerRepository;
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

    @Transactional
    public void addLecturerInfo(LecturerCreationRequest request,Integer userId) {
        Lecturer lecturer = new Lecturer();
        lecturer.setFullName(request.getName());
        lecturer.setDateOfBirth(request.getDateOfBirth());
        lecturer.setImagePath(request.getImageUrl());
        lecturer.setUser(userRepository.findById(userId).get());
        // 2. Lưu vào database
        lecturerRepository.save(lecturer);
    }


    public List<AvailableLecturerResponse> getAvailableLecturers(
            String schedulePattern,
            LocalTime startTime,
            Integer durationMinutes,
            LocalDate startDate
    ) {
        // Lấy tất cả giảng viên
        List<Lecturer> lecturers = lecturerRepository.findAll();

        // Parse pattern
        SchedulePattern pattern;
        try {
            pattern = SchedulePattern.fromPattern(schedulePattern);
        } catch (Exception e) {
            return Collections.emptyList();
        }

        LocalTime endTime = startTime.plusMinutes(durationMinutes);

        List<AvailableLecturerResponse> result = new ArrayList<>();

        for (Lecturer lecturer : lecturers) {
            boolean isAvailable = checkLecturerAvailability(
                    lecturer.getLecturerId(),
                    pattern,
                    startTime,
                    endTime,
                    startDate
            );

            if (isAvailable) {
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
            LocalDate startDate
    ) {
        // Tìm các lớp giảng viên đang dạy và còn hoạt động
        List<CourseClass> classes = classRepository.findByLecturer_LecturerIdAndStatus(lecturerId, ClassStatusEnum.InProgress.name());

        for (CourseClass courseClass : classes) {

            LocalDate classEndDate = calculateClassEndDate(courseClass);

            if (classEndDate.isBefore(startDate)) continue;

            // Pattern của lớp cũ
            SchedulePattern classPattern = SchedulePattern.fromPattern(courseClass.getSchedule());

            Set<DayOfWeek> commonDays = new HashSet<>(pattern.getDaysOfWeek());
            commonDays.retainAll(classPattern.getDaysOfWeek());

            if (commonDays.isEmpty()) continue; // không trùng ngày

            if (courseClass.getStartTime() != null) {
                LocalTime classEndTime = courseClass.getStartTime()
                        .plusMinutes(courseClass.getMinutesPerSession());

                boolean overlap = !(endTime.isBefore(courseClass.getStartTime()) ||
                        startTime.isAfter(classEndTime));

                if (overlap) return false;
            }
        }

        return true;
    }

    private LocalDate calculateClassEndDate(CourseClass cls) {
        Course course = cls.getCourse();
        SchedulePattern pattern = SchedulePattern.fromPattern(cls.getSchedule());

        // Tổng phút học
        BigDecimal totalMinutes = BigDecimal.valueOf(course.getStudyHours())
                .multiply(BigDecimal.valueOf(60));

        // Số buổi cần học
        int totalSessions = totalMinutes
                .divide(BigDecimal.valueOf(cls.getMinutesPerSession()), 0, RoundingMode.CEILING)
                .intValue();

        LocalDate date = cls.getStartDate();
        int created = 0;

        // Lặp qua từng ngày, tạo buổi theo pattern
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



    @Transactional(readOnly = true)
    public TeacherInfo getLecturerById(Integer userId, Integer lecturerId) {

        // 1. Lấy user hiện tại
        User usr = userRepository.getUserByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Lecturer lecturer;
        boolean isAdmin = usr.getRole().equalsIgnoreCase("ADMIN");

        // 2. Nếu là admin → phải truyền lecturerId để xem thông tin bất kỳ giảng viên nào
        if (isAdmin) {

            if (lecturerId == null) {
                throw new AppException(ErrorCode.UNCATEGORIZED);
            }

            lecturer = lecturerRepository.getLecturersByLecturerId(lecturerId)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        }

        // 3. Nếu là giảng viên tự xem hồ sơ → lấy theo userId hiện tại
        else if (usr.getRole().equalsIgnoreCase(RoleEnum.TEACHER.name())) {

            lecturer = lecturerRepository.getByUser_UserId(userId);
            if (lecturer == null) {
                throw new AppException(ErrorCode.USER_NOT_FOUND);
            }

            if (lecturerId != null && !lecturerId.equals(lecturer.getLecturerId())) {
                throw new AppException(ErrorCode.UNCATEGORIZED);
            }
        }

        // 4. Các role khác → không có quyền
        else {
            throw new AppException(ErrorCode.UNCATEGORIZED);
        }

        // ==============================
        // Build DTO
        // ==============================
        TeacherInfo dto = new TeacherInfo();
        dto.setLecturerId(lecturer.getLecturerId());
        dto.setFullName(lecturer.getFullName());
        dto.setDateOfBirth(lecturer.getDateOfBirth());
        dto.setImagePath(lecturer.getImagePath());

        User lecturerUser = lecturer.getUser();
        if (lecturerUser != null) {
            dto.setEmail(lecturerUser.getEmail());
            dto.setPhoneNumber(lecturerUser.getPhoneNumber());

            // Thông tin tài khoản - chỉ Admin mới xem được mật khẩu
            TeacherInfo.AccountInfo accountInfo = new TeacherInfo.AccountInfo();
            accountInfo.setUserId(lecturerUser.getUserId());
            accountInfo.setUsername(lecturerUser.getEmail()); // hoặc phone
            accountInfo.setRole(lecturerUser.getRole());
            accountInfo.setCreatedAt(lecturerUser.getCreatedAt());
            accountInfo.setIsVerified(lecturerUser.getIsVerified());

            // Chỉ Admin mới xem được mật khẩu
            if (isAdmin) {
                accountInfo.setPassword(lecturerUser.getPasswordHash());
            }

            dto.setAccountInfo(accountInfo);
        }

        // Thống kê số lớp và số học viên
        List<CourseClass> teacherClasses = classRepository.findByLecturer_LecturerIdAndStatusNot(
                lecturer.getLecturerId(), ClassStatusEnum.Closed.name());
        dto.setTotalClasses(teacherClasses.size());

        // Tính tổng số học viên từ tất cả các lớp
        int totalStudents = 0;
        for (CourseClass cls : teacherClasses) {
            Integer count = invoiceDetailRepository.countByClassIdAndActiveInvoice(cls.getClassId());
            totalStudents += (count != null ? count : 0);
        }
        dto.setTotalStudents(totalStudents);

        // Tính rating từ bảng đánh giá
        Double avgRating = courseReviewRepository.getAverageTeacherRatingByLecturerId(lecturer.getLecturerId());
        dto.setRating(avgRating != null ? avgRating : 0.0);

        // Đếm số lượng đánh giá
        int reviewCount = 0;
        List<CourseClass> allClasses = classRepository.findByLecturer_LecturerId(lecturer.getLecturerId());
        for (CourseClass cls : allClasses) {
            reviewCount += courseReviewRepository.countByClassId(cls.getClassId());
        }
        dto.setTotalReviews(reviewCount);

        List<LecturerDegree> list = lecturerDegreeRepository.findByLecturer_LecturerId(lecturer.getLecturerId());
        var qualList = list.stream().map(ld -> {
            TeacherInfo.QualificationDTO q = new TeacherInfo.QualificationDTO();

            if (ld.getDegree() != null) {
                q.setDegreeId(ld.getDegree().getId());
                q.setDegreeName(ld.getDegree().getName());
            }

            q.setLevel(ld.getLevel());
            return q;
        }).toList();

        dto.setQualifications(qualList);

        return dto;
    }

    // ==================== CRUD với Phân trang ====================

    /**
     * Lấy danh sách giảng viên có phân trang
     */
    public Page<LecturerResponse> getAllLecturers(int page, int size, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("desc") 
            ? Sort.by(sortBy).descending() 
            : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Lecturer> lecturersPage = lecturerRepository.findAll(pageable);
        
        return lecturersPage.map(this::convertToResponse);
    }

    /**
     * Lấy thông tin chi tiết giảng viên theo ID
     */
    public LecturerResponse getLecturerByIdForCRUD(Integer lecturerId) {
        Lecturer lecturer = lecturerRepository.findById(lecturerId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        
        return convertToResponse(lecturer);
    }

    /**
     * Tạo giảng viên mới (bao gồm user, thông tin cá nhân và bằng cấp)
     */
    @Transactional
    public LecturerResponse createLecturer(LecturerRequest request) {
        // 1. Kiểm tra email đã tồn tại chưa
        if (userRepository.findByPhoneNumberOrEmail(null, request.getEmail()).isPresent()) {
            throw new AppException(ErrorCode.USER_EXIST);
        }
        
        // 2. Tạo User mới
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(RoleEnum.TEACHER.name());
        user.setIsVerified(true); // Auto verify cho giảng viên
        User savedUser = userRepository.save(user);
        
        // 3. Tạo Lecturer
        Lecturer lecturer = new Lecturer();
        lecturer.setFullName(request.getFullName());
        lecturer.setDateOfBirth(request.getDateOfBirth());
        lecturer.setImagePath(request.getImagePath());
        lecturer.setUser(savedUser);
        Lecturer savedLecturer = lecturerRepository.save(lecturer);
        
        // 4. Thêm bằng cấp
        if (request.getCertificates() != null && !request.getCertificates().isEmpty()) {
            for (LecturerRequest.CertificateRequest certReq : request.getCertificates()) {
                Degree degree = degreeRepository.findById(certReq.getDegreeId())
                        .orElseThrow(() -> new AppException(ErrorCode.UNCATEGORIZED));
                
                LecturerDegree lecturerDegree = new LecturerDegree();
                lecturerDegree.setLecturer(savedLecturer);
                lecturerDegree.setDegree(degree);
                lecturerDegree.setLevel(certReq.getLevel());
                lecturerDegreeRepository.save(lecturerDegree);
            }
        }
        
        return convertToResponse(savedLecturer);
    }

    /**
     * Cập nhật thông tin giảng viên (bao gồm user info và bằng cấp)
     */
    @Transactional
    public LecturerResponse updateLecturer(Integer lecturerId, LecturerUpdateRequest request) {
        Lecturer lecturer = lecturerRepository.findById(lecturerId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        
        // 1. Cập nhật thông tin giảng viên
        if (request.getFullName() != null) {
            lecturer.setFullName(request.getFullName());
        }
        if (request.getDateOfBirth() != null) {
            lecturer.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getImagePath() != null) {
            lecturer.setImagePath(request.getImagePath());
        }
        
        // 2. Cập nhật thông tin User
        User user = lecturer.getUser();
        if (user != null) {
            if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
                // Kiểm tra email mới đã tồn tại chưa
                if (userRepository.findByPhoneNumberOrEmail(null, request.getEmail()).isPresent()) {
                    throw new AppException(ErrorCode.USER_EXIST);
                }
                user.setEmail(request.getEmail());
            }
            if (request.getPhoneNumber() != null) {
                user.setPhoneNumber(request.getPhoneNumber());
            }
            if (request.getPassword() != null && !request.getPassword().isBlank()) {
                user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            }
            userRepository.save(user);
        }
        
        // 3. Cập nhật bằng cấp (nếu có)
        if (request.getCertificates() != null) {
            // Xóa tất cả bằng cấp cũ
            List<LecturerDegree> oldDegrees = lecturerDegreeRepository.findByLecturer_LecturerId(lecturerId);
            lecturerDegreeRepository.deleteAll(oldDegrees);
            
            // Thêm bằng cấp mới
            for (LecturerUpdateRequest.CertificateRequest certReq : request.getCertificates()) {
                if (certReq.getDegreeId() != null) {
                    Degree degree = degreeRepository.findById(certReq.getDegreeId())
                            .orElseThrow(() -> new AppException(ErrorCode.UNCATEGORIZED));
                    
                    LecturerDegree lecturerDegree = new LecturerDegree();
                    lecturerDegree.setLecturer(lecturer);
                    lecturerDegree.setDegree(degree);
                    lecturerDegree.setLevel(certReq.getLevel());
                    lecturerDegreeRepository.save(lecturerDegree);
                }
            }
        }
        
        Lecturer updatedLecturer = lecturerRepository.save(lecturer);
        return convertToResponse(updatedLecturer);
    }

    /**
     * Xóa giảng viên (soft delete hoặc hard delete tùy yêu cầu)
     */
    @Transactional
    public void deleteLecturer(Integer lecturerId) {
        Lecturer lecturer = lecturerRepository.findById(lecturerId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        
        // Kiểm tra giảng viên có lớp đang hoạt động không
        List<CourseClass> activeClasses = classRepository.findByLecturer_LecturerIdAndStatusNot(
                lecturerId, ClassStatusEnum.Closed.name());
        
        if (!activeClasses.isEmpty()) {
            throw new AppException(ErrorCode.UNCATEGORIZED); // Không thể xóa giảng viên đang có lớp
        }
        
        lecturerRepository.delete(lecturer);
    }

    /**
     * Chuyển đổi Entity sang Response DTO
     */
    private LecturerResponse convertToResponse(Lecturer lecturer) {
        // Đếm số lớp
        List<CourseClass> allClasses = classRepository.findByLecturer_LecturerId(lecturer.getLecturerId());
        List<CourseClass> activeClasses = classRepository.findByLecturer_LecturerIdAndStatusNot(
                lecturer.getLecturerId(), ClassStatusEnum.Closed.name());
        
        // Lấy chứng chỉ
        List<LecturerDegree> degrees = lecturerDegreeRepository.findByLecturer_LecturerId(lecturer.getLecturerId());
        List<LecturerResponse.CertificateInfo> certificates = degrees.stream()
                .filter(ld -> ld.getDegree() != null)
                .map(ld -> LecturerResponse.CertificateInfo.builder()
                        .certificateId(ld.getDegree().getId())
                        .certificateName(ld.getDegree().getName())
                        .level(ld.getLevel())
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
                .certificates(certificates)
                .build();
    }

}
