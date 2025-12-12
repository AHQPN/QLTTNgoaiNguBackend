package org.example.qlttngoaingu.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.example.qlttngoaingu.entity.RefreshToken;
import org.example.qlttngoaingu.entity.User;
import org.example.qlttngoaingu.exception.AppException;
import org.example.qlttngoaingu.exception.ErrorCode;
import org.example.qlttngoaingu.repository.RefreshTokenRepository;
import org.example.qlttngoaingu.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshTokenService {
    @Value("${app.jwtRefreshExpirationMs}")
    private Long refreshTokenDurationMs;
    
    @Value("${spring.profiles.active:dev}")
    private String activeProfile;
    
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, UserRepository userRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByRefreshToken(token);
    }

    public RefreshToken createRefreshToken(Integer userId) {
        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setUser(userRepository.findById(userId).get());
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setRefreshToken(UUID.randomUUID().toString());
        refreshToken.setRevoked(false);
        refreshToken = refreshTokenRepository.save(refreshToken);
        return refreshToken;
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new AppException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }
        if (token.getRevoked())
            throw new AppException(ErrorCode.REFRESH_TOKEN_REVOKED);

        return token;
    }

    public RefreshToken rotateToken(RefreshToken oldToken) {
        oldToken.setRevoked(true);
        refreshTokenRepository.save(oldToken);

        // Tạo token mới
        return createRefreshToken(oldToken.getUser().getUserId());
    }

    public void setRevoked(RefreshToken refreshToken) {
        refreshToken.setRevoked(true);
    }

    @Transactional
    public void deleteByUserId(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        refreshTokenRepository.deleteAllByUser(user);
    }

    /**
     * Tạo ResponseCookie cho refresh token với cấu hình phù hợp theo môi trường
     * Production: secure=true, sameSite=None
     * Dev: secure=false, sameSite=Lax (default)
     * 
     * @param tokenValue Giá trị của refresh token
     * @param maxAgeSeconds Thời gian sống của cookie (giây)
     * @return ResponseCookie đã được cấu hình
     */
    public ResponseCookie createRefreshTokenCookie(String tokenValue, long maxAgeSeconds) {
        boolean isProduction = "prod".equalsIgnoreCase(activeProfile) 
                            || "production".equalsIgnoreCase(activeProfile);
        
        ResponseCookie.ResponseCookieBuilder cookieBuilder = ResponseCookie
                .from("refreshToken", tokenValue)
                .httpOnly(true)
                .path("/auth/refreshtoken")
                .maxAge(Duration.ofSeconds(maxAgeSeconds));
        
        if (isProduction) {
            cookieBuilder.secure(true).sameSite("None");
        } else {
            cookieBuilder.secure(false);
        }
        
        return cookieBuilder.build();
    }

    /**
     * Tạo cookie để xóa refresh token (dùng khi logout)
     * 
     * @return ResponseCookie với giá trị rỗng và maxAge=0
     */
    public ResponseCookie createDeleteRefreshTokenCookie() {
        return createRefreshTokenCookie("", 0);
    }

}
