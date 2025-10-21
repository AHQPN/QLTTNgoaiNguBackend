package org.example.qlttngoaingu.Service;

import lombok.AllArgsConstructor;
import org.example.qlttngoaingu.Dto.Request.*;
import org.example.qlttngoaingu.Repository.ContentRepository;
import org.example.qlttngoaingu.Repository.DocumentRepository;
import org.example.qlttngoaingu.Service.enums.ActionEnum;
import org.example.qlttngoaingu.entity.Content;
import org.example.qlttngoaingu.entity.Document;
import org.example.qlttngoaingu.entity.Module;
import org.example.qlttngoaingu.Repository.CourseRepository;
import org.example.qlttngoaingu.Repository.ModuleRepository;
import org.example.qlttngoaingu.entity.Course;
import org.example.qlttngoaingu.exception.AppException;
import org.example.qlttngoaingu.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class ModuleService {
    private final CourseRepository courseRepository;
    private final ModuleRepository moduleRepository;
    private final ContentRepository  contentRepository;
    private final DocumentRepository documentRepository;
    // Thêm module
    @Transactional
    public Module addModule(Integer courseId, ModuleRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        Module module = new Module();
        module.setModuleName(request.getModuleName());
        module.setDuration(request.getDuration());
        module.setCourse(course);
        moduleRepository.save(module);


        if (request.getDocuments() != null) {
            List<Document> docs = request.getDocuments().stream().map(docReq -> {
                Document doc = new Document();
                doc.setFileName(docReq.getFileName());
                doc.setModule(module);
                doc.setLink(docReq.getLink());
                doc.setImage(docReq.getImage());
                doc.setDescription(docReq.getDescription());
                return doc;
            }).collect(Collectors.toList());
            documentRepository.saveAll(docs);
        }


        // Thêm locations nếu có
        if (request.getContents() != null) {
            List<Content> contents = request.getContents().stream().map(contentRequest -> {
                Content content = new Content();
                content.setContentName(contentRequest.getContentName());
                content.setModule(module);

                return content;
            }).toList();
            contentRepository.saveAll(contents);
        }

        return module; // trả về module vừa tạo
    }


    // Cập nhật module
    @Transactional
    public void updateModule(Integer moduleId, ModuleRequest request) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));


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
    public void deleteModule(Integer moduleId) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));


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
            for (DocumentUpdateRequest tl : request.getDocuments()) {
                ActionEnum action = tl.getAction();
                switch (action.name()) {

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
            for (ContentUpdateRequest nd : request.getContents()) {
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

    public List<Module> getmodules(Integer courseId) {
        return moduleRepository.findByCourse_CourseId(courseId);

    }

}
