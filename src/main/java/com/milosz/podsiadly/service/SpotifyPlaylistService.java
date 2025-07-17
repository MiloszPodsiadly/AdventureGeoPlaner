package com.milosz.podsiadly.service;

import com.milosz.podsiadly.model.SpotifyPlaylist;
import com.milosz.podsiadly.model.User;
import com.milosz.podsiadly.repository.SpotifyPlaylistRepository;
import com.milosz.podsiadly.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

/*
 * Service for managing Spotify playlists and integrating with the Spotify Web API.
 *
 * Notes:
 * - WebClient instances initialized in @PostConstruct for API and auth endpoints.
 * - Access tokens are cached on the User and refreshed when near expiry.
 * - Blocking JPA I/O is kept inside @Transactional methods; if moving to reactive, wrap in boundedElastic.
 */
@Service
@RequiredArgsConstructor
public class SpotifyPlaylistService {

    private final UserRepository userRepository;
    private final SpotifyPlaylistRepository spotifyPlaylistRepository;
    private final WebClient.Builder webClientBuilder;

    @Value("${spotify.client-id}")
    private String clientId;

    @Value("${spotify.client-secret}")
    private String clientSecret;

    @Value("${spotify.token-url}")
    private String spotifyTokenUrl;

    @Value("${spotify.api-url}")
    private String spotifyApiUrl;

    private WebClient spotifyWebClient;
    private WebClient spotifyAuthWebClient;

    @PostConstruct
    public void init() {
        this.spotifyWebClient = webClientBuilder
                .baseUrl(spotifyApiUrl)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();

        this.spotifyAuthWebClient = webClientBuilder
                .baseUrl(spotifyTokenUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .build();
    }

    @Transactional(readOnly = true)
    public List<SpotifyPlaylist> getAll() {
        return spotifyPlaylistRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<SpotifyPlaylist> getByUserId(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        return spotifyPlaylistRepository.findByUser_Id(userId);
    }

    @Transactional(readOnly = true)
    public SpotifyPlaylist getById(Long id) {
        return spotifyPlaylistRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("SpotifyPlaylist not found with id: " + id));
    }

    @Transactional
    public SpotifyPlaylist createPlaylist(Long userId, SpotifyPlaylist playlist) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        playlist.setUser(user);
        return spotifyPlaylistRepository.save(playlist);
    }

    @Transactional
    public void deletePlaylist(Long id) {
        if (!spotifyPlaylistRepository.existsById(id)) {
            throw new EntityNotFoundException("SpotifyPlaylist not found with id: " + id);
        }
        spotifyPlaylistRepository.deleteById(id);
    }

    /**
     * Import and upsert playlists from Spotify for a given user.
     */
    @Transactional
    public List<SpotifyPlaylist> importPlaylistsForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        String token = getValidAccessToken(user);
        JsonNode root = spotifyWebClient.get()
                .uri("/me/playlists?limit=50")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        List<SpotifyPlaylist> imported = parseSpotifyPlaylists(root, user);
        for (SpotifyPlaylist p : imported) {
            Optional<SpotifyPlaylist> existing = spotifyPlaylistRepository.findBySpotifyId(p.getSpotifyId());
            SpotifyPlaylist toSave = existing
                    .map(e -> updateExistingPlaylist(e, p))
                    .orElse(p);
            spotifyPlaylistRepository.save(toSave);
        }
        return imported;
    }

    /** Helper to update fields on an existing playlist. */
    private SpotifyPlaylist updateExistingPlaylist(SpotifyPlaylist current, SpotifyPlaylist incoming) {
        current.setName(incoming.getName());
        current.setDescription(incoming.getDescription());
        current.setIsPublic(incoming.getIsPublic());
        current.setCollaborative(incoming.getCollaborative());
        current.setOwnerDisplayName(incoming.getOwnerDisplayName());
        current.setOwnerSpotifyId(incoming.getOwnerSpotifyId());
        current.setExternalUrl(incoming.getExternalUrl());
        current.setImageUrl(incoming.getImageUrl());
        return current;
    }

    @Transactional
    protected String getValidAccessToken(User user) {
        if (user.getSpotifyTokenExpiresAt() == null ||
                user.getSpotifyTokenExpiresAt().isBefore(Instant.now().plusSeconds(300))) {
            return refreshAccessToken(user);
        }
        return user.getSpotifyAccessToken();
    }

    /**
     * Refreshes access token using the stored refresh token.
     * Throws if Spotify response is missing expected fields.
     */
    @Transactional
    protected String refreshAccessToken(User user) {
        String refresh = user.getSpotifyRefreshToken();
        if (refresh == null) {
            throw new RuntimeException("No refresh token for user id " + user.getId());
        }

        String authHeader = "Basic " + Base64.getEncoder()
                .encodeToString((clientId + ":" + clientSecret).getBytes());

        JsonNode resp = spotifyAuthWebClient.post()
                .body(BodyInserters.fromFormData("grant_type", "refresh_token")
                        .with("refresh_token", refresh))
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        if (resp == null) {
            throw new RuntimeException("Empty token response from Spotify");
        }

        JsonNode at = resp.get("access_token");
        if (at == null) {
            throw new RuntimeException("Missing access_token in Spotify response");
        }
        String newToken = at.asText();

        JsonNode ex = resp.get("expires_in");
        long expires = ex != null ? ex.asLong() : 0;

        user.setSpotifyAccessToken(newToken);
        user.setSpotifyTokenExpiresAt(Instant.now().plusSeconds(expires));
        userRepository.save(user);
        return newToken;
    }

    /**
     * Safely parse playlists list; guards against missing nodes.
     */
    private List<SpotifyPlaylist> parseSpotifyPlaylists(JsonNode root, User user) {
        List<SpotifyPlaylist> result = new ArrayList<>();
        if (root != null && root.has("items") && root.get("items").isArray()) {
            for (JsonNode item : root.get("items")) {
                SpotifyPlaylist p = new SpotifyPlaylist();
                p.setSpotifyId(item.has("id") ? item.get("id").asText() : null);
                p.setName(item.has("name") ? item.get("name").asText() : null);
                p.setDescription(item.has("description") ? item.get("description").asText() : null);
                p.setIsPublic(item.has("public") && item.get("public").asBoolean());
                p.setCollaborative(item.has("collaborative") && item.get("collaborative").asBoolean());
                p.setOwnerDisplayName(item.path("owner").path("display_name").asText(null));
                p.setOwnerSpotifyId(item.path("owner").path("id").asText(null));
                p.setUser(user);
                p.setExternalUrl(item.path("external_urls").path("spotify").asText(null));

                JsonNode imgs = item.path("images");
                if (imgs.isArray() && !imgs.isEmpty()) {
                    p.setImageUrl(imgs.get(0).path("url").asText(null));
                }
                result.add(p);
            }
        }
        return result;
    }
}
