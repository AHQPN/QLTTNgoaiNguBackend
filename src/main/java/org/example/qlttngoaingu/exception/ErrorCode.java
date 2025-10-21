package org.example.qlttngoaingu.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // General
    UNCATEGORIZED(HttpStatus.INTERNAL_SERVER_ERROR, 9999, "Uncategorized error"),
    INVALID_ACTION(HttpStatus.BAD_REQUEST, 9998, "Invalid action, must be ADD or DELETE or UPDATE"),

    // User errors
    USER_EXIST(HttpStatus.BAD_REQUEST, 1001, "User already exists"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, 1002, "User not found"),
    USER_NOT_VERIFIED(HttpStatus.FORBIDDEN, 1003, "User not verified, please check your email to verify"),
    USER_INVALID_PASSWORD(HttpStatus.BAD_REQUEST, 1004, "Password must be at least 6 characters"),
    USER_PHONE_OR_EMAIL_EXIST(HttpStatus.BAD_REQUEST, 1005, "Phone number or email already exists"),
    FAIL_TO_VERIFY_EMAIL(HttpStatus.INTERNAL_SERVER_ERROR, 1007, "Failed to send verification email"),

    // Course errors
    COURSE_NOT_FOUND(HttpStatus.NOT_FOUND, 2000, "Course not found"),
    BOOK_EXIST(HttpStatus.BAD_REQUEST, 2001, "Book already exists"),
    BOOK_OUT_OF_STOCK(HttpStatus.BAD_REQUEST, 2002, "Book out of stock"),
    BOOK_INVALID_GENRE(HttpStatus.BAD_REQUEST, 2003, "Invalid book genre"),
    BOOK_INVALID_PUBLISHER(HttpStatus.BAD_REQUEST, 2004, "Invalid book publisher"),

    // Module
    MISS_MATCH_COURSE(HttpStatus.NOT_FOUND, 3000, "Not found this module in course"),
    MODULE_NOT_FOUND(HttpStatus.BAD_REQUEST, 3001, "Module not found"),

    // Genre errors
    GENRE_NOT_FOUND(HttpStatus.NOT_FOUND, 4000, "Genre not found"),
    GENRE_EXIST(HttpStatus.BAD_REQUEST, 4001, "Genre already exists"),

    // Publisher errors
    PUBLISHER_NOT_FOUND(HttpStatus.NOT_FOUND, 5000, "Publisher not found"),
    PUBLISHER_EXIST(HttpStatus.BAD_REQUEST, 5001, "Publisher already exists"),

    // Borrow slip errors
    BORROW_SLIP_NOT_FOUND(HttpStatus.NOT_FOUND, 6000, "Borrow slip not found"),
    BORROW_SLIP_OVERDUE(HttpStatus.BAD_REQUEST, 6001, "Borrow slip is overdue"),
    BORROW_SLIP_ALREADY_RETURNED(HttpStatus.BAD_REQUEST, 6002, "Borrow slip already returned"),

    // Borrow slip detail errors
    BORROW_SLIP_DETAIL_NOT_FOUND(HttpStatus.NOT_FOUND, 7000, "Borrow slip detail not found"),

    // Payment errors
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, 8000, "Payment not found"),
    PAYMENT_FAILED(HttpStatus.BAD_REQUEST, 8001, "Payment failed"),

    // Purchase slip errors
    PURCHASE_SLIP_NOT_FOUND(HttpStatus.NOT_FOUND, 9000, "Purchase slip not found"),

    // Refresh token errors
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, 10000, "Refresh token not found"),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, 10001, "Refresh token expired"),
    REFRESH_TOKEN_REVOKED(HttpStatus.UNAUTHORIZED, 10002, "Refresh token revoked"),

    // Auth errors
    AUTH_INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, 11000, "Invalid authentication credentials"),
    AUTH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, 11001, "Authentication token expired"),
    AUTH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, 11002, "Invalid authentication token"),
    AUTH_ACCESS_DENIED(HttpStatus.FORBIDDEN, 11003, "Access denied"),
    UNAUTHENTICATED(HttpStatus.UNAUTHORIZED, 11004, "Unauthenticated"),
    EXPIRED_VERIFICATION_CODE(HttpStatus.GONE, 11005, "Expired verification code"),

    // Field validation
    FIELD_NOT_BLANK(HttpStatus.BAD_REQUEST, 12000, "Field not blank"),
    INVALID_EMAIL(HttpStatus.BAD_REQUEST, 12001, "Invalid email"),
    INVALID_PHONE_NUMBER(HttpStatus.BAD_REQUEST, 12002, "Invalid phone number"),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, 12003, "Password must be at least 6 characters"),
    INVALID_EMAIL_OR_PHONE_NUMBER(HttpStatus.BAD_REQUEST, 12004, "Invalid email or phone number"),
    INVALID_PRICE(HttpStatus.BAD_REQUEST, 12005, "Price cannot be negative"),
    INVALID_CODE(HttpStatus.BAD_REQUEST, 12006, "Invalid verification code"),
    INVALID_DURATION(HttpStatus.BAD_REQUEST, 12008, "Duration cannot be negative"),
    IMAGE_MISSING(HttpStatus.BAD_REQUEST, 12009, "Image missing"),

    // Course request validation errors
    COURSE_NAME_NOT_BLANK(HttpStatus.BAD_REQUEST, 7000, "Course name cannot be blank"),
    COURSE_NAME_TOO_SHORT(HttpStatus.BAD_REQUEST, 7001, "Course name must be at least 3 characters"),
    COURSE_STUDY_HOURS_INVALID(HttpStatus.BAD_REQUEST, 7002, "Study hours must be positive"),
    COURSE_TUITION_FEE_INVALID(HttpStatus.BAD_REQUEST, 7003, "Tuition fee must be positive"),
    COURSE_NUMBER_OF_SESSIONS_INVALID(HttpStatus.BAD_REQUEST, 7004, "Number of sessions must be positive"),
    COURSE_VIDEO_INVALID(HttpStatus.BAD_REQUEST, 7005, "Video URL is invalid"),
    COURSE_OBJECTIVES_EMPTY(HttpStatus.BAD_REQUEST, 7006, "Course objectives cannot be empty"),
    COURSE_MODULES_EMPTY(HttpStatus.BAD_REQUEST, 7007, "Course modules cannot be empty"),
    DURATION_TOO_LONG(HttpStatus.BAD_REQUEST, 7008, "Duration of all modules must be less than number of sessions"),
    DURATION_MUST_POSITIVE(HttpStatus.BAD_REQUEST, 7009, "Duration must be positive");


    private final HttpStatus httpStatus;
    private final int code;
    private final String message;
}
