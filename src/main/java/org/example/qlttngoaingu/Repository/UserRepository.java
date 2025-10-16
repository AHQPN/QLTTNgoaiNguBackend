package org.example.qlttngoaingu.Repository;

import org.example.qlttngoaingu.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository<User,Integer> {
    Boolean existsByPhoneNumber(String phoneNumber);
    Boolean existsByEmail(String email);

    Optional<User> findByPhoneNumberOrEmail(String phoneNumber, String email);
}
