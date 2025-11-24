package org.example.qlttngoaingu.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.qlttngoaingu.dto.request.LoginRequest;
import org.example.qlttngoaingu.dto.request.SignupRequest;
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
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;


    // ===== LOGIN =====
    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletResponse response) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getIdentifier(), loginRequest.getPassword())
        );

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        if (!userDetails.isVerified()) {
            throw new AppException(ErrorCode.USER_NOT_VERIFIED);
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);



        String accessToken = jwtUtils.generateJwtToken(authentication);

        // Tạo refresh token
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

        // Set HTTP-only cookie cho refresh token
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken.getRefreshToken())
                .httpOnly(true)
                .secure(false)
                .path("/auth/refreshtoken")
                .maxAge(Duration.ofDays(7))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        LoginResponse loginResponse = new LoginResponse(accessToken,refreshToken.getRefreshToken(),userDetails.getRole(),userDetails.getId());
        return ResponseEntity.ok().body(ApiResponse.builder().message("Login Successfully").data(loginResponse).build());
    }

    // ===== SIGNUP =====
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse> signup(@Valid @RequestBody StudentSignupRequest studentSignupRequest, HttpServletRequest request, @Value("${APP_SITE_URL}") String siteUrl) {

        RoleEnum role = RoleEnum.STUDENT;
        StudentInfo user = userService.signUpForStudent(studentSignupRequest,role, true, siteUrl);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.builder()
                        .message("We have sent a verification email, please check your inbox")
                        .build());
    }



    // ===== REFRESH TOKEN =====
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

        String accessToken = jwtUtils.generateTokenFromIdentifier(user.getPhoneNumber());

        // Set new refresh token cookie
        ResponseCookie cookie = ResponseCookie.from("refreshToken", newRefreshToken.getRefreshToken())
                .httpOnly(true)
                .secure(false)
                .path("/auth/refreshtoken")
                .maxAge(Duration.ofDays(7))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        TokenRefreshResponse tokenRefreshResponse = new TokenRefreshResponse(accessToken, newRefreshToken.getRefreshToken());

        return ResponseEntity.ok().body(ApiResponse.builder()
                .message("New Refresh Token and Access Token are created Successfully")
                .data(tokenRefreshResponse)
                .build());
    }

    // ===== Resend verified code  =====
    @PostMapping("/resend")
    public ResponseEntity<String> sendVerification(
            @RequestParam String email,
            @RequestParam VerificationCodeEnum type,
            @Value("${APP_SITE_URL}") String siteUrl) {

        // 1️⃣ Tìm user theo email
        Optional<User> optionalUser = userService.getUserByIdentifier(email);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.badRequest().body("User not found");
        }
        User user = optionalUser.get();

        // 2️⃣ Tạo mã xác minh mới
        Optional<VerificationCode> optionalCode = userService.generateNewVerificationCode(user, type);


        // 3️⃣ Gửi email xác minh
        optionalCode.ifPresent(code ->
                userService.sendVerificationEmail(user, siteUrl, code)
        );

        return ResponseEntity.ok("Verification email sent successfully!");
    }

    // ====================== XÁC MINH CODE ======================
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
                "        body { font-family: Arial, sans-serif; background-color: #f5f6fa; display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0; }\n" +
                "        .container { background-color: #fff; padding: 40px; border-radius: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.1); text-align: center; }\n" +
                "        h1 { color: " + (success ? "#2ecc71" : "#e74c3c") + "; margin-bottom: 20px; }\n" +
                "        p { font-size: 16px; color: #333; margin-bottom: 30px; }\n" +
                "        a.button { display: inline-block; text-decoration: none; background-color: #3498db; color: #fff; padding: 12px 24px; border-radius: 6px; transition: background-color 0.3s ease; }\n" +
                "        a.button:hover { background-color: #2980b9; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"container\">\n" +
                "        <h1>" + (success ? "Verification Successful!" : "Verification Failed") + "</h1>\n" +
                "        <p>" + result.getMessage() + "</p>\n" +
                "        <a class=\"button\" href=\"http://localhost:3000\">Go Back to App</a>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";

        return ResponseEntity.ok().body(html);
    }

    // ===== LOGOUT =====
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(HttpServletResponse response) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            String identifier = authentication.getName();
            User user = userService.getUserByIdentifier(identifier)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

            refreshTokenService.deleteByUserId(user.getUserId());
            SecurityContextHolder.clearContext();


            ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                    .httpOnly(true)
                    .secure(false)
                    .path("/auth/refreshtoken")
                    .maxAge(0)
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        }

        return ResponseEntity.ok(ApiResponse.builder().message("Logout successful").build());
    }
}
