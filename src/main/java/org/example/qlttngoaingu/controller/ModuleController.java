package org.example.qlttngoaingu.controller;

import jakarta.validation.Valid;
import org.example.qlttngoaingu.dto.request.ModuleRequest;
import org.example.qlttngoaingu.dto.request.ModuleUpdateRequest;
import org.example.qlttngoaingu.dto.response.ApiResponse;
import org.example.qlttngoaingu.dto.response.CourseResponse;
import org.example.qlttngoaingu.entity.CourseSkill;
import org.example.qlttngoaingu.service.ModuleService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.example.qlttngoaingu.entity.Module;
import java.util.List;

@RestController
@RequestMapping("/modules")
public class ModuleController {
    private final ModuleService moduleService;

    public ModuleController(ModuleService moduleService) {
        this.moduleService = moduleService;
    }


    @GetMapping
    public ResponseEntity<List<Module>> getModulesByCourseId(@RequestParam Integer courseId) {
        List<Module> modules = moduleService.getModulesByCourseId(courseId);
        return ResponseEntity.ok(modules);
    }
    @PostMapping({"/{id}"})
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> addModule(@PathVariable Integer id,@Valid @RequestBody ModuleRequest request)
    {
        moduleService.addModule(id, request);
        return ResponseEntity.ok().body(ApiResponse.builder().message("Successfully added Module").build());
    }



    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateModule(@PathVariable Integer id,
                                               @RequestBody ModuleUpdateRequest request) {
        moduleService.updateModuleDetail(id, request);
        return ResponseEntity.ok().body(ApiResponse.builder().message("Module updated successfully").build());
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteModule(@PathVariable Integer id) {
        moduleService.deleteModule(id);
        return ResponseEntity.ok().body(ApiResponse.builder().message("Delete course successfully"));
    }




}
