package org.example.qlttngoaingu.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.example.qlttngoaingu.dto.response.StudentDocumentResponse;
import org.example.qlttngoaingu.entity.Document;
import org.example.qlttngoaingu.entity.Student;
import org.example.qlttngoaingu.exception.AppException;
import org.example.qlttngoaingu.exception.ErrorCode;
import org.example.qlttngoaingu.repository.DocumentRepository;
import org.example.qlttngoaingu.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DocumentService {
    
    private final DocumentRepository documentRepository;
    private final StudentRepository studentRepository;
    
    @Transactional(readOnly = true)
    public List<StudentDocumentResponse> getStudentDocuments(Integer userId) {
        // Get student
        Student student = studentRepository.getStudentByAccount_UserId(userId);
        if (student == null) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }
        
        // Get all documents for courses student enrolled in
        List<Document> documents = documentRepository.findDocumentsByStudentId(student.getId());
        
        // Group by Course only
        Map<Integer, List<Document>> groupedByCourse = documents.stream()
                .collect(Collectors.groupingBy(
                        doc -> doc.getModule().getCourseSkill().getCourse().getCourseId(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
        
        // Convert to response DTO
        return groupedByCourse.entrySet().stream()
                .map(courseEntry -> {
                    // Get course info from first document
                    Document firstDoc = courseEntry.getValue().stream()
                            .findFirst()
                            .orElse(null);
                    
                    if (firstDoc == null) return null;
                    
                    var course = firstDoc.getModule().getCourseSkill().getCourse();
                    
                    // Build documents list directly (no module grouping)
                    List<StudentDocumentResponse.DocumentInfo> documentInfos = courseEntry.getValue().stream()
                            .map(doc -> StudentDocumentResponse.DocumentInfo.builder()
                                    .documentId(doc.getDocumentId())
                                    .fileName(doc.getFileName())
                                    .link(doc.getLink())
                                    .description(doc.getDescription())
                                    .image(doc.getImage())
                                    .build())
                            .collect(Collectors.toList());
                    
                    return StudentDocumentResponse.builder()
                            .courseId(course.getCourseId())
                            .courseName(course.getCourseName())
                            .documents(documentInfos)
                            .build();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
