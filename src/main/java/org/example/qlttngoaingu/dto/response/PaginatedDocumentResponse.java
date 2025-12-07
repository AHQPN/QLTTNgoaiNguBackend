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
public class PaginatedDocumentResponse {
    
    private List<DocumentItemResponse> documents;
    private int currentPage;
    private int totalPages;
    private long totalItems;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DocumentItemResponse {
        private Integer documentId;
        private String fileName;
        private String link;
        private String description;
        private String image;
        private Integer courseId;
        private String courseName;
        private Integer moduleId;
        private String moduleName;
    }
}
