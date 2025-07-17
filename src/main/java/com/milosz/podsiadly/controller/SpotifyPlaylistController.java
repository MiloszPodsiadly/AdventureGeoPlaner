package com.milosz.podsiadly.controller;

import com.milosz.podsiadly.dto.SpotifyPlaylistDto;
import com.milosz.podsiadly.mapper.SpotifyPlaylistMapper;
import com.milosz.podsiadly.model.SpotifyPlaylist;
import com.milosz.podsiadly.service.SpotifyPlaylistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/playlists")
@RequiredArgsConstructor
public class SpotifyPlaylistController {

    private final SpotifyPlaylistService playlistService;

    @GetMapping
    public ResponseEntity<List<SpotifyPlaylistDto>> getAll() {
        var result = SpotifyPlaylistMapper.mapToDtoList(playlistService.getAll());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<SpotifyPlaylistDto>> getByUser(@PathVariable Long userId) {
        var result = SpotifyPlaylistMapper.mapToDtoList(playlistService.getByUserId(userId));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SpotifyPlaylistDto> getById(@PathVariable Long id) {
        var result = SpotifyPlaylistMapper.mapToDto(playlistService.getById(id));
        return ResponseEntity.ok(result);
    }

    @PostMapping("/user/{userId}")
    public ResponseEntity<SpotifyPlaylistDto> create(
            @PathVariable Long userId,
            @RequestBody SpotifyPlaylist playlist
    ) {
        var created = playlistService.createPlaylist(userId, playlist);
        return ResponseEntity.ok(SpotifyPlaylistMapper.mapToDto(created));
    }

    // ────────────────────────────────────────────────────────────────────────
    // EXISTING “bulk import”:
    @PostMapping("/user/{userId}/import")
    public ResponseEntity<List<SpotifyPlaylistDto>> importFromSpotify(@PathVariable Long userId) {
        var imported = playlistService.importPlaylistsForUser(userId);
        return ResponseEntity.ok(SpotifyPlaylistMapper.mapToDtoList(imported));
    }

    // ────────────────────────────────────────────────────────────────────────

    // ────────────────────────────────────────────────────────────────────────
    // NEW: single‐playlist import
    @PostMapping("/user/{userId}/import/{playlistId}")
    public ResponseEntity<SpotifyPlaylistDto> importOne(
            @PathVariable Long userId,
            @PathVariable String playlistId
    ) {
        // calls new service method importSinglePlaylist(...)
        var playlist = playlistService.importSinglePlaylist(userId, playlistId);
        return ResponseEntity.ok(SpotifyPlaylistMapper.mapToDto(playlist));
    }
    // ────────────────────────────────────────────────────────────────────────

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        playlistService.deletePlaylist(id);
        return ResponseEntity.noContent().build();
    }
}
