package org.example.qlttngoaingu.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.example.qlttngoaingu.exception.AppException;
import org.example.qlttngoaingu.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FileService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final String[] ALLOWED_IMAGE_TYPES = {
        "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    };

    /**
     * Upload file ảnh
     * @param file MultipartFile cần upload
     * @return URL của file đã upload
     */
    public String uploadFile(MultipartFile file) {
        try {
            // Validate file không null và không rỗng
            validateFile(file);

            // Tạo thư mục nếu chưa tồn tại
            Path dirPath = Paths.get(uploadDir);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            // Tạo tên file unique
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = dirPath.resolve(fileName);

            // Lưu file
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            log.info("File uploaded successfully: {}", fileName);
            return fileName;

        } catch (AppException e) {
            throw e;
        } catch (IOException e) {
            log.error("Failed to upload file", e);
            throw new AppException(ErrorCode.UNCATEGORIZED);
        }
    }

    /**
     * Lấy file resource
     * @param fileName tên file
     * @return Resource của file
     */
    public Resource getFile(String fileName) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(fileName).normalize();
            
            if (!Files.exists(filePath)) {
                throw new AppException(ErrorCode.FILE_NOT_FOUND);
            }

            Resource resource = new UrlResource(filePath.toUri());
            
            if (!resource.exists() || !resource.isReadable()) {
                throw new AppException(ErrorCode.FILE_NOT_FOUND);
            }

            return resource;

        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to get file: {}", fileName, e);
            throw new AppException(ErrorCode.UNCATEGORIZED);
        }
    }

    /**
     * Lấy content type của file
     * @param fileName tên file
     * @return content type
     */
    public String getContentType(String fileName) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(fileName).normalize();
            String contentType = Files.probeContentType(filePath);
            return contentType != null ? contentType : "application/octet-stream";
        } catch (IOException e) {
            return "application/octet-stream";
        }
    }

    /**
     * Xóa file
     * @param fileName tên file cần xóa
     */
    public void deleteFile(String fileName) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(fileName).normalize();
            
            if (!Files.exists(filePath)) {
                throw new AppException(ErrorCode.FILE_NOT_FOUND);
            }

            Files.delete(filePath);
            log.info("File deleted successfully: {}", fileName);

        } catch (AppException e) {
            throw e;
        } catch (IOException e) {
            log.error("Failed to delete file: {}", fileName, e);
            throw new AppException(ErrorCode.UNCATEGORIZED);
        }
    }

    /**
     * Validate file upload
     */
    private void validateFile(MultipartFile file) {
        // Kiểm tra file không null và không rỗng
        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.FILE_EMPTY);
        }

        // Kiểm tra kích thước file (max 5MB)
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new AppException(ErrorCode.FILE_TOO_LARGE);
        }

        // Kiểm tra loại file (chỉ cho phép ảnh)
        String contentType = file.getContentType();
        if (contentType == null || !isImageFile(contentType)) {
            throw new AppException(ErrorCode.INVALID_FILE_TYPE);
        }
    }

    /**
     * Kiểm tra file có phải là ảnh hợp lệ không
     */
    private boolean isImageFile(String contentType) {
        for (String allowedType : ALLOWED_IMAGE_TYPES) {
            if (allowedType.equalsIgnoreCase(contentType)) {
                return true;
            }
        }
        return false;
    }
}
