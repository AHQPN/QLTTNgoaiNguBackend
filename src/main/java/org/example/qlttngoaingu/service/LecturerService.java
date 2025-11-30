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
import org.example.qlttngoaingu.dto.response.AvailableLecturerResponse;
import org.example.qlttngoaingu.dto.response.TeacherInfo;
import org.example.qlttngoaingu.entity.*;
import org.example.qlttngoaingu.exception.AppException;
import org.example.qlttngoaingu.exception.ErrorCode;
import org.example.qlttngoaingu.repository.CourseClassRepository;
import org.example.qlttngoaingu.repository.LecturerDegreeRepository;
import org.example.qlttngoaingu.repository.LecturerRepository;
import org.example.qlttngoaingu.repository.UserRepository;
import org.example.qlttngoaingu.service.enums.ClassStatusEnum;
import org.example.qlttngoaingu.service.enums.RoleEnum;
import org.example.qlttngoaingu.service.enums.SchedulePattern;
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

        // 2. Nếu là admin → phải truyền lecturerId để xem thông tin bất kỳ giảng viên nào
        if (usr.getRole().equalsIgnoreCase("ADMIN")) {

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

        if (lecturer.getUser() != null) {
            dto.setEmail(lecturer.getUser().getEmail());
            dto.setPhoneNumber(lecturer.getUser().getPhoneNumber());
        }

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



}
