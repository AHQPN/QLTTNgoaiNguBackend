package org.example.qlttngoaingu.Dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ModuleRequest {
    @NotBlank(message = "FIELD_NOT_BLANK")
    private String moduleName;
    @NotNull(message = "FIELD_NOT_BLANK")
    @Positive(message = "")
    private Integer duration;
}