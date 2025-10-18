package org.example.qlttngoaingu.Dto.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.example.qlttngoaingu.Service.enums.ActionEnum;
@Data
public class ContentRequest {
    private Integer Id;
    private String contentName;
    private ActionEnum action;
}
