package com.backend.stockAllocation.repository;

import com.backend.stockAllocation.entity.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    Optional<Portfolio> findBySubscriberId(Long subscriberId);
}
