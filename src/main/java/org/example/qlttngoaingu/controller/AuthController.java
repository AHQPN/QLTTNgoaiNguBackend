package org.example.qlttngoaingu.controller;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

import org.example.qlttngoaingu.dto.request.ForgotPasswordRequest;
import org.example.qlttngoaingu.dto.request.LoginRequest;
import org.example.qlttngoaingu.dto.request.ResetPasswordRequest;
import org.example.qlttngoaingu.dto.request.StudentSignupRequest;
import org.example.qlttngoaingu.dto.response.ApiResponse;
import org.example.qlttngoaingu.dto.response.LoginResponse;
import org.example.qlttngoaingu.dto.response.StudentInfo;
import org.example.qlttngoaingu.dto.response.TokenRefreshResponse;
import org.example.qlttngoaingu.entity.RefreshToken;
import org.example.qlttngoaingu.entity.User;
import org.example.qlttngoaingu.entity.VerificationCode;
import org.example.qlttngoaingu.exception.AppException;
import org.example.qlttngoaingu.exception.ErrorCode;
import org.example.qlttngoaingu.repository.VerificationCodeRepository;
import org.example.qlttngoaingu.security.jwt.JwtUtils;
import org.example.qlttngoaingu.security.model.UserDetailsImpl;
import org.example.qlttngoaingu.service.RefreshTokenService;
import org.example.qlttngoaingu.service.UserService;
import org.example.qlttngoaingu.service.enums.RoleEnum;
import org.example.qlttngoaingu.service.enums.VerificationCodeEnum;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
@Tag(name = "Authentication", description = "API xác thực và quản lý tài khoản người dùng")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final VerificationCodeRepository verificationCodeRepository;

    // ===== LOGIN =====
    @Operation(summary = "Đăng nhập", description = "Đăng nhập vào hệ thống bằng số điện thoại/email và mật khẩu. Trả về access token và refresh token.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Đăng nhập thành công", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = "{\"code\": 1000, \"message\": \"Login Successfully\", \"data\": {\"accessToken\": \"eyJhbGciOi...\", \"refreshToken\": \"uuid-token\", \"role\": \"STUDENT\", \"userId\": 1}}"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Sai thông tin đăng nhập"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Tài khoản chưa được xác thực email")
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@Valid @RequestBody LoginRequest loginRequest,
                                             HttpServletResponse response) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getIdentifier(), loginRequest.getPassword()));

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        if (!userDetails.isVerified()) {
            throw new AppException(ErrorCode.USER_NOT_VERIFIED);
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = jwtUtils.generateJwtToken(authentication);

        // Tạo refresh token
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

        // Set HTTP-only cookie cho refresh token
        ResponseCookie cookie = refreshTokenService.createRefreshTokenCookie(
                refreshToken.getRefreshToken(), 
                Duration.ofDays(7).getSeconds());
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        LoginResponse loginResponse = new LoginResponse(accessToken, refreshToken.getRefreshToken(),
                userDetails.getRole(), userDetails.getId());
        return ResponseEntity.ok()
                .body(ApiResponse.builder().message("Login Successfully").data(loginResponse).build());
    }

    // ===== SIGNUP =====
    @Operation(summary = "Đăng ký tài khoản học viên", description = "Đăng ký tài khoản mới cho học viên. Hệ thống sẽ gửi email xác thực đến địa chỉ email đã đăng ký.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Đăng ký thành công, email xác thực đã được gửi"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Số điện thoại hoặc email đã tồn tại")
    })
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse> signup(@Valid @RequestBody StudentSignupRequest studentSignupRequest,
                                              HttpServletRequest request, @Value("${app.site-url}") String siteUrl) {

        RoleEnum role = RoleEnum.STUDENT;
        StudentInfo user = userService.signUpForStudent(studentSignupRequest, role, true, siteUrl);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.builder()
                        .message("We have sent a verification email, please check your inbox")
                        .build());
    }

    // ===== REFRESH TOKEN =====
    @Operation(summary = "Làm mới access token", description = "Sử dụng refresh token để lấy access token mới khi token cũ hết hạn. Refresh token có thể gửi qua cookie hoặc request body.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tạo token mới thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Refresh token không hợp lệ hoặc đã hết hạn")
    })
    @PostMapping("/refreshtoken")
    public ResponseEntity<ApiResponse> refreshToken(
            @CookieValue(name = "refreshToken", required = false) String cookieRefreshToken,
            @RequestBody(required = false) Map<String, String> body,
            HttpServletResponse response) {

        String requestRefreshToken = cookieRefreshToken; // lấy giá trị từ cookie
        if (requestRefreshToken == null && body != null) {
            requestRefreshToken = body.get("refreshToken");
        }

        if (requestRefreshToken == null) {
            throw new AppException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }

        RefreshToken refreshToken = refreshTokenService.findByToken(requestRefreshToken)
                .orElseThrow(() -> new AppException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));

        refreshTokenService.verifyExpiration(refreshToken);

        User user = refreshToken.getUser();

        refreshTokenService.setRevoked(refreshToken);
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user.getUserId());

        String accessToken = jwtUtils.generateTokenFromIdentifier(user);

        // Set new refresh token cookie
        ResponseCookie cookie = refreshTokenService.createRefreshTokenCookie(
                newRefreshToken.getRefreshToken(), 
                Duration.ofDays(7).getSeconds());
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        TokenRefreshResponse tokenRefreshResponse = new TokenRefreshResponse(accessToken,
                newRefreshToken.getRefreshToken());

        return ResponseEntity.ok().body(ApiResponse.builder()
                .message("New Refresh Token and Access Token are created Successfully")
                .data(tokenRefreshResponse)
                .build());
    }

    // ===== Resend verified code =====
    @Operation(summary = "Gửi lại mã xác thực", description = "Gửi lại email chứa mã xác thực cho người dùng. Có thể dùng để gửi lại email xác thực tài khoản hoặc reset password.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Email xác thực đã được gửi lại"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Không tìm thấy người dùng")
    })
    @PostMapping("/resend")
    public ResponseEntity<String> sendVerification(
            @RequestParam String email,
            @RequestParam VerificationCodeEnum type,
            @Value("${app.site-url}") String siteUrl) {

        // Tìm user theo email
        Optional<User> optionalUser = userService.getUserByIdentifier(email);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.badRequest().body("User not found");
        }
        User user = optionalUser.get();

        // Tạo mã xác minh mới
        Optional<VerificationCode> optionalCode = userService.generateNewVerificationCode(user, type);

        // Gửi email xác minh
        optionalCode.ifPresent(code -> userService.sendVerificationEmail(user, siteUrl, code));

        return ResponseEntity.ok("Verification email sent successfully!");
    }

    // ====================== XÁC MINH CODE ======================
    @Operation(summary = "Xác thực email/mã xác nhận", description = "Xác thực tài khoản qua mã từ email. Trả về trang HTML thông báo kết quả xác thực.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Trả về trang HTML thông báo kết quả", content = @Content(mediaType = "text/html"))
    })
    @GetMapping("/verify")
    public ResponseEntity<String> verifyCode(
            @RequestParam String code,
            @RequestParam VerificationCodeEnum type) {

        ApiResponse result = userService.verify(code, type);
        boolean success = result.getCode() == 1000;
        String html = "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>Verification Status</title>\n" +
                "    <style>\n" +
                "        body { font-family: Arial, sans-serif; background-color: #f5f6fa; display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0; }\n"
                +
                "        .container { background-color: #fff; padding: 40px; border-radius: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.1); text-align: center; }\n"
                +
                "        h1 { color: " + (success ? "#2ecc71" : "#e74c3c") + "; margin-bottom: 20px; }\n" +
                "        p { font-size: 16px; color: #333; margin-bottom: 30px; }\n" +
                "        a.button { display: inline-block; text-decoration: none; background-color: #3498db; color: #fff; padding: 12px 24px; border-radius: 6px; transition: background-color 0.3s ease; }\n"
                +
                "        a.button:hover { background-color: #2980b9; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"container\">\n" +
                "        <h1>" + (success ? "Verification Successful!" : "Verification Failed") + "</h1>\n" +
                "        <p>" + result.getMessage() + "</p>\n" +
                "        <a class=\"button\" href=\"https://quan-ly-trung-tam-ngoai-ngu.vercel.app\">Go Back to App</a>\n"
                +
                "    </div>\n" +
                "</body>\n" +
                "</html>";

        return ResponseEntity.ok().body(html);
    }

    // ===== LOGOUT =====
    @Operation(summary = "Đăng xuất", description = "Đăng xuất khỏi hệ thống. Refresh token sẽ bị xóa và người dùng phải đăng nhập lại.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Đăng xuất thành công")
    })
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(HttpServletResponse response) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            String identifier = authentication.getName();
            User user = userService.getUserByIdentifier(identifier)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

            refreshTokenService.deleteByUserId(user.getUserId());
            SecurityContextHolder.clearContext();

            ResponseCookie cookie = refreshTokenService.createDeleteRefreshTokenCookie();
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        }

        return ResponseEntity.ok(ApiResponse.builder().message("Logout successful").build());
    }

    // ===== FORGOT PASSWORD - Gửi email reset password =====
    @Operation(summary = "Quên mật khẩu", description = "Gửi yêu cầu đặt lại mật khẩu. Hệ thống sẽ gửi email chứa link đặt lại mật khẩu (có hiệu lực trong 2 phút).")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Email đặt lại mật khẩu đã được gửi"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Email không tồn tại trong hệ thống")
    })
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request,
            @Value("${app.frontend-url}") String frontendUrl,
            @Value("${app.admin-url}") String adminUrl) {

        userService.requestPasswordReset(request.getEmail(), frontendUrl, adminUrl);

        return ResponseEntity.ok().body(ApiResponse.builder()
                .message("Password reset email has been sent. Please check your inbox.")
                .build());
    }

    // ===== RESET PASSWORD - Đổi mật khẩu với code =====
    @Operation(summary = "Đặt lại mật khẩu", description = "Đặt lại mật khẩu mới bằng mã xác thực từ email. Sau khi đổi mật khẩu thành công, người dùng sẽ bị đăng xuất khỏi tất cả thiết bị.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Đặt lại mật khẩu thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Mã xác thực không hợp lệ hoặc mật khẩu không khớp"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "410", description = "Mã xác thực đã hết hạn")
    })
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        ApiResponse response = userService.resetPassword(
                request.getCode(),
                request.getNewPassword(),
                request.getConfirmPassword());

        return ResponseEntity.ok().body(response);
    }

    // ===== VERIFY RESET PASSWORD CODE - Xác minh code có hợp lệ không =====
    @Operation(summary = "Kiểm tra mã đặt lại mật khẩu", description = "Xác minh mã đặt lại mật khẩu có hợp lệ và chưa hết hạn hay không. Endpoint này là optional, có thể bỏ qua và gọi trực tiếp /reset-password.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Mã hợp lệ, trả về email của người dùng"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Mã không hợp lệ"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "410", description = "Mã đã hết hạn")
    })
    @GetMapping("/verify-reset-code")
    public ResponseEntity<ApiResponse> verifyResetCode(
            @Parameter(description = "Mã xác thực từ email", required = true) @RequestParam String code) {

        Optional<VerificationCode> verificationCode = verificationCodeRepository.findByVerificationCode(code);

        if (verificationCode.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_CODE);
        }

        VerificationCode vc = verificationCode.get();

        // Kiểm tra hết hạn
        if (vc.getExpiresAt() != null && vc.getExpiresAt().isBefore(java.time.LocalDateTime.now())) {
            throw new AppException(ErrorCode.EXPIRED_VERIFICATION_CODE);
        }

        // Kiểm tra đúng loại
        if (!vc.getType().equals(VerificationCodeEnum.PASSWORD_RESET)) {
            throw new AppException(ErrorCode.INVALID_CODE);
        }

        return ResponseEntity.ok().body(ApiResponse.builder()
                .code(1000)
                .message("Valid reset code")
                .data(Map.of("email", vc.getUser().getEmail()))
                .build());
    }

    // ===== CHANGE PASSWORD - Đổi mật khẩu khi đã đăng nhập =====
    @Operation(summary = "Đổi mật khẩu", description = "Đổi mật khẩu cho người dùng đã đăng nhập. Yêu cầu xác thực mật khẩu hiện tại. Sau khi đổi mật khẩu, người dùng sẽ bị đăng xuất khỏi tất cả thiết bị.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Đổi mật khẩu thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Mật khẩu hiện tại không đúng hoặc mật khẩu mới không hợp lệ"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chưa đăng nhập")
    })
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse> changePassword(
            @Valid @RequestBody org.example.qlttngoaingu.dto.request.ChangePasswordRequest request,
            @org.springframework.security.core.annotation.AuthenticationPrincipal UserDetailsImpl principal) {

        // Validate confirm password
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new AppException(ErrorCode.PASSWORD_NOT_MATCH);
        }

        // Validate new password != current password
        if (request.getCurrentPassword().equals(request.getNewPassword())) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .code(400)
                    .message("Mật khẩu mới phải khác mật khẩu hiện tại")
                    .build());
        }

        // Call service to change password
        userService.changePassword(
                principal.getId(),
                request.getCurrentPassword(),
                request.getNewPassword());

        // Clear all refresh tokens (logout from all devices)
        refreshTokenService.deleteByUserId(principal.getId());

        return ResponseEntity.ok().body(ApiResponse.builder()
                .code(1000)
                .message("Đổi mật khẩu thành công. Vui lòng đăng nhập lại.")
                .build());
    }
}