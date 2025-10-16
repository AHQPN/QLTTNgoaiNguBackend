package org.example.qlttngoaingu.Repository;

import org.example.qlttngoaingu.entity.Content;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContentRepository extends JpaRepository<Content, Integer> {
}