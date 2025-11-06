package org.example.qlttngoaingu.dto.response;

import java.util.List;

public class ConflictCheckResponse {
    private Boolean hasConflict;
    private List<String> roomConflicts;
    private List<String> teacherConflicts;
    private String message;
}
