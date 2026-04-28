package com.backend.stockAllocation.service.impl;

import com.backend.stockAllocation.dto.request.LoginDto;
import com.backend.stockAllocation.entity.Subscriber;
import com.backend.stockAllocation.repository.SubscriberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final SubscriberRepository subscriberRepository;
    public Optional<Subscriber> login(LoginDto loginDto)
    {
        Optional<Subscriber>subscriber=subscriberRepository.findByEmail(loginDto.getEmail());
        if(subscriber.get()==null)return null;
        if(subscriber.get().getPassword().equals(loginDto.getPassword()))
        return subscriber;
        return null;
    }
}
