package org.example.qlttngoaingu.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test")
public class TestController {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @GetMapping("/encode-password")
    public ResponseEntity<String> encodePassword(@RequestParam String password) {
        String encoded = passwordEncoder.encode(password);
        return ResponseEntity.ok("BCrypt: " + encoded);
    }
}
