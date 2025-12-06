package org.example.qlttngoaingu.controller;

import lombok.RequiredArgsConstructor;
import org.example.qlttngoaingu.dto.response.ApiResponse;
import org.example.qlttngoaingu.entity.Degree;
import org.example.qlttngoaingu.repository.DegreeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/degrees")
@RequiredArgsConstructor
public class DegreeController {
    private final DegreeRepository degreeRepository;

    /**
     * GET /degrees - Lấy tất cả loại bằng cấp
     */
    @GetMapping
    public ResponseEntity<ApiResponse> getAll() {
        List<Degree> degrees = degreeRepository.findAll();
        
        var data = degrees.stream()
                .map(d -> new DegreeDTO(d.getId(), d.getName()))
                .collect(Collectors.toList());
        
        return ResponseEntity.ok()
                .body(ApiResponse.builder()
                        .data(data)
                        .build());
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    static class DegreeDTO {
        private Integer id;
        private String name;
    }
}
