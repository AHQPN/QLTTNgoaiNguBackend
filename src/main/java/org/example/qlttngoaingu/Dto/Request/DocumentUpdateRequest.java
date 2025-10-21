package org.example.qlttngoaingu.Dto.Request;

import lombok.Data;
import org.example.qlttngoaingu.Service.enums.ActionEnum;
@Data
public class DocumentUpdateRequest {
    private Integer id;

    private String fileName;

    private String link;

    private String description;

    private String image;

    private ActionEnum action;
}
