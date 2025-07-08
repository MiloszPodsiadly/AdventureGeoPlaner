package com.milosz.podsiadly.service;

import com.milosz.podsiadly.security.spotify.SpotifyAuthService;
import com.milosz.podsiadly.external.SpotifyClient;
import com.milosz.podsiadly.mapper.SpotifyPlaylistMapper;
import com.milosz.podsiadly.model.SpotifyPlaylist;
import com.milosz.podsiadly.model.User;
import com.milosz.podsiadly.repository.SpotifyPlaylistRepository;
import com.milosz.podsiadly.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SpotifyPlaylistService {

    private final SpotifyPlaylistRepository playlistRepository;
    private final UserRepository userRepository;
    private final SpotifyClient spotifyClient;
    private final SpotifyAuthService spotifyAuthService;

    @Transactional
    public List<SpotifyPlaylist> importPlaylistsForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        String token = spotifyAuthService.getAccessToken();
        var response = spotifyClient.fetchFeaturedPlaylists(token);

        List<SpotifyPlaylist> playlists = response.playlists().items().stream()
                .map(SpotifyPlaylistMapper::toEntity)
                .peek(p -> p.setUser(user))
                .toList();

        return playlistRepository.saveAll(playlists);
    }

    public List<SpotifyPlaylist> getAll() {
        return playlistRepository.findAll();
    }

    public List<SpotifyPlaylist> getByUserId(Long userId) {
        return playlistRepository.findByUserId(userId);
    }

    public SpotifyPlaylist getById(Long id) {
        return playlistRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Playlist not found with id: " + id));
    }

    @Transactional
    public SpotifyPlaylist createPlaylist(Long userId, SpotifyPlaylist playlist) {
        validate(playlist);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        playlist.setUser(user);
        return playlistRepository.save(playlist);
    }

    @Transactional
    public void deletePlaylist(Long id) {
        if (!playlistRepository.existsById(id)) {
            throw new EntityNotFoundException("Playlist not found with id: " + id);
        }
        playlistRepository.deleteById(id);
    }

    private void validate(SpotifyPlaylist playlist) {
        if (playlist == null) throw new IllegalArgumentException("Playlist cannot be null");
        if (StringUtils.isBlank(playlist.getSpotifyId()))
            throw new IllegalArgumentException("Spotify ID is required");
        if (StringUtils.isBlank(playlist.getName()))
            throw new IllegalArgumentException("Playlist name is required");
    }
}
