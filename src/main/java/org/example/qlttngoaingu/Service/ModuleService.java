package org.example.qlttngoaingu.Service;

import lombok.AllArgsConstructor;
import org.example.qlttngoaingu.Dto.Request.ContentRequest;
import org.example.qlttngoaingu.Dto.Request.DocumentRequest;
import org.example.qlttngoaingu.Dto.Request.ModuleUpdateRequest;
import org.example.qlttngoaingu.Repository.ContentRepository;
import org.example.qlttngoaingu.Repository.DocumentRepository;
import org.example.qlttngoaingu.Service.enums.ActionEnum;
import org.example.qlttngoaingu.entity.Content;
import org.example.qlttngoaingu.entity.Document;
import org.example.qlttngoaingu.entity.Module;
import org.example.qlttngoaingu.Dto.Request.ModuleRequest;
import org.example.qlttngoaingu.Repository.CourseRepository;
import org.example.qlttngoaingu.Repository.ModuleRepository;
import org.example.qlttngoaingu.entity.Course;
import org.example.qlttngoaingu.exception.AppException;
import org.example.qlttngoaingu.exception.ErrorCode;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
public class ModuleService {
    private final CourseRepository courseRepository;
    private final ModuleRepository moduleRepository;
    private final ContentRepository  contentRepository;
    private final DocumentRepository documentRepository;
    // Thêm module
    @Transactional
    public void addModule(Integer courseId, ModuleRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        Module module = new Module();
        module.setModuleName(request.getModuleName());
        module.setDuration(request.getDuration());
        module.setCourse(course);

        moduleRepository.save(module);
    }

    // Cập nhật module
    @Transactional
    public void updateModule(Integer courseId, Integer moduleId, ModuleRequest request) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        if (!module.getCourse().getCourseId().equals(courseId)) {
            throw new AppException(ErrorCode.MISS_MATCH_COURSE);
        }

        if (request.getModuleName() != null) {
            module.setModuleName(request.getModuleName());
        }
        if (request.getDuration() != null) {
            module.setDuration(request.getDuration());
        }

        moduleRepository.save(module);
    }
    private Module getModule(Integer moduleId) {
        return moduleRepository.findById(moduleId).orElseThrow(() -> new AppException(ErrorCode.MODULE_NOT_FOUND));
    }

    // Xóa module
    @Transactional
    public void deleteModule(Integer courseId, Integer moduleId) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        if (!module.getCourse().getCourseId().equals(courseId)) {
            throw new AppException(ErrorCode.MISS_MATCH_COURSE);
        }

        moduleRepository.delete(module);
    }
    private void updateModuleDetail(Integer moduleId, ModuleRequest request) {

    }
    @Transactional
    public void updateModuleDetail(Integer moduleId, ModuleUpdateRequest request) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module not found"));

        module.setModuleName(request.getModuleName());
        module.setDuration(request.getDuration());

        // ---- 1️⃣ Xử lý TÀI LIỆU ----
        if (request.getDocuments() != null) {
            for (DocumentRequest tl : request.getDocuments()) {
                ActionEnum action = tl.getAction();
                switch (action.name()) {
                    case "ADD":
                        Document newTL = new Document();
                        newTL.setFileName(tl.getFileName());
                        newTL.setLink(tl.getLink());
                        newTL.setDescription(tl.getDescription());
                        newTL.setImage(tl.getImage());
                        newTL.setModule(module);
                        documentRepository.save(newTL);
                        break;
                    case "UPDATE":
                        Document existing = documentRepository.findById(tl.getId())
                                .orElseThrow(() -> new RuntimeException("File not found"));
                        existing.setFileName(tl.getFileName());
                        existing.setLink(tl.getLink());
                        existing.setDescription(tl.getDescription());
                        existing.setImage(tl.getImage());
                        documentRepository.save(existing);
                        break;
                    case "DELETE":
                        documentRepository.deleteById(tl.getId());
                        break;
                }
            }
        }

        // ---- 2️⃣ Xử lý NỘI DUNG ----
        if (request.getContents() != null) {
            for (ContentRequest nd : request.getContents()) {
                ActionEnum action = nd.getAction();

                switch (action.name()) {
                    case "ADD":
                        Content newND = new Content();
                        newND.setContentName(nd.getContentName());
                        newND.setModule(module);
                        contentRepository.save(newND);
                        break;
                    case "UPDATE":
                        Content existingND = contentRepository.findById(nd.getId())
                                .orElseThrow(() -> new RuntimeException("Nội dung không tồn tại"));
                        existingND.setContentName(nd.getContentName());
                        contentRepository.save(existingND);
                        break;
                    case "DELETE":
                        contentRepository.deleteById(nd.getId());
                        break;
                }
            }
        }
    }

}
