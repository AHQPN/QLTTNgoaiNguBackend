package org.example.qlttngoaingu.dto.request;

import lombok.Data;
import org.example.qlttngoaingu.service.enums.ActionEnum;

@Data
public class ContentUpdateRequest {

    private Integer Id;
    private String contentName;
    private ActionEnum action;
}
