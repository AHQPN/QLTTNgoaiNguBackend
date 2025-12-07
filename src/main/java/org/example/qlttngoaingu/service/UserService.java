package org.example.qlttngoaingu.service;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.example.qlttngoaingu.dto.request.LecturerCreationRequest;
import org.example.qlttngoaingu.dto.request.SignupRequest;
import org.example.qlttngoaingu.dto.request.StudentSignupRequest;
import org.example.qlttngoaingu.dto.response.ApiResponse;
import org.example.qlttngoaingu.dto.response.NameAndEmail;
import org.example.qlttngoaingu.dto.response.StudentInfo;
import org.example.qlttngoaingu.entity.Lecturer;
import org.example.qlttngoaingu.entity.Student;
import org.example.qlttngoaingu.entity.User;
import org.example.qlttngoaingu.entity.VerificationCode;
import org.example.qlttngoaingu.exception.AppException;
import org.example.qlttngoaingu.exception.ErrorCode;
import org.example.qlttngoaingu.mapper.StudentMapper;
import org.example.qlttngoaingu.repository.LecturerRepository;
import org.example.qlttngoaingu.repository.StudentRepository;
import org.example.qlttngoaingu.repository.UserRepository;
import org.example.qlttngoaingu.repository.VerificationCodeRepository;
import org.example.qlttngoaingu.service.enums.RoleEnum;
import org.example.qlttngoaingu.service.enums.VerificationCodeEnum;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;

@Service @AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationCodeRepository verificationCodeRepository;
    private final JavaMailSender mailSender;
    private final LecturerRepository lecturerRepository;
    private final StudentRepository studentRepository;
    private final StudentMapper studentMapper;
    LecturerService lecturerService;
    @Transactional
    public User createUser(SignupRequest signupRequest, RoleEnum userType, Boolean sendVerification, String siteURL) {

        String phoneNumber = signupRequest.getPhoneNumber();

        if (userRepository.existsByPhoneNumber(phoneNumber) || userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new AppException(ErrorCode.USER_PHONE_OR_EMAIL_EXIST);
        }

        User user = new User();

        user.setIsVerified(!sendVerification);

        user.setEmail(signupRequest.getEmail());
        user.setPhoneNumber(phoneNumber);
        user.setRole(userType.name());
        user.setCreatedAt(LocalDateTime.now());
        user.setPasswordHash(passwordEncoder.encode(signupRequest.getPassword()));
        userRepository.save(user);
        if (!user.getIsVerified())
        {
            Optional<VerificationCode>  verificationCode = generateNewVerificationCode(user, VerificationCodeEnum.EMAIL_VERIFICATION);
            verificationCode.ifPresent(code -> sendVerificationEmail(user, siteURL, code));
        }

        return user;
    }
    @Transactional
    public StudentInfo signUpForStudent(StudentSignupRequest studentSignupRequest,RoleEnum role,Boolean sendVerification, String siteURL)
    {
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setPhoneNumber(studentSignupRequest.getPhoneNumber());
        signupRequest.setEmail(studentSignupRequest.getEmail());
        signupRequest.setPassword(studentSignupRequest.getPassword());
        User usr = createUser(signupRequest,role,sendVerification,siteURL);
        Student student = new Student();
        student.setAccount(usr);
        student.setName(studentSignupRequest.getName());
        student.setAddress(studentSignupRequest.getAddress());
        student.setGender(studentSignupRequest.getGender());
        student.setNgaySinh(studentSignupRequest.getNgaySinh());
        student.setJob(studentSignupRequest.getJob());

        student = studentRepository.save(student);
        return studentMapper.toStudentInfo(student);
    }

    public Optional<User> getUserByIdentifier(String identifier) {
        return userRepository.findByPhoneNumberOrEmail(identifier,identifier);
    }


    // ====================== TẠO CODE MỚI ======================
    @Transactional
    public Optional<VerificationCode> generateNewVerificationCode(User user, VerificationCodeEnum type) {

        Optional<VerificationCode> existingCode = verificationCodeRepository
                .findByUserAndType(user, type)
                .filter(code -> code.getExpiresAt().isAfter(LocalDateTime.now()));

        if (existingCode.isPresent()) {
            // Đã có mã còn hạn, không tạo mới
            return existingCode;
        }

        // 2️⃣ Xóa mã cũ cùng loại (nếu hết hạn hoặc tồn tại)
        verificationCodeRepository.findByUserAndType(user, type)
                .ifPresent(verificationCodeRepository::delete);

        // 3️⃣ Tạo mã mới
        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setUser(user);
        verificationCode.setType(type);
        verificationCode.setVerificationCode(UUID.randomUUID().toString());
        verificationCode.setCreatedAt(LocalDateTime.now());
        verificationCode.setExpiresAt(LocalDateTime.now().plusMinutes(2));

        verificationCodeRepository.save(verificationCode);
        return Optional.of(verificationCode);
    }

    // ====================== GỬI EMAIL ======================
    public void sendVerificationEmail(User user, String siteURL, VerificationCode verificationCode) {
        String toAddress = user.getEmail();
        String senderName = "Ipower IELTS";
        String fromAddress = "nguyenbro9721@gmail.com";
        VerificationCodeEnum type = verificationCode.getType();
        RoleEnum userRole = RoleEnum.valueOf(user.getRole());
        
        String verifyURL;
        String subject;
        String content;

        switch (type) {
            case EMAIL_VERIFICATION -> {
                verifyURL = siteURL + "/auth/verify?code="
                        + verificationCode.getVerificationCode()
                        + "&type=" + type.name();
                subject = "Please verify your registration";
                content = String.format("""
                        Please click the link below to verify your email:<br>
                        <h3><a href="%s" target="_self">VERIFY EMAIL</a></h3>
                        Thank you,<br>%s.
                        """, verifyURL, senderName);
            }
            case PASSWORD_RESET -> {
                // Redirect đến trang reset password: frontendUrl cho STUDENT, adminUrl cho ADMIN/TEACHER/ACADEMIC_MANAGER
                verifyURL = siteURL + "/reset-password?code=" + verificationCode.getVerificationCode();
                subject = "Password Reset Request";
                content = String.format("""
                        You requested to reset your password.<br>
                        Click below to continue:<br>
                        <h3><a href="%s" target="_self">RESET PASSWORD</a></h3>
                        <p>This link will expire in 2 minutes.</p>
                        <p>If you did not request this, please ignore this email.</p>
                        Thank you,<br>%s.
                        """, verifyURL, senderName);
            }
            case CHANGE_MAIL -> {
                verifyURL = siteURL + "/auth/verify?code="
                        + verificationCode.getVerificationCode()
                        + "&type=" + type.name();
                subject = "Confirm your new email";
                content = String.format("""
                        Please verify your new email by clicking the link below:<br>
                        <h3><a href="%s" target="_self">CONFIRM EMAIL</a></h3>
                        Thank you,<br>%s.
                        """, verifyURL, senderName);
            }
            default -> throw new AppException(ErrorCode.INVALID_CODE);
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);

            helper.setFrom(fromAddress, senderName);
            helper.setTo(toAddress);
            helper.setSubject(subject);
            helper.setText(content, true);
            mailSender.send(message);

        } catch (UnsupportedEncodingException | MessagingException e) {
            throw new AppException(ErrorCode.FAIL_TO_VERIFY_EMAIL);
        }
    }

    public ApiResponse verify(String code, VerificationCodeEnum type) {

        Optional<VerificationCode> verificationCode = verificationCodeRepository.findByVerificationCode(code);
        if(verificationCode.isEmpty()) {
            return ApiResponse.builder().code(ErrorCode.INVALID_CODE.getCode()).message(ErrorCode.INVALID_CODE.getMessage()).build();
        }
        VerificationCode verificationCode1 = verificationCode.get();
        if (verificationCode1.getExpiresAt() != null &&
                verificationCode1.getExpiresAt().isBefore(LocalDateTime.now())) {
            verificationCodeRepository.delete(verificationCode1);
            return ApiResponse.builder().code(ErrorCode.EXPIRED_VERIFICATION_CODE.getCode()).message(ErrorCode.EXPIRED_VERIFICATION_CODE.getMessage()).build();
        }

        if (!verificationCode1.getType().equals(type)) {
            return ApiResponse.builder().code(ErrorCode.INVALID_CODE.getCode()).message(ErrorCode.INVALID_CODE.getMessage()).build();
        }

        User user = verificationCode1.getUser();
        switch (type) {
            case EMAIL_VERIFICATION -> {
                if (!user.getIsVerified()) {
                    user.setIsVerified(true);
                    userRepository.save(user);
                }
                return ApiResponse.builder().code(1000).message("Your email was verified").build();
            }
            case PASSWORD_RESET, CHANGE_MAIL -> {
            }
            default -> {

            return ApiResponse.builder().code(ErrorCode.INVALID_CODE.getCode()).message(ErrorCode.INVALID_CODE.getMessage()).build();
            }
        }
        verificationCodeRepository.delete(verificationCode1);
        return ApiResponse.builder().code(1000).message("Your email was verified").build();

    }

    public void UpdateUserProfile(User user) {

    }

    @Transactional
    public void AddLecturerUser(LecturerCreationRequest lecturerCreationRequest) {
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setPassword("123456");
        signupRequest.setEmail(lecturerCreationRequest.getEmail());
        signupRequest.setPhoneNumber(lecturerCreationRequest.getPhoneNumber());

        User user = createUser(signupRequest,RoleEnum.TEACHER,false,"abc");

        lecturerService.addLecturerInfo(lecturerCreationRequest,user.getUserId());

    }


    public StudentInfo getStudentInfo(Integer id) {
        User usr = userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if(usr.getRole().equals(RoleEnum.STUDENT.name())) {
            Student student = studentRepository.findByAccount_UserId(usr.getUserId()).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));



            StudentInfo studentInfo = studentMapper.toStudentInfo(student);


            return studentInfo;
        }
        return null;

    }

    public NameAndEmail getStudentEmailAndName(Integer id) {
        User user = userRepository.findByUserId(id).orElseThrow();
        NameAndEmail nameAndEmail = new NameAndEmail();
        Student student = studentRepository.getStudentByAccount_UserId(user.getUserId());
        nameAndEmail.setName(student.getName());
        nameAndEmail.setEmail(user.getEmail());
        return nameAndEmail;
    }

    public NameAndEmail getTeacherEmailAndName(Integer id) {
        User user = userRepository.findByUserId(id).orElseThrow();
        Lecturer lecturer = lecturerRepository.getByUser_UserId(id);
        NameAndEmail nameAndEmail = new NameAndEmail();
        nameAndEmail.setName(lecturer.getFullName());
        nameAndEmail.setEmail(user.getEmail());
        return nameAndEmail;
    }
    @Transactional
    public void updateStudentInfo(Integer id, StudentInfo studentInfo) {
        User user = userRepository.getUserByUserId(id).orElseThrow(()-> new AppException(ErrorCode.USER_NOT_FOUND));

        if(user.getRole().equals(RoleEnum.STUDENT.name())) {
            if(studentInfo.getEmail() != null)
                user.setEmail(studentInfo.getEmail());
            if(studentInfo.getPhoneNumber() != null)
                user.setPhoneNumber(studentInfo.getPhoneNumber());
            Student student = studentRepository.getStudentByAccount_UserId(user.getUserId());
            if(studentInfo.getAddress() != null)
                student.setAddress(studentInfo.getAddress());
            if (studentInfo.getGender() != null)
                student.setGender(studentInfo.getGender());
            if(studentInfo.getDateOfBirth() != null)
                student.setNgaySinh(studentInfo.getDateOfBirth());
            if(studentInfo.getJobs() != null)
                student.setJob(studentInfo.getJobs());
            if(studentInfo.getName() != null)
                student.setName(studentInfo.getName());
            if(studentInfo.getImage() != null)
                student.setAvatar(studentInfo.getImage());

            userRepository.save(user);
            studentRepository.save(student);
        }

    }

    // ====================== FORGOT PASSWORD ======================
    @Transactional
    public void requestPasswordReset(String email, String frontendUrl, String adminUrl) {
        // Tìm user theo email
        User user = userRepository.findByPhoneNumberOrEmail(email, email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        
        // Tạo mã xác minh reset password
        Optional<VerificationCode> verificationCode = generateNewVerificationCode(user, VerificationCodeEnum.PASSWORD_RESET);
        
        // Xác định URL dựa trên role: STUDENT dùng frontendUrl, các role còn lại (ADMIN, TEACHER, ACADEMIC_MANAGER) dùng adminUrl
        RoleEnum role = RoleEnum.valueOf(user.getRole());
        String targetUrl = (role == RoleEnum.STUDENT) ? frontendUrl : adminUrl;
        
        // Gửi email với URL phù hợp
        verificationCode.ifPresent(code -> sendVerificationEmail(user, targetUrl, code));
    }

    @Transactional
    public ApiResponse resetPassword(String code, String newPassword, String confirmPassword) {
        // Kiểm tra mật khẩu khớp
        if (!newPassword.equals(confirmPassword)) {
            throw new AppException(ErrorCode.PASSWORD_NOT_MATCH);
        }
        
        // Tìm mã xác minh
        VerificationCode verificationCode = verificationCodeRepository.findByVerificationCode(code)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_CODE));
        
        // Kiểm tra mã hết hạn
        if (verificationCode.getExpiresAt() != null && 
            verificationCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            verificationCodeRepository.delete(verificationCode);
            throw new AppException(ErrorCode.EXPIRED_VERIFICATION_CODE);
        }
        
        // Kiểm tra đúng loại mã
        if (!verificationCode.getType().equals(VerificationCodeEnum.PASSWORD_RESET)) {
            throw new AppException(ErrorCode.INVALID_CODE);
        }
        
        // Cập nhật mật khẩu mới
        User user = verificationCode.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        // Xóa mã xác minh
        verificationCodeRepository.delete(verificationCode);
        
        return ApiResponse.builder()
                .code(1000)
                .message("Password has been reset successfully. Please login with your new password.")
                .build();
    }

    // ====================== ADMIN CREATE STUDENT ======================
    @Transactional
    public StudentInfo createStudentByAdmin(org.example.qlttngoaingu.dto.request.AdminCreateStudentRequest request) {
        // Kiểm tra số điện thoại đã tồn tại
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new AppException(ErrorCode.USER_PHONE_OR_EMAIL_EXIST);
        }
        
        // Kiểm tra email nếu có
        if (request.getEmail() != null && !request.getEmail().isEmpty() 
            && userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.USER_PHONE_OR_EMAIL_EXIST);
        }

        // Tạo user với mật khẩu mặc định "123456", không gửi email xác thực
        User user = new User();
        user.setPhoneNumber(request.getPhoneNumber());
        user.setEmail(request.getEmail());
        user.setRole(RoleEnum.STUDENT.name());
        user.setPasswordHash(passwordEncoder.encode("123456")); // Mật khẩu mặc định
        user.setIsVerified(true); // Admin tạo nên đã xác thực sẵn
        user.setCreatedAt(LocalDateTime.now());
        userRepository.save(user);

        // Tạo Student
        Student student = new Student();
        student.setAccount(user);
        student.setName(request.getName());
        student.setNgaySinh(request.getDateOfBirth());
        student.setGender(request.getGender());
        student.setAddress(request.getAddress());
        student.setJob(request.getJob());
        studentRepository.save(student);

        return studentMapper.toStudentInfo(student);
    }


}
