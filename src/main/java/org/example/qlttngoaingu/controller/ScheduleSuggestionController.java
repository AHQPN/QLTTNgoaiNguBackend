package org.example.qlttngoaingu.controller;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.example.qlttngoaingu.dto.request.ScheduleCheckRequest;
import org.example.qlttngoaingu.dto.response.ApiResponse;
import org.example.qlttngoaingu.dto.response.ScheduleAlternative;
import org.example.qlttngoaingu.dto.response.ScheduleSuggestionResponse;
import org.example.qlttngoaingu.service.SmartScheduleSuggestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/schedules")
@RequiredArgsConstructor
public class ScheduleSuggestionController {

    private final SmartScheduleSuggestionService suggestionService;

    /**
     * API chính: Kiểm tra và gợi ý lịch học
     *
     * POST /api/schedules/check-and-suggest
     *
     * Request Body:
     * {
     *   "courseId": 1,
     *   "startDate": "2025-01-15",
     *   "startTime": "08:00",
     *   "durationMinutes": 90,
     *   "schedulePattern": "2-4-6",
     *   "preferredRoomId": 5,        // Optional
     *   "preferredLecturerId": 3      // Optional
     * }
     *
     * Response khi AVAILABLE:
     * {
     *   "status": "AVAILABLE",
     *   "message": "Lịch học khả dụng! Vui lòng chọn phòng và giảng viên.",
     *   "initialCheck": {
     *     "hasAvailableRooms": true,
     *     "hasAvailableLecturers": true,
     *     "availableRoomCount": 5,
     *     "availableLecturerCount": 3
     *   },
     *   "availableRooms": [
     *     {"id": 1, "name": "Phòng A1"},
     *     {"id": 2, "name": "Phòng B2"},
     *     ...
     *   ],
     *   "availableLecturers": [
     *     {"id": 1, "name": "Nguyễn Văn A"},
     *     {"id": 2, "name": "Trần Thị B"},
     *     ...
     *   ]
     * }
     *
     * Response khi CONFLICT:
     * {
     *   "status": "CONFLICT",
     *   "message": "Lịch học bị xung đột. Dưới đây là các gợi ý thay thế:",
     *   "initialCheck": {
     *     "hasAvailableRooms": false,
     *     "hasAvailableLecturers": true,
     *     "roomConflicts": [
     *       {
     *         "type": "ROOM_CONFLICT",
     *         "description": "Phòng 'A1' bị trùng với lớp 'Lớp TOEIC 550' (TOEIC Intermediate) vào T2, T4, T6 từ 08:00-09:30"
     *       }
     *     ]
     *   },
     *   "alternatives": [
     *     {
     *       "type": "ALTERNATIVE_TIME",
     *       "reason": "Đổi giờ từ 08:00 sang 13:00",
     *       "priority": 115,
     *       "startDate": "2025-01-15",
     *       "startTime": "13:00",
     *       "endTime": "14:30",
     *       "schedulePattern": "2-4-6",
     *       "availableRooms": [
     *         {"id": 1, "name": "Phòng A1"},
     *         {"id": 3, "name": "Phòng C3"}
     *       ],
     *       "availableLecturers": [
     *         {"id": 2, "name": "Trần Thị B"},
     *         {"id": 5, "name": "Lê Văn E"}
     *       ]
     *     },
     *     {
     *       "type": "ALTERNATIVE_ROOM",
     *       "reason": "Đổi phòng sang Phòng B2",
     *       "priority": 95,
     *       "startDate": "2025-01-15",
     *       "startTime": "08:00",
     *       "endTime": "09:30",
     *       "schedulePattern": "2-4-6",
     *       "availableRooms": [
     *         {"id": 2, "name": "Phòng B2"}
     *       ],
     *       "availableLecturers": [
     *         {"id": 2, "name": "Trần Thị B"},
     *         {"id": 3, "name": "Nguyễn Văn C"}
     *       ]
     *     },
     *     ...
     *   ]
     * }
     */
    @PostMapping("/check-and-suggest")
    public ResponseEntity<?> checkAndSuggest(
            @RequestBody ScheduleCheckRequest request) {

        ScheduleSuggestionResponse response = suggestionService.checkAndSuggest(request);
        return ResponseEntity.ok().body(ApiResponse.builder().data(response).build());
    }

