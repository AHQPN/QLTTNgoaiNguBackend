package org.example.qlttngoaingu.Dto.Request;

import lombok.Data;
import org.example.qlttngoaingu.entity.Content;
import org.example.qlttngoaingu.entity.Document;

import java.util.List;
@Data
public class ModuleUpdateRequest {
    private String moduleName;
    private Integer duration;
    private List<DocumentUpdateRequest> documents;
    private List<ContentUpdateRequest> contents;
}
