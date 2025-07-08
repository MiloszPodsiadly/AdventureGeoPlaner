package com.milosz.podsiadly.security;

import com.milosz.podsiadly.model.User;
import com.milosz.podsiadly.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccessGuard {

    private final UserRepository userRepository;

    public void checkOwnershipOrAdmin(Long userId, Long resourceOwnerId, String role) {
        if (!userId.equals(resourceOwnerId) && !"ADMIN".equalsIgnoreCase(role)) {
            throw new SecurityException("Access denied");
        }
    }

    public void checkUserExists(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    // Rozszerzalne – np. do sprawdzania czy użytkownik ma dostęp do edytowania miejsca, planu itp.
}
