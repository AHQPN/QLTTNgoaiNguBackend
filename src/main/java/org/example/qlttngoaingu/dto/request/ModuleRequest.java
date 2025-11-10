package org.example.qlttngoaingu.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ModuleRequest {
    @NotBlank(message = "FIELD_NOT_BLANK")
    private String moduleName;
    private Integer duration;
    private List<DocumentRequest> documents;
    private List<ContentRequest> contents;
}