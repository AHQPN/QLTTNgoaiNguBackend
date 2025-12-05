package org.example.qlttngoaingu.repository;

import org.example.qlttngoaingu.entity.InvoiceDetailPromotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceDetailPromotionRepository extends JpaRepository<InvoiceDetailPromotion, Long> {
}
