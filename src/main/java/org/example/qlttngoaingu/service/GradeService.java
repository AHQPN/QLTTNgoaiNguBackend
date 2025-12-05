package org.example.qlttngoaingu.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.qlttngoaingu.dto.request.GradeRequest;
import org.example.qlttngoaingu.dto.response.ClassGradesResponse;
import org.example.qlttngoaingu.dto.response.GradeResponse;
import org.example.qlttngoaingu.entity.*;
import org.example.qlttngoaingu.exception.AppException;
import org.example.qlttngoaingu.exception.ErrorCode;
import org.example.qlttngoaingu.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GradeService {

    private final GradeSheetRepository gradeSheetRepository;
    private final InvoiceDetailRepository invoiceDetailRepository;
    private final StudentRepository studentRepository;
    private final CourseClassRepository courseClassRepository;
    private final UserRepository userRepository;
    private final LecturerRepository lecturerRepository;

    // Các loại điểm hỗ trợ
    private static final String GRADE_TYPE_ATTENDANCE = "Chuyên cần";
    private static final String GRADE_TYPE_MIDTERM = "Giữa kỳ";
    private static final String GRADE_TYPE_FINAL = "Cuối kỳ";

    /**
     * STU-01: Lấy tất cả điểm của học viên đang đăng nhập
     */
    public List<GradeResponse> getStudentGrades(Integer userId) {
        // Lấy student từ userId
        Student student = studentRepository.findByAccount_UserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Lấy tất cả lớp học mà học viên đã đăng ký
        List<CourseClass> enrolledClasses = invoiceDetailRepository.findAllByHocVienId(student.getId());

        List<GradeResponse> responses = new ArrayList<>();

        for (CourseClass cls : enrolledClasses) {
            // Lấy enrollment của học viên trong lớp này
            InvoiceDetail enrollment = invoiceDetailRepository
                    .findByClassIdAndStudentId(cls.getClassId(), student.getId())
                    .orElse(null);

            if (enrollment == null) continue;

            // Lấy điểm của học viên trong lớp này
            List<GradeSheet> grades = gradeSheetRepository.findByEnrollment_DetailId(enrollment.getDetailId());

            // Build response
            GradeResponse response = buildGradeResponse(cls, grades);
            responses.add(response);
        }

        return responses;
    }

    /**
     * STU-02: Lấy điểm của học viên theo lớp cụ thể
     */
    public GradeResponse getStudentGradesByClass(Integer userId, Integer classId) {
        // Lấy student từ userId
        Student student = studentRepository.findByAccount_UserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Lấy lớp học
        CourseClass courseClass = courseClassRepository.findById(classId)
                .orElseThrow(() -> new AppException(ErrorCode.CLASS_NOT_FOUND));

        // Lấy enrollment
        InvoiceDetail enrollment = invoiceDetailRepository
                .findByClassIdAndStudentId(classId, student.getId())
                .orElseThrow(() -> new AppException(ErrorCode.ENROLLMENT_NOT_FOUND));

        // Lấy điểm
        List<GradeSheet> grades = gradeSheetRepository.findByEnrollment_DetailId(enrollment.getDetailId());

        return buildGradeResponse(courseClass, grades);
    }

    /**
     * TEA-01: Lấy danh sách điểm của tất cả học viên trong lớp (cho giảng viên)
     */
    public ClassGradesResponse getClassGrades(Integer userId, Integer classId) {
        // Verify giảng viên có quyền xem lớp này
        Lecturer lecturer = lecturerRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.LECTURER_NOT_FOUND));

        CourseClass courseClass = courseClassRepository.findById(classId)
                .orElseThrow(() -> new AppException(ErrorCode.CLASS_NOT_FOUND));

        // Kiểm tra giảng viên có dạy lớp này không
        if (!courseClass.getLecturer().getLecturerId().equals(lecturer.getLecturerId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // Lấy danh sách học viên trong lớp
        List<Student> students = invoiceDetailRepository.findStudentsByClassId(classId);

        // Lấy tất cả điểm của lớp
        List<GradeSheet> allGrades = gradeSheetRepository.findAllByClassId(classId);

        // Group grades theo enrollment
        Map<Integer, List<GradeSheet>> gradesByEnrollment = allGrades.stream()
                .collect(Collectors.groupingBy(g -> g.getEnrollment().getDetailId()));

        // Build response cho từng học viên
        List<ClassGradesResponse.StudentGradeInfo> studentInfos = new ArrayList<>();

        for (Student student : students) {
            InvoiceDetail enrollment = invoiceDetailRepository
                    .findByClassIdAndStudentId(classId, student.getId())
                    .orElse(null);

            if (enrollment == null) continue;

            List<GradeSheet> studentGrades = gradesByEnrollment.getOrDefault(enrollment.getDetailId(), List.of());

            ClassGradesResponse.StudentGradeInfo info = buildStudentGradeInfo(student, enrollment, studentGrades);
            studentInfos.add(info);
        }

        return ClassGradesResponse.builder()
                .classId(classId)
                .className(courseClass.getClassName())
                .courseId(courseClass.getCourse().getCourseId())
                .courseName(courseClass.getCourse().getCourseName())
                .lecturerId(lecturer.getLecturerId())
                .lecturerName(lecturer.getFullName())
                .students(studentInfos)
                .build();
    }

    /**
     * TEA-02: Nhập điểm cho học viên
     */
    @Transactional
    public GradeSheet submitGrade(Integer userId, GradeRequest request) {
        // Verify người dùng là giảng viên hoặc admin
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Lấy enrollment
        InvoiceDetail enrollment = invoiceDetailRepository.findById(request.getEnrollmentId())
                .orElseThrow(() -> new AppException(ErrorCode.ENROLLMENT_NOT_FOUND));

        // Chuyển đổi gradeTypeId thành tên loại điểm
        String gradeTypeName = convertGradeTypeIdToName(request.getGradeTypeId());

        // Kiểm tra xem đã có điểm cho loại này chưa
        Optional<GradeSheet> existingGrade = gradeSheetRepository
                .findByEnrollmentIdAndGradeType(request.getEnrollmentId(), gradeTypeName);

        GradeSheet gradeSheet;
        if (existingGrade.isPresent()) {
            // Cập nhật điểm cũ
            gradeSheet = existingGrade.get();
            gradeSheet.setScore(request.getScore());
            gradeSheet.setComment(request.getComment());
            gradeSheet.setGradedAt(LocalDateTime.now());
        } else {
            // Tạo điểm mới
            gradeSheet = new GradeSheet();
            gradeSheet.setEnrollment(enrollment);
            gradeSheet.setGradeType(gradeTypeName);
            gradeSheet.setScore(request.getScore());
            gradeSheet.setComment(request.getComment());
            gradeSheet.setGradedAt(LocalDateTime.now());
        }

        return gradeSheetRepository.save(gradeSheet);
    }

    /**
     * TEA-03: Cập nhật điểm
     */
    @Transactional
    public GradeSheet updateGrade(Integer userId, Integer gradeId, GradeRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        GradeSheet gradeSheet = gradeSheetRepository.findById(gradeId)
                .orElseThrow(() -> new AppException(ErrorCode.GRADE_NOT_FOUND));

        gradeSheet.setScore(request.getScore());
        gradeSheet.setComment(request.getComment());
        gradeSheet.setGradedAt(LocalDateTime.now());

        return gradeSheetRepository.save(gradeSheet);
    }

    // ==================== HELPER METHODS ====================

    /**
     * Chuyển đổi grade type ID từ frontend sang tên loại điểm trong database
     */
    private String convertGradeTypeIdToName(Integer gradeTypeId) {
        return switch (gradeTypeId) {
            case 1 -> GRADE_TYPE_ATTENDANCE;
            case 2 -> GRADE_TYPE_MIDTERM;
            case 3 -> GRADE_TYPE_FINAL;
            default -> throw new AppException(ErrorCode.GRADE_TYPE_NOT_FOUND);
        };
    }

    /**
     * Xác định loại điểm dựa trên tên
     */
    private Integer getGradeTypeId(String gradeTypeName) {
        if (gradeTypeName == null) return null;
        if (gradeTypeName.contains("Chuyên cần") || gradeTypeName.equalsIgnoreCase("Attendance")) return 1;
        if (gradeTypeName.contains("Giữa kỳ") || gradeTypeName.equalsIgnoreCase("Midterm")) return 2;
        if (gradeTypeName.contains("Cuối kỳ") || gradeTypeName.equalsIgnoreCase("Final")) return 3;
        return null;
    }

    private GradeResponse buildGradeResponse(CourseClass cls, List<GradeSheet> grades) {
        BigDecimal attendance = null, midterm = null, finalScore = null;
        String comment = null;
        LocalDateTime lastGradedAt = null;

        for (GradeSheet grade : grades) {
            Integer typeId = getGradeTypeId(grade.getGradeType());
            if (typeId == null) continue;

            switch (typeId) {
                case 1 -> attendance = grade.getScore();
                case 2 -> midterm = grade.getScore();
                case 3 -> finalScore = grade.getScore();
            }

            if (grade.getComment() != null) comment = grade.getComment();
            if (grade.getGradedAt() != null) {
                if (lastGradedAt == null || grade.getGradedAt().isAfter(lastGradedAt)) {
                    lastGradedAt = grade.getGradedAt();
                }
            }
        }

        BigDecimal totalScore = GradeResponse.calculateTotalScore(attendance, midterm, finalScore);

        return GradeResponse.builder()
                .classId(cls.getClassId())
                .className(cls.getClassName())
                .courseId(cls.getCourse().getCourseId())
                .courseName(cls.getCourse().getCourseName())
                .courseImage(cls.getCourse().getImage())
                .attendanceScore(attendance)
                .midtermScore(midterm)
                .finalScore(finalScore)
                .totalScore(totalScore)
                .grade(GradeResponse.calculateGrade(totalScore))
                .status(GradeResponse.calculateStatus(attendance, midterm, finalScore))
                .comment(comment)
                .lastGradedAt(lastGradedAt)
                .gradedByName(null)
                .build();
    }

    private ClassGradesResponse.StudentGradeInfo buildStudentGradeInfo(
            Student student, InvoiceDetail enrollment, List<GradeSheet> grades) {

        BigDecimal attendance = null, midterm = null, finalScore = null;
        ClassGradesResponse.GradeDetail attendanceGrade = null;
        ClassGradesResponse.GradeDetail midtermGrade = null;
        ClassGradesResponse.GradeDetail finalGrade = null;

        for (GradeSheet grade : grades) {
            Integer typeId = getGradeTypeId(grade.getGradeType());
            if (typeId == null) continue;

            ClassGradesResponse.GradeDetail detail = ClassGradesResponse.GradeDetail.builder()
                    .gradeId(grade.getGradeSheetId())
                    .score(grade.getScore())
                    .comment(grade.getComment())
                    .gradedAt(grade.getGradedAt())
                    .gradedByName(null)
                    .build();

            switch (typeId) {
                case 1 -> { attendance = grade.getScore(); attendanceGrade = detail; }
                case 2 -> { midterm = grade.getScore(); midtermGrade = detail; }
                case 3 -> { finalScore = grade.getScore(); finalGrade = detail; }
            }
        }

        BigDecimal totalScore = GradeResponse.calculateTotalScore(attendance, midterm, finalScore);

        return ClassGradesResponse.StudentGradeInfo.builder()
                .studentId(student.getId())
                .studentName(student.getName())
                .email(student.getAccount() != null ? student.getAccount().getEmail() : null)
                .avatar(student.getAvatar())
                .enrollmentId(enrollment.getDetailId())
                .attendanceScore(attendance)
                .midtermScore(midterm)
                .finalScore(finalScore)
                .totalScore(totalScore)
                .grade(GradeResponse.calculateGrade(totalScore))
                .status(GradeResponse.calculateStatus(attendance, midterm, finalScore))
                .attendanceGrade(attendanceGrade)
                .midtermGrade(midtermGrade)
                .finalGrade(finalGrade)
                .build();
    }
}
