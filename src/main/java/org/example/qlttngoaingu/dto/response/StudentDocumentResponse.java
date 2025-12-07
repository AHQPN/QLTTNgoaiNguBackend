package org.example.qlttngoaingu.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentDocumentResponse {
    
    private Integer courseId;
    private String courseName;
    private List<DocumentInfo> documents;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DocumentInfo {
        private Integer documentId;
        private String fileName;
        private String link;
        private String description;
        private String image;
    }
}
