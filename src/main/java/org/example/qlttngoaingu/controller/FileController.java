package org.example.qlttngoaingu.controller;

import java.util.Map;

import org.example.qlttngoaingu.dto.response.ApiResponse;
import org.example.qlttngoaingu.service.FileService;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    /**
     * Upload file ảnh
     */
    @PostMapping
    public ResponseEntity<ApiResponse> uploadFile(@RequestParam("file") MultipartFile file) {
        String fileUrl = fileService.uploadFile(file);
        Map<String, String> response = Map.of("fileUrl", fileUrl);
        return ResponseEntity.ok().body(ApiResponse.builder().data(response).build());
    }

    /**
     * Lấy file theo tên
     */
    @GetMapping("/{fileName:.+}")
    public ResponseEntity<Resource> getFile(@PathVariable String fileName) {
        Resource resource = fileService.getFile(fileName);
        String contentType = fileService.getContentType(fileName);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }

    /**
     * Xóa file
     */
    @DeleteMapping("/{fileName:.+}")
    public ResponseEntity<ApiResponse> deleteFile(@PathVariable String fileName) {
        fileService.deleteFile(fileName);
        return ResponseEntity.ok(ApiResponse.builder()
                .message("File deleted successfully")
                .build());
    }
}
