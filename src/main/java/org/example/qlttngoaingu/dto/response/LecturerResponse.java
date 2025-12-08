package org.example.qlttngoaingu.dto.response;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LecturerResponse {
    private Integer lecturerId;
    private String fullName;
    private LocalDate dateOfBirth;
    private String imagePath;
    private Integer userId;
    private String username;
    private String email;
    private String phoneNumber;
    private Integer totalClasses;
    private Integer activeClasses;
    private List<CertificateInfo> certificates;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CertificateInfo {
        private Integer certificateId;
        private String certificateName;
        private String level;
    }
}
