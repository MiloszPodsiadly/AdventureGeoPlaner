package com.milosz.podsiadly.service;

import com.milosz.podsiadly.model.User;
import com.milosz.podsiadly.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
    }

    public User getUserBySpotifyId(String spotifyId) {
        return userRepository.findBySpotifyId(spotifyId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with Spotify ID: " + spotifyId));
    }

    @Transactional
    public User register(User user) {
        // 1) Validate only the three required fields on the entity:
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (user.getDisplayName() == null || user.getDisplayName().isBlank()) {
            throw new IllegalArgumentException("Display name is required");
        }

        // 2) Unique‐email check:
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("User with this email already exists");
        }

        // 3) Clear any incoming ID so JPA will generate one:
        user.setId(null);

        // 4) Populate defaults & generated values:
        user.setProvider("LOCAL");
        user.setRole("USER");
        String spotifyId;
        do {
            spotifyId = UUID.randomUUID()
                    .toString()
                    .replace("-", "")
                    .substring(0, 10);
        } while (userRepository.existsBySpotifyId(spotifyId));

        // 5) Encode the raw password:
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 6) Persist: JPA will set ID; AuditingEntityListener will fill createdAt/updatedAt
        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(Long id, User updated) {
        User existing = getUserById(id);

        existing.setDisplayName(updated.getDisplayName());
        existing.setEmail(updated.getEmail());
        existing.setRole(updated.getRole());
        existing.setProvider(updated.getProvider());

        return userRepository.save(existing);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User not found");
        }
        userRepository.deleteById(id);
    }


}
