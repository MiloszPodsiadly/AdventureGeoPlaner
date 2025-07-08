package com.milosz.podsiadly.repository;

import com.milosz.podsiadly.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    boolean existsBySpotifyId(String spotifyId);

    Optional<User> findByEmail(String email);

    Optional<User> findBySpotifyId(String spotifyId);
}
