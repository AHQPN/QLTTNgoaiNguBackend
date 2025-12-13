package org.example.qlttngoaingu.controller;

import java.util.List;

import org.example.qlttngoaingu.dto.request.ModuleRequest;
import org.example.qlttngoaingu.dto.request.ModuleUpdateBasicInfoRequest;
import org.example.qlttngoaingu.dto.request.ModuleUpdateRequest;
import org.example.qlttngoaingu.dto.response.ApiResponse;
import org.example.qlttngoaingu.entity.Module;
import org.example.qlttngoaingu.service.ModuleService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

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
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_MANAGER')")
    public ResponseEntity<ApiResponse> addModule(@PathVariable Integer id,@Valid @RequestBody ModuleRequest request)
    {
        moduleService.addModule(id, request);
        return ResponseEntity.ok().body(ApiResponse.builder().message("Successfully added Module").build());
    }



    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_MANAGER')")
    public ResponseEntity<?> updateModuleDetail(@PathVariable Integer id,
                                               @RequestBody ModuleUpdateRequest request) {
        moduleService.updateModuleDetail(id, request);
        return ResponseEntity.ok().body(ApiResponse.builder().message("Module updated successfully").build());
    }

    @PutMapping("/{id}/info")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_MANAGER')")

    public ResponseEntity<?> updateModuleInfo(@PathVariable Integer id, @RequestBody ModuleUpdateBasicInfoRequest request) {
        moduleService.updateModuleBasicInfo(id,request.getModuleName(), request.getDuration());
        return ResponseEntity.ok().body(ApiResponse.builder().message("Module updated successfully").build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_MANAGER')")
    public ResponseEntity<?> deleteModule(@PathVariable Integer id) {
        moduleService.deleteModule(id);
        return ResponseEntity.ok().body(ApiResponse.builder().message("Delete course successfully"));
    }




}
