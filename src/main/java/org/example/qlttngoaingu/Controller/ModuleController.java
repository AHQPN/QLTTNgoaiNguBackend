package org.example.qlttngoaingu.Controller;

import jakarta.validation.Valid;
import org.example.qlttngoaingu.Dto.Request.ModuleRequest;
import org.example.qlttngoaingu.Dto.Response.ApiResponse;
import org.example.qlttngoaingu.Service.ModuleService;
import org.springframework.http.ResponseEntity;
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

    // -------------------------------
    // Lấy danh sách module theo courseId
    // -------------------------------
    @GetMapping
    public ResponseEntity<List<Module>> getModulesByCourseId(@RequestParam Integer courseId) {
        List<Module> modules = moduleService.getmodules(courseId);
        return ResponseEntity.ok(modules);
    }
    @PostMapping({"/{id}"})
    public ResponseEntity<ApiResponse> addModule(@PathVariable Integer id,@Valid @RequestBody ModuleRequest request)
    {
        moduleService.addModule(id, request);
        return ResponseEntity.ok().body(ApiResponse.builder().message("Successfully added Module").build());
    }



    @PutMapping("/{id}")
    public ResponseEntity<?> updateModule(@PathVariable Integer id,
                                               @RequestBody ModuleRequest request) {
        updateModule(id, request);
        return ResponseEntity.ok().body(ApiResponse.builder().message("Module updated successfully").build());
    }

    // -------------------------------
    // Xóa module
    // -------------------------------
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteModule(@PathVariable Integer id) {
        moduleService.deleteModule(id);
        return ResponseEntity.ok().body(ApiResponse.builder().message("Delete course successfully"));
    }




}
