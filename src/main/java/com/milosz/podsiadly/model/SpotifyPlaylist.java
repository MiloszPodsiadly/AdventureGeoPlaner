package com.milosz.podsiadly.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "spotify_playlists") // Or whatever table name you prefer
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpotifyPlaylist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "spotify_id", unique = true)
    private String spotifyId; // Spotify's unique ID for the playlist

    private String name;
    private String description;
    private Boolean isPublic; // Renamed to isPublic to avoid keyword conflict, if any
    private Boolean collaborative;
    private String ownerDisplayName;
    private String ownerSpotifyId;
    private String externalUrl; // Link to the playlist on Spotify
    private String imageUrl; // URL of the playlist's cover image

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // Link to your internal User entity

    // You might add a list of tracks, but that gets more complex (many-to-many relationship)
    // private List<SpotifyTrack> tracks;
}