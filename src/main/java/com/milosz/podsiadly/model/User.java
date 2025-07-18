package com.milosz.podsiadly.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String displayName;

    @Column(name = "spotify_id", unique = true)
    private String spotifyId;

    private String provider; // SPOTIFY, GOOGLE, etc.

    private String role; // USER, ADMIN

    @Column(name = "spotify_access_token", length = 1000) // Access tokens can be long
    private String spotifyAccessToken;

    @Column(name = "spotify_refresh_token", length = 1000) // Refresh tokens can also be long
    private String spotifyRefreshToken;

    @Column(name = "spotify_token_expires_at")
    private Instant spotifyTokenExpiresAt; // Use Instant to store the expiration timestamp

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TripPlan> tripPlans;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Recommendation> recommendations;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SpotifyPlaylist> playlists = new ArrayList<>();
}