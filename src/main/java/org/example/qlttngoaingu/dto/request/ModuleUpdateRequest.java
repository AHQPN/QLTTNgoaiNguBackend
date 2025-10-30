package org.example.qlttngoaingu.dto.request;

import lombok.Data;

import java.util.List;
@Data
public class ModuleUpdateRequest {
    private String moduleName;

    private List<DocumentUpdateRequest> documents;
    private List<ContentUpdateRequest> contents;
}
