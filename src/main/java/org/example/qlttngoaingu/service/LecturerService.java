package org.example.qlttngoaingu.service;

import lombok.RequiredArgsConstructor;
import org.example.qlttngoaingu.dto.request.LecturerCreationRequest;
import org.example.qlttngoaingu.entity.Lecturer;
import org.example.qlttngoaingu.repository.LecturerRepository;
import org.example.qlttngoaingu.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LecturerService {
    private final LecturerRepository lecturerRepository;
    private final UserRepository userRepository;

    @Transactional
    public void addLecturerInfo(LecturerCreationRequest request,Integer userId) {
        Lecturer lecturer = new Lecturer();
        lecturer.setFullName(request.getName());
        lecturer.setDateOfBirth(request.getDateOfBirth());
        lecturer.setImagePath(request.getImageUrl());
        lecturer.setUser(userRepository.findById(userId).get());
        // 2. Lưu vào database
        lecturerRepository.save(lecturer);
    }
}
