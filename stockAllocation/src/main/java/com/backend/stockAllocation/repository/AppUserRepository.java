package com.backend.stockAllocation.repository;

import com.backend.stockAllocation.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
@Repository
public interface AppUserRepository extends JpaRepository<AppUser,Long> {
    Optional<AppUser> findByEmail(String email);

    boolean existsByEmail(String email);

    // Update last login timestamp
    @Modifying
    @Query("UPDATE AppUser u SET u.lastLoginAt = :time WHERE u.id = :id")
    void updateLastLogin(@Param("id") Long id, @Param("time") LocalDateTime time);
}
