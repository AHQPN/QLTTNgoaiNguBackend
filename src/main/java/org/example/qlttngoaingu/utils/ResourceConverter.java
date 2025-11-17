package org.example.qlttngoaingu.utils;

import org.example.qlttngoaingu.dto.response.ResourceInfo;
import org.example.qlttngoaingu.entity.Lecturer;
import org.example.qlttngoaingu.entity.Room;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class để convert entities sang ResourceInfo DTO
 */
public class ResourceConverter {

    /**
     * Convert List<Room> to List<ResourceInfo>
     */
    public static List<ResourceInfo> fromRooms(List<Room> rooms) {
        if (rooms == null || rooms.isEmpty()) {
            return new ArrayList<>();
        }

        return rooms.stream()
                .map(room -> new ResourceInfo(
                        room.getRoomId(),
                        room.getRoomName()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Convert List<Lecturer> to List<ResourceInfo>
     */
    public static List<ResourceInfo> fromLecturers(List<Lecturer> lecturers) {
        if (lecturers == null || lecturers.isEmpty()) {
            return new ArrayList<>();
        }

        return lecturers.stream()
                .map(lecturer -> new ResourceInfo(
                        lecturer.getLecturerId(),
                        lecturer.getFullName()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Convert single Room to ResourceInfo
     */
    public static ResourceInfo fromRoom(Room room) {
        if (room == null) return null;
        return new ResourceInfo(room.getRoomId(), room.getRoomName());
    }

    /**
     * Convert single Lecturer to ResourceInfo
     */
    public static ResourceInfo fromLecturer(Lecturer lecturer) {
        if (lecturer == null) return null;
        return new ResourceInfo(lecturer.getLecturerId(), lecturer.getFullName());
    }
}