
package com.milosz.podsiadly.repository;

import com.milosz.podsiadly.model.SpotifyPlaylist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpotifyPlaylistRepository extends JpaRepository<SpotifyPlaylist, Long> {

    // Custom query method to find playlists by a specific user
    List<SpotifyPlaylist> findByUser_Id(Long userId);

    // You might also want to find by Spotify's own ID
    Optional<SpotifyPlaylist> findBySpotifyId(String spotifyId);
}