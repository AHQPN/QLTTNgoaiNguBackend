package org.example.qlttngoaingu.dto.request;

import java.util.List;

import lombok.Data;

@Data
public class CartPreviewRequest {
    private List<Integer> courseClassIds;
}
