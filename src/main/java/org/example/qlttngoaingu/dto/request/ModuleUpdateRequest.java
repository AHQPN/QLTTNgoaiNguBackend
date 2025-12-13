package org.example.qlttngoaingu.dto.request;

import lombok.Data;

import java.util.List;
@Data
public class ModuleUpdateRequest {
    private List<DocumentUpdateRequest> documents;
    private List<ContentUpdateRequest> contents;
}
