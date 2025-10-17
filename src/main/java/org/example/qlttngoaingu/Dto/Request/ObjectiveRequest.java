package org.example.qlttngoaingu.Dto.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ObjectiveRequest {
    @NotBlank(message = "FIELD_NOT_BLANK")
    private String objectiveName;
}