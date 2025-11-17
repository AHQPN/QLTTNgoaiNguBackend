// ========== RESPONSE CLASSES ==========

package org.example.qlttngoaingu.dto.response;

import lombok.Data;
import java.util.List;

@Data
public class ScheduleSuggestionResponse {
    private String status; // "AVAILABLE" hoặc "CONFLICT"
    private String message;
    private AvailabilityResult initialCheck;

    // Nếu available
    private List<ResourceInfo> availableRooms;
    private List<ResourceInfo> availableLecturers;

    // Nếu conflict
    private List<ScheduleAlternative> alternatives;
}