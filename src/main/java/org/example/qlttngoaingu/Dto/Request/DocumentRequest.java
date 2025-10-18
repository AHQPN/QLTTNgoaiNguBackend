package org.example.qlttngoaingu.Dto.Request;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.example.qlttngoaingu.Service.enums.ActionEnum;

@Data
public class DocumentRequest {
    private Integer id;

    private String fileName;

    private String link;

    private String description;

    private String image;

    private ActionEnum action;
}
