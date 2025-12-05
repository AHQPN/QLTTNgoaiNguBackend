package org.example.qlttngoaingu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO cho hoạt động gần đây trên Dashboard
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityResponse {
    
    // ID hoạt động
    private String id;
    
    // Loại hoạt động: registration, payment, class_end, profile_update, other
    private String type;
    
    // Tiêu đề hoạt động
    private String title;
    
    // Mô tả chi tiết
    private String description;
    
    // Thời gian xảy ra
    private LocalDateTime timestamp;
    
    // ID người dùng liên quan
    private String userId;
    
    // Tên người dùng liên quan
    private String userName;
}
