package org.example.qlttngoaingu.controller;

import lombok.AllArgsConstructor;
import org.example.qlttngoaingu.dto.request.SignupRequest;
import org.example.qlttngoaingu.dto.request.UserCreateRequest;
import org.example.qlttngoaingu.dto.request.UserUpdateRequest;
import org.example.qlttngoaingu.dto.response.ApiResponse;
import org.example.qlttngoaingu.entity.User;
import org.example.qlttngoaingu.service.UserService;
import org.example.qlttngoaingu.service.enums.RoleEnum;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> createUser(@RequestBody UserCreateRequest user, @Value("${APP_SITE_URL}") String siteUrl) {
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setEmail(user.getEmail());
        signupRequest.setPassword(user.getPhoneNumber());
        RoleEnum role = RoleEnum.valueOf(user.getRole());
        userService.createUser(signupRequest,role,true,siteUrl);
        return ResponseEntity.ok().body(ApiResponse.builder().message("User has been created").build());
    }

    @PostMapping("/seedUser")
    public ResponseEntity<ApiResponse> createSeedUser(@RequestBody UserCreateRequest user,@Value("${APP_SITE_URL}") String siteUrl) {
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setEmail(user.getEmail());
        signupRequest.setPassword(user.getPhoneNumber());
        userService.createUser(signupRequest,RoleEnum.ADMIN,false,siteUrl);

        return ResponseEntity.ok().body(ApiResponse.builder().message("User has been created").build());
    }
    @PutMapping("/{id}")
    public ResponseEntity updateUserInfo(@RequestBody UserUpdateRequest userUpdateRequest)
    {
        return ResponseEntity.ok().body(ApiResponse.builder().message("User has been updated").build());
    }
}
