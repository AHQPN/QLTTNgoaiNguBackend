package org.example.qlttngoaingu.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.example.qlttngoaingu.dto.request.AttendanceEntryRequest;
import org.example.qlttngoaingu.dto.request.AttendanceSessionRequest;
import org.example.qlttngoaingu.dto.response.AttendanceEntryResponse;
import org.example.qlttngoaingu.dto.response.AttendanceSessionResponse;
import org.example.qlttngoaingu.entity.Attendance;
import org.example.qlttngoaingu.entity.CourseClass;
import org.example.qlttngoaingu.entity.Lecturer;
import org.example.qlttngoaingu.entity.Session;
import org.example.qlttngoaingu.entity.Student;
import org.example.qlttngoaingu.entity.User;
import org.example.qlttngoaingu.exception.AppException;
import org.example.qlttngoaingu.exception.ErrorCode;
import org.example.qlttngoaingu.repository.AttendanceRepository;
import org.example.qlttngoaingu.repository.InvoiceDetailRepository;
import org.example.qlttngoaingu.repository.LecturerRepository;
import org.example.qlttngoaingu.repository.SessionRepository;
import org.example.qlttngoaingu.repository.UserRepository;
import org.example.qlttngoaingu.service.enums.RoleEnum;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final SessionRepository sessionRepository;
    // studentRepository is not needed since we lookup students through InvoiceDetail
    private final InvoiceDetailRepository invoiceDetailRepository;
    private final UserRepository userRepository;
    private final LecturerRepository lecturerRepository;

    @Transactional
    public AttendanceSessionResponse markAttendance(Integer lecturerId, AttendanceSessionRequest request) {
        User usr = userRepository.findByUserId(lecturerId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        Lecturer lecturer = lecturerRepository.getByUser_UserId(usr.getUserId());

        Session session = sessionRepository.getSessionBySessionId(request.getSessionId())
                .orElseThrow(() -> new AppException(ErrorCode.CLASS_NOT_FOUND));

        // validate lecturer owns the class
        CourseClass cls = session.getCourseClass();
        if (cls == null || cls.getLecturer() == null || !Objects.equals(cls.getLecturer().getLecturerId(), lecturer.getLecturerId())) {
            throw new AppException(ErrorCode.LECTURER_NOT_OWN_THIS_CLASS);
        }

        List<AttendanceEntryResponse> responses = new ArrayList<>();

        for (AttendanceEntryRequest e : request.getEntries()) {
            // Find InvoiceDetail (enrollment) by class and student
            org.example.qlttngoaingu.entity.InvoiceDetail invoiceDetail = invoiceDetailRepository.findByClassIdAndStudentId(cls.getClassId(), e.getStudentId())
                    .orElseThrow(() -> new RuntimeException("Student " + e.getStudentId() + " is not enrolled in class " + cls.getClassId()));

            Student student = invoiceDetail.getInvoice().getStudent();

            java.util.Optional<Attendance> existingOpt = attendanceRepository.findBySessionAndInvoiceDetail(session, invoiceDetail);
            if (existingOpt.isEmpty()) {
                Attendance newa = new Attendance();
                newa.setSession(session);
                newa.setInvoiceDetail(invoiceDetail);
                newa.setAbsent(Boolean.TRUE.equals(e.getAbsent()));
                newa.setNote(e.getNote());
                attendanceRepository.save(newa);
                responses.add(new AttendanceEntryResponse(student.getId(), student.getName(), newa.getAbsent(), newa.getNote()));
            } else {
                Attendance existing = existingOpt.get();
                existing.setAbsent(Boolean.TRUE.equals(e.getAbsent()));
                existing.setNote(e.getNote());
                attendanceRepository.save(existing);
                responses.add(new AttendanceEntryResponse(student.getId(), student.getName(), existing.getAbsent(), existing.getNote()));
            }
        }

        return new AttendanceSessionResponse(request.getSessionId(), responses);
    }

    public AttendanceSessionResponse getAttendanceForSession(Integer lecturerId, Integer sessionId) {
        User usr = userRepository.findByUserId(lecturerId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));


        Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));
        CourseClass cls = session.getCourseClass();
        if (Objects.equals(usr.getRole(), RoleEnum.TEACHER.name())) {
            Lecturer lecturer = lecturerRepository.getByUser_UserId(usr.getUserId());
            if (cls == null || cls.getLecturer() == null || !Objects.equals(cls.getLecturer().getLecturerId(), lecturer.getLecturerId())) {
                throw new RuntimeException("Lecturer does not own the class for this session");
            }
        }


        List<Student> studentsInClass = invoiceDetailRepository.findStudentsByClassId(cls.getClassId());
        List<AttendanceEntryResponse> responses = new ArrayList<>();
        for (Student s : studentsInClass) {
            org.example.qlttngoaingu.entity.InvoiceDetail invoiceDetail = invoiceDetailRepository.findByClassIdAndStudentId(cls.getClassId(), s.getId()).orElse(null);
            if (invoiceDetail == null) {
                // Should not happen because student is returned by findStudentsByClassId, but guard anyway
                responses.add(new AttendanceEntryResponse(s.getId(), s.getName(), false, null));
                continue;
            }
            java.util.Optional<Attendance> existingOpt = attendanceRepository.findBySessionAndInvoiceDetail(session, invoiceDetail);
            if (existingOpt.isEmpty()) {
                responses.add(new AttendanceEntryResponse(s.getId(), s.getName(), false, null));
            } else {
                Attendance existing = existingOpt.get();
                responses.add(new AttendanceEntryResponse(s.getId(), s.getName(), existing.getAbsent(), existing.getNote()));
            }
        }
        return new AttendanceSessionResponse(sessionId, responses);
    }
}
