package org.example.qlttngoaingu.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ModuleRequest {
    @NotBlank(message = "FIELD_NOT_BLANK")
    private String moduleName;

    private Integer duration;

    // Thêm skillId để biết module này thuộc skill nào
    @NotNull(message = "SKILL_ID_NOT_NULL")
    private Integer skillId;

    @Valid
    private List<DocumentRequest> documents;

    @Valid
    private List<ContentRequest> contents;
}