package org.example.qlttngoaingu.repository;

import org.example.qlttngoaingu.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository<User,Integer> {
    Boolean existsByPhoneNumber(String phoneNumber);
    Boolean existsByEmail(String email);

    Optional<User> findByPhoneNumberOrEmail(String phoneNumber, String email);

    Optional<User> findByUserId(Integer userId);

    Optional<User> getUserByUserId(Integer id);
}
