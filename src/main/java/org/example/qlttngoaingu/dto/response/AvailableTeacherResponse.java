package org.example.qlttngoaingu.dto.response;

import lombok.Data;

@Data
public class AvailableTeacherResponse {
    private Integer maGiangVien;
    private String tenGiangVien;
    private Integer soLopDangDay; // Số lớp đang dạy
    private Boolean isRecommended; // Giảng viên ít lớp = recommended
}
