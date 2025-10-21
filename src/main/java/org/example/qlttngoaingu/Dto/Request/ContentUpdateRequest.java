package org.example.qlttngoaingu.Dto.Request;

import lombok.Data;
import org.example.qlttngoaingu.Service.enums.ActionEnum;

@Data
public class ContentUpdateRequest {

    private Integer Id;
    private String contentName;
    private ActionEnum action;
}