    /**
     * API đơn giản hơn: Chỉ check availability
     *
     * POST /api/schedules/quick-check
     */
    @PostMapping("/quick-check")
    public ResponseEntity<?> quickCheck(
            @RequestBody ScheduleCheckRequest request) {

        ScheduleSuggestionResponse fullResponse = suggestionService.checkAndSuggest(request);

        QuickCheckResponse quick = new QuickCheckResponse();
        quick.setAvailable("AVAILABLE".equals(fullResponse.getStatus()));
        quick.setMessage(fullResponse.getMessage());
        quick.setAvailableRoomCount(fullResponse.getInitialCheck().getAvailableRoomCount());
        quick.setAvailableLecturerCount(fullResponse.getInitialCheck().getAvailableLecturerCount());

        if (!quick.isAvailable() && fullResponse.getAlternatives() != null) {
            quick.setBestAlternative(fullResponse.getAlternatives().isEmpty()
                    ? null
                    : fullResponse.getAlternatives().get(0));
        }

        return ResponseEntity.ok().body(ApiResponse.builder().data(quick).build());
    }
}


@Data
class QuickCheckResponse {
    private boolean available;
    private String message;
    private int availableRoomCount;
    private int availableLecturerCount;
    private ScheduleAlternative bestAlternative;
}


/**
 * ======================
 * USAGE EXAMPLES (Frontend)
 * ======================
 */

/*

// 1. User nhập thông tin ban đầu
const checkRequest = {
  courseId: 1,
  startDate: "2025-01-15",
  startTime: "08:00",
  durationMinutes: 90,
  schedulePattern: "2-4-6"
};

// Gọi API check
const response = await fetch('/api/schedules/check-and-suggest', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify(checkRequest)
});

const result = await response.json();

if (result.status === "AVAILABLE") {
  // Hiển thị danh sách phòng và GV để user chọn
  showRoomList(result.availableRooms);
  showLecturerList(result.availableLecturers);

} else {
  // Hiển thị các gợi ý thay thế
  showAlternatives(result.alternatives);

  // Ví dụ alternatives:
  // [
  //   { type: "ALTERNATIVE_TIME", reason: "Đổi giờ từ 08:00 sang 13:00", priority: 115, ... },
  //   { type: "ALTERNATIVE_ROOM", reason: "Đổi phòng sang Phòng B2", priority: 95, ... },
  //   { type: "ALTERNATIVE_START_DATE", reason: "Bắt đầu từ 2025-01-17", priority: 85, ... }
  // ]
}

// 2. User chọn 1 alternative và apply
function applyAlternative(alternative) {
  // Cập nhật form với thông tin từ alternative
  formData.startDate = alternative.startDate;
  formData.startTime = alternative.startTime;
  formData.schedulePattern = alternative.schedulePattern;

  // Hiển thị danh sách resources
  showRoomList(alternative.availableRooms);
  showLecturerList(alternative.availableLecturers);
}

// 3. User chọn phòng/GV cụ thể → Check lại
checkRequest.preferredRoomId = selectedRoomId;
checkRequest.preferredLecturerId = selectedLecturerId;

const finalCheck = await fetch('/api/schedules/check-and-suggest', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify(checkRequest)
});

const finalResult = await finalCheck.json();

if (finalResult.status === "AVAILABLE") {
  // Cho phép tạo lớp
  enableCreateButton();
} else {
  // Vẫn còn conflict → Hiển thị lại suggestions
  showConflicts(finalResult.initialCheck.roomConflicts);
  showConflicts(finalResult.initialCheck.lecturerConflicts);
  showAlternatives(finalResult.alternatives);
}

*/