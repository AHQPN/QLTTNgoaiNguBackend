package org.example.qlttngoaingu.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.example.qlttngoaingu.dto.request.RoomRequest;
import org.example.qlttngoaingu.dto.response.AvailableRoomResponse;
import org.example.qlttngoaingu.entity.Course;
import org.example.qlttngoaingu.entity.CourseClass;
import org.example.qlttngoaingu.entity.Room;
import org.example.qlttngoaingu.repository.CourseClassRepository;
import org.example.qlttngoaingu.repository.RoomRepository;
import org.example.qlttngoaingu.service.enums.ClassStatusEnum;
import org.example.qlttngoaingu.service.enums.SchedulePattern;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoomService {
    private final RoomRepository roomRepository;
    private final CourseClassRepository classRepository;

    public List<AvailableRoomResponse> getAvailableRooms(
            String schedulePattern,
            LocalTime startTime,
            Integer durationMinutes,
            LocalDate startDate) {
        // Get all rooms that are available (status = "Sẵn sàng")
        List<Room> allRooms = roomRepository.findAll().stream()
                .filter(room -> "Sẵn sàng".equals(room.getStatus()))
                .toList();

        // Parse pattern
        SchedulePattern pattern;
        try {
            pattern = SchedulePattern.fromPattern(schedulePattern);
        } catch (Exception e) {
            return Collections.emptyList();
        }

        LocalTime endTime = startTime.plusMinutes(durationMinutes);

        // Check each room
        List<AvailableRoomResponse> availableRooms = new ArrayList<>();

        for (Room room : allRooms) {
            boolean isAvailable = checkRoomAvailability(
                    room.getRoomId(),
                    pattern,
                    startTime,
                    endTime,
                    startDate
            );

            if (isAvailable) {
                AvailableRoomResponse response = new AvailableRoomResponse();
                response.setRoomId(room.getRoomId());
                response.setRoomName(room.getRoomName());
                response.setCapacity(room.getCapacity());
                response.setStatus(room.getStatus());

                availableRooms.add(response);
            }
        }

        return availableRooms;
    }

    private boolean checkRoomAvailability(
            Integer roomId,
            SchedulePattern pattern,
            LocalTime startTime,
            LocalTime endTime,
            LocalDate startDate
            ) {

        List<CourseClass> classes = classRepository.findByRoom_RoomIdAndStatus(roomId, ClassStatusEnum.InProgress.name());

        for (CourseClass courseClass : classes) {

            LocalDate classEndDate = calculateClassEndDate(courseClass);
            if (classEndDate.isBefore(startDate)) continue; // lớp đã kết thúc

            SchedulePattern classPattern = SchedulePattern.fromPattern(courseClass.getSchedule());

            Set<DayOfWeek> commonDays = new HashSet<>(pattern.getDaysOfWeek());
            commonDays.retainAll(classPattern.getDaysOfWeek());

            if (!commonDays.isEmpty() && courseClass.getStartTime() != null) {
                LocalTime classEndTime = courseClass.getStartTime()
                        .plusMinutes(courseClass.getMinutesPerSession());

                boolean overlap = !(endTime.isBefore(courseClass.getStartTime()) ||
                        startTime.isAfter(classEndTime));

                if (overlap) return false; // conflict
            }
        }

        return true; // không conflict
    }



    private LocalDate calculateClassEndDate(CourseClass cls) {
        Course course = cls.getCourse();
        SchedulePattern pattern = SchedulePattern.fromPattern(cls.getSchedule());

        // Tổng phút học
        BigDecimal totalMinutes = BigDecimal.valueOf(course.getStudyHours())
                .multiply(BigDecimal.valueOf(60));

        // Số buổi cần học
        int totalSessions = totalMinutes
                .divide(BigDecimal.valueOf(cls.getMinutesPerSession()), 0, RoundingMode.CEILING)
                .intValue();

        LocalDate date = cls.getStartDate();
        int created = 0;

        // Lặp qua từng ngày, tạo buổi theo pattern
        while (created < totalSessions) {
            if (pattern.getDaysOfWeek().contains(date.getDayOfWeek())) {
                created++;
            }
            date = date.plusDays(1);
        }

        return date.minusDays(1);
    }

    public List<AvailableRoomResponse> getAllRooms() {
        return roomRepository.findAll()
                .stream()
                .map(r -> {
                    AvailableRoomResponse dto = new AvailableRoomResponse();
                    dto.setRoomId(r.getRoomId());
                    dto.setRoomName(r.getRoomName());
                    dto.setCapacity(r.getCapacity());
                    return dto;
                })
                .toList();
    }


    public AvailableRoomResponse getRoomById(Integer id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room Not Found"));
        
        AvailableRoomResponse response = new AvailableRoomResponse();
        response.setRoomId(room.getRoomId());
        response.setRoomName(room.getRoomName());
        response.setCapacity(room.getCapacity());
        response.setStatus(room.getStatus());
        return response;
    }

    public AvailableRoomResponse createRoom(RoomRequest request) {
        Room room = new Room();
        room.setRoomName(request.getRoomName());
        room.setCapacity(request.getCapacity());
        room.setStatus("Sẵn sàng"); // Mặc định phòng mới là "Sẵn sàng"
        Room saved = roomRepository.save(room);
        
        AvailableRoomResponse response = new AvailableRoomResponse();
        response.setRoomId(saved.getRoomId());
        response.setRoomName(saved.getRoomName());
        response.setCapacity(saved.getCapacity());
        response.setStatus(saved.getStatus());
        return response;
    }

    public AvailableRoomResponse updateRoom(Integer id, RoomRequest request) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room Not Found"));

        room.setRoomName(request.getRoomName());
        room.setCapacity(request.getCapacity());

        Room updated = roomRepository.save(room);
        
        AvailableRoomResponse response = new AvailableRoomResponse();
        response.setRoomId(updated.getRoomId());
        response.setRoomName(updated.getRoomName());
        response.setCapacity(updated.getCapacity());
        response.setStatus(updated.getStatus());
        return response;
    }

    public void deleteRoom(Integer id) {
        if (!roomRepository.existsById(id)) {
            throw new RuntimeException("Room Not Found");
        }
        roomRepository.deleteById(id);
    }

}