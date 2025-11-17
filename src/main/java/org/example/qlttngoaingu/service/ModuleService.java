package org.example.qlttngoaingu.service;

import lombok.AllArgsConstructor;
import org.example.qlttngoaingu.dto.request.*;
import org.example.qlttngoaingu.entity.*;
import org.example.qlttngoaingu.entity.Module;
import org.example.qlttngoaingu.repository.*;
import org.example.qlttngoaingu.service.enums.ActionEnum;
import org.example.qlttngoaingu.exception.AppException;
import org.example.qlttngoaingu.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class ModuleService {

    private final ModuleRepository moduleRepository;
    private final ContentRepository contentRepository;
    private final DocumentRepository documentRepository;
    private final CourseSkillRepository courseSkillRepository;

    /**
     * Thêm module mới vào CourseSkill
     * @param courseSkillId ID của CourseSkill (quan hệ giữa Course và Skill)
     * @param request Thông tin module cần tạo
     */
    @Transactional
    public Module addModule(Integer courseSkillId, ModuleRequest request) {
        CourseSkill courseSkill = courseSkillRepository.findById(courseSkillId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_SKILL_NOT_FOUND));

        // Tạo module
        Module module = new Module();
        module.setModuleName(request.getModuleName());
        module.setDuration(request.getDuration());
        module.setCourseSkill(courseSkill);
        moduleRepository.save(module);

        // Thêm Documents nếu có
        if (request.getDocuments() != null && !request.getDocuments().isEmpty()) {
            List<Document> docs = request.getDocuments().stream().map(docReq -> {
                Document doc = new Document();
                doc.setFileName(docReq.getFileName());
                doc.setLink(docReq.getLink());
                doc.setImage(docReq.getImage());
                doc.setDescription(docReq.getDescription());
                doc.setModule(module);
                return doc;
            }).collect(Collectors.toList());
            documentRepository.saveAll(docs);
        }

        // Thêm Contents nếu có
        if (request.getContents() != null && !request.getContents().isEmpty()) {
            List<Content> contents = request.getContents().stream().map(contentReq -> {
                Content content = new Content();
                content.setContentName(contentReq.getContentName());
                content.setModule(module);
                return content;
            }).collect(Collectors.toList());
            contentRepository.saveAll(contents);
        }

        return module;
    }

    /**
     * Cập nhật thông tin cơ bản của module (tên, duration)
     */
    @Transactional
    public Module updateModuleBasicInfo(Integer moduleId, String moduleName, Integer duration) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new AppException(ErrorCode.MODULE_NOT_FOUND));

        if (moduleName != null) {
            module.setModuleName(moduleName);
        }
        if (duration != null) {
            module.setDuration(duration);
        }

        return moduleRepository.save(module);
    }

    /**
     * Cập nhật chi tiết module (tên, documents, contents)
     * Sử dụng cho update toàn diện
     */
    @Transactional
    public void updateModuleDetail(Integer moduleId, ModuleUpdateRequest request) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new AppException(ErrorCode.MODULE_NOT_FOUND));

        // Update tên module
        if (request.getModuleName() != null) {
            module.setModuleName(request.getModuleName());
            moduleRepository.save(module);
        }

        // Xử lý Documents
        if (request.getDocuments() != null) {
            for (DocumentUpdateRequest docReq : request.getDocuments()) {
                switch (docReq.getAction()) {
                    case CREATE:
                        Document newDoc = new Document();
                        newDoc.setFileName(docReq.getFileName());
                        newDoc.setLink(docReq.getLink());
                        newDoc.setDescription(docReq.getDescription());
                        newDoc.setImage(docReq.getImage());
                        newDoc.setModule(module);
                        documentRepository.save(newDoc);
                        break;

                    case UPDATE:
                        Document existingDoc = documentRepository.findById(docReq.getId())
                                .orElseThrow(() -> new AppException(ErrorCode.DOCUMENT_NOT_FOUND));
                        existingDoc.setFileName(docReq.getFileName());
                        existingDoc.setLink(docReq.getLink());
                        existingDoc.setDescription(docReq.getDescription());
                        existingDoc.setImage(docReq.getImage());
                        documentRepository.save(existingDoc);
                        break;

                    case DELETE:
                        documentRepository.deleteById(docReq.getId());
                        break;
                }
            }
        }

        // Xử lý Contents
        if (request.getContents() != null) {
            for (ContentUpdateRequest contReq : request.getContents()) {
                switch (contReq.getAction()) {
                    case CREATE:
                        Content newContent = new Content();
                        newContent.setContentName(contReq.getContentName());
                        newContent.setModule(module);
                        contentRepository.save(newContent);
                        break;

                    case UPDATE:
                        Content existingContent = contentRepository.findById(contReq.getId())
                                .orElseThrow(() -> new AppException(ErrorCode.CONTENT_NOT_FOUND));
                        existingContent.setContentName(contReq.getContentName());
                        contentRepository.save(existingContent);
                        break;

                    case DELETE:
                        contentRepository.deleteById(contReq.getId());
                        break;
                }
            }
        }
    }

    /**
     * Xóa module
     */
    @Transactional
    public void deleteModule(Integer moduleId) {
        if (!moduleRepository.existsById(moduleId)) {
            throw new AppException(ErrorCode.MODULE_NOT_FOUND);
        }
        moduleRepository.deleteById(moduleId);
    }

    /**
     * Lấy tất cả modules của một CourseSkill
     */
    public List<Module> getModulesByCourseSkill(Integer courseSkillId) {
        CourseSkill courseSkill = courseSkillRepository.findById(courseSkillId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_SKILL_NOT_FOUND));
        return courseSkill.getModules();
    }

    /**
     * Lấy chi tiết một module
     */
    public Module getModuleById(Integer moduleId) {
        return moduleRepository.findById(moduleId)
                .orElseThrow(() -> new AppException(ErrorCode.MODULE_NOT_FOUND));
    }

    /**
     * Lấy tất cả modules của course (từ tất cả các skills)
     */
    public List<Module> getModulesByCourseId(Integer courseId) {
        List<CourseSkill> courseSkills = courseSkillRepository.findByCourse_CourseId(courseId);
        return courseSkills.stream()
                .flatMap(cs -> cs.getModules().stream())
                .collect(Collectors.toList());
    }
}