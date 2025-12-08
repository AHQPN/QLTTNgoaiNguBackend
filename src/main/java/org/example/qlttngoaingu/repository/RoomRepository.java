package org.example.qlttngoaingu.repository;

import java.util.Optional;

import org.example.qlttngoaingu.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Integer> {
    Optional<Room> findByRoomName(String roomName);
}
