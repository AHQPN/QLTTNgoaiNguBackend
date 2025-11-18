package org.example.qlttngoaingu.dto.response;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AvailabilityResult {
    private boolean hasAvailableRooms;
    private boolean hasAvailableLecturers;
    private int availableRoomCount;
    private int availableLecturerCount;

    // Chi tiết xung đột (nếu user chọn phòng/GV cụ thể)
    private List<ConflictInfo> roomConflicts = new ArrayList<ConflictInfo>();
    private List<ConflictInfo> lecturerConflicts = new ArrayList<ConflictInfo>();

    public boolean isFullyAvailable() {
        return hasAvailableRooms && hasAvailableLecturers;
    }
}
