package com.milosz.podsiadly.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "spotify_playlists")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpotifyPlaylist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String spotifyId;

    private String name;

    private String description;

    private String imageUrl;

    private String externalUrl;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;
}
