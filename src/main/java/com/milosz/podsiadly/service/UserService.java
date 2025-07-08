package com.milosz.podsiadly.service;

import com.milosz.podsiadly.model.User;
import com.milosz.podsiadly.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    public User createUser(User user) {
        validate(user);
        if (user.getEmail() != null && userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("User with email already exists");
        }

        if (user.getSpotifyId() != null && userRepository.existsBySpotifyId(user.getSpotifyId())) {
            throw new IllegalArgumentException("Spotify ID already taken");
        }

        if (StringUtils.isNotBlank(user.getProvider()) && user.getProvider().equalsIgnoreCase("LOCAL")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
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

    private void validate(User user) {
        if (user == null) throw new IllegalArgumentException("User cannot be null");
        if (StringUtils.isBlank(user.getDisplayName())) throw new IllegalArgumentException("Display name is required");
        if (StringUtils.isBlank(user.getProvider())) throw new IllegalArgumentException("Provider is required");
    }
}
