package com.infra.authorization.services;

import com.infra.authorization.persistence.entities.User;
import com.infra.authorization.persistence.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    public User findByEmail(final String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("User does not exists: " + email));
    }
    public User findById(final UUID userId) {
        return userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User does not exists with ID: " + userId));
    }

    public User saveUser(final User user) {
        return userRepository.saveAndFlush(user);
    }
}
