package com.backend.stockAllocation.repository;

import com.backend.stockAllocation.entity.Subscriber;
import com.backend.stockAllocation.enums.SubscriberStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriberRepository extends JpaRepository<Subscriber, Long> {
    Optional<Subscriber> findByEmail(String email);
    List<Subscriber> findByStatus(SubscriberStatus status);
    boolean existsByEmail(String email);
}
