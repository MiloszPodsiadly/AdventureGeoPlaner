package com.milosz.podsiadly.repository;

import com.milosz.podsiadly.model.SpotifyPlaylist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface SpotifyPlaylistRepository extends JpaRepository<SpotifyPlaylist, Long> {
    List<SpotifyPlaylist> findByUserId(Long userId);
}
