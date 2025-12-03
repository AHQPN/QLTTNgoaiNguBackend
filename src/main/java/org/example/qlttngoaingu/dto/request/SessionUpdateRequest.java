package org.example.qlttngoaingu.dto.request;

import lombok.Data;

@Data
public class SessionUpdateRequest {
    private String status; // "Completed", "Canceled", "NotCompleted"
    private String note;
}
