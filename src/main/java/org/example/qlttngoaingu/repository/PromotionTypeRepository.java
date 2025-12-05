package org.example.qlttngoaingu.repository;

import org.example.qlttngoaingu.entity.PromotionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PromotionTypeRepository extends JpaRepository<PromotionType, Integer> {
}
