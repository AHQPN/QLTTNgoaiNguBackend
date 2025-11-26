package org.example.qlttngoaingu.controller;

import lombok.AllArgsConstructor;
import org.example.qlttngoaingu.dto.request.LecturerCreationRequest;
import org.example.qlttngoaingu.dto.request.SignupRequest;
import org.example.qlttngoaingu.dto.request.UserCreateRequest;
import org.example.qlttngoaingu.dto.request.UserUpdateRequest;
import org.example.qlttngoaingu.dto.response.ApiResponse;
import org.example.qlttngoaingu.dto.response.NameAndEmail;
import org.example.qlttngoaingu.entity.User;
import org.example.qlttngoaingu.security.model.UserDetailsImpl;
import org.example.qlttngoaingu.service.LecturerService;
import org.example.qlttngoaingu.service.UserService;
import org.example.qlttngoaingu.service.enums.RoleEnum;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class UserController {
    private final UserService userService;
    private final LecturerService lecturerService;
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
        signupRequest.setPhoneNumber(user.getPhoneNumber());
        signupRequest.setPassword("123456");
        userService.createUser(signupRequest,RoleEnum.ADMIN,false,siteUrl);

        return ResponseEntity.ok().body(ApiResponse.builder().message("User has been created").build());
    }
    @PutMapping("/{id}")
    public ResponseEntity updateUserInfo(@RequestBody UserUpdateRequest userUpdateRequest)
    {
        return ResponseEntity.ok().body(ApiResponse.builder().message("User has been updated").build());
    }

    @PostMapping("/add-lecturer")
    public ResponseEntity<ApiResponse> addLectureUser(@RequestBody LecturerCreationRequest lecturerCreationRequest)
    {
        userService.AddLecturerUser(lecturerCreationRequest);
        return ResponseEntity.ok().body(ApiResponse.builder().message("User has been created").build());
    }

    @GetMapping("/student-info")
    public ResponseEntity<ApiResponse> getStudentInfo(@AuthenticationPrincipal UserDetailsImpl principal) {
        return ResponseEntity.ok().body(ApiResponse.builder().data(userService.getStudentInfo(principal.getId())).build());

    }
    @GetMapping("/name-email")
    public ResponseEntity<ApiResponse> getNameEmail(@AuthenticationPrincipal UserDetailsImpl principal) {
        NameAndEmail nameAndEmail = new NameAndEmail();
        if(Objects.equals(principal.getRole(), RoleEnum.STUDENT.name()))
            nameAndEmail = userService.getStudentEmailAndName(principal.getId());

        else if(Objects.equals(principal.getRole(), RoleEnum.TEACHER.name()))
            nameAndEmail = userService.getTeacherEmailAndName(principal.getId());

        return ResponseEntity.ok().body(ApiResponse.builder().data(nameAndEmail).build());
    }

//    @GetMapping("/students")
//    public ResponseEntity<ApiResponse> getStudents(@RequestParam(defaultValue = "0") int page,
//                                                   @RequestParam(defaultValue = "15") int size) {
//        userService.getStudents();
//    }



//    @PostMapping("/get-user-info")
//    public ResponseEntity<ApiResponse> getUserInfo(@AuthenticationPrincipal UserDetailsImpl principal)
//    {
//    }
}
