package org.example.qlttngoaingu.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.example.qlttngoaingu.service.enums.ActionEnum;

import java.util.List;

@Data
class ModuleDetailUpdateRequest {
    private Integer id; // null nếu là tạo mới

    private String moduleName;

    private Integer duration;

    @NotNull
    private ActionEnum action; // CREATE, UPDATE, DELETE

    @Valid
    private List<DocumentUpdateRequest> documents;

    @Valid
    private List<ContentUpdateRequest> contents;
}