package com.milosz.podsiadly.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "spotify_playlists")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class SpotifyPlaylist {
    @Id
    @GeneratedValue
    private Long      id;

    // the Spotify PK for this playlist
    @Column(unique = true, nullable = false)
    private String    spotifyId;

    private String    name;
    private String    description;
    private Boolean   isPublic;
    private Boolean   collaborative;
    private String    snapshotId;
    private Integer   trackCount;

    private String    ownerSpotifyId;
    private String    ownerDisplayName;

    private String    externalUrl;
    private String    imageUrl;

    // link back to your own User
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private User      user;

    // You might add a list of tracks, but that gets more complex (many-to-many relationship)
    // private List<SpotifyTrack> tracks;
}