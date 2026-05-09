package com.backend.stockAllocation.service.impl;

import com.backend.stockAllocation.authentication.UserDetailsServiceImpl;
import com.backend.stockAllocation.repository.AppUserRepository;
import com.backend.stockAllocation.repository.SubscriberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserMapping {

    private final AppUserRepository appUserRepository;
    private final SubscriberRepository subscriberRepository;

    public Long getSubscriberIdFromUserId(Long id) {
        String email=appUserRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("User not found with id: " + id))
                .getEmail();

        Long subId=subscriberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Subscriber not found with email: " + email))
                .getId();
        return subId;
    }



}
