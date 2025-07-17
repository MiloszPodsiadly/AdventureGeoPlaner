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
                .orElseThrow(() -> new EntityNotFoundException("User " + userId));

        // 1) call /me/playlists
        JsonNode root = spotifyWebClient
                .get()
                .uri("/me/playlists?limit=50")
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        List<SpotifyPlaylist> saved = new ArrayList<>();

        // 2) for each "item" map → entity → upsert
        for (JsonNode item : root.path("items")) {
            String sid     = item.path("id").asText();
            String name    = item.path("name").asText(null);
            String desc    = item.path("description").asText(null);
            boolean pub    = item.path("public").asBoolean(false);
            boolean collab = item.path("collaborative").asBoolean(false);
            String snap    = item.path("snapshot_id").asText(null);
            int    count   = item.path("tracks").path("total").asInt(0);

            String ownerId   = item.path("owner").path("id").asText(null);
            String ownerName = item.path("owner").path("display_name").asText(null);

            String extUrl = item.path("external_urls").path("spotify").asText(null);
            String imgUrl = null;
            JsonNode images = item.path("images");
            if (images.isArray() && !images.isEmpty()) {
                imgUrl = images.get(0).path("url").asText(null);
            }

            SpotifyPlaylist pl = SpotifyPlaylist.builder()
                    .spotifyId(sid)
                    .name(name)
                    .description(desc)
                    .isPublic(pub)
                    .collaborative(collab)
                    .snapshotId(snap)
                    .trackCount(count)
                    .ownerSpotifyId(ownerId)
                    .ownerDisplayName(ownerName)
                    .externalUrl(extUrl)
                    .imageUrl(imgUrl)
                    .user(user)
                    .build();

            // upsert by spotifyId
            SpotifyPlaylist persisted = spotifyPlaylistRepository.findBySpotifyId(sid)
                    .map(existing -> {
                        existing.setName(pl.getName());
                        existing.setDescription(pl.getDescription());
                        existing.setIsPublic(pl.getIsPublic());
                        existing.setCollaborative(pl.getCollaborative());
                        existing.setSnapshotId(pl.getSnapshotId());
                        existing.setTrackCount(pl.getTrackCount());
                        existing.setOwnerSpotifyId(pl.getOwnerSpotifyId());
                        existing.setOwnerDisplayName(pl.getOwnerDisplayName());
                        existing.setExternalUrl(pl.getExternalUrl());
                        existing.setImageUrl(pl.getImageUrl());
                        return spotifyPlaylistRepository.save(existing);
                    })
                    .orElseGet(() -> spotifyPlaylistRepository.save(pl));

            saved.add(persisted);
        }

        return saved;
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

    @Transactional
    public SpotifyPlaylist importSinglePlaylist(Long userId, String spotifyPlaylistId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));

        // 1) Grab that one playlist
        JsonNode node = spotifyWebClient.get()
                .uri("/playlists/{id}", spotifyPlaylistId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getValidAccessToken(user))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        if (node == null || !node.has("id")) {
            throw new EntityNotFoundException("Spotify playlist not found: " + spotifyPlaylistId);
        }

        // 2) Map to your entity
        SpotifyPlaylist p = new SpotifyPlaylist();
        p.setSpotifyId(node.get("id").asText());
        p.setName(node.path("name").asText(null));
        p.setDescription(node.path("description").asText(null));
        p.setIsPublic(node.path("public").asBoolean(false));
        p.setCollaborative(node.path("collaborative").asBoolean(false));
        p.setOwnerDisplayName(node.path("owner").path("display_name").asText(null));
        p.setOwnerSpotifyId(node.path("owner").path("id").asText(null));
        p.setExternalUrl(node.path("external_urls").path("spotify").asText(null));
        JsonNode imgs = node.path("images");
        if (imgs.isArray() && !imgs.isEmpty()) {
            p.setImageUrl(imgs.get(0).path("url").asText(null));
        }
        p.setUser(user);

        // 3) Upsert into your DB
        return spotifyPlaylistRepository.findBySpotifyId(p.getSpotifyId())
                .map(existing -> {
                    // update fields if you wish…
                    existing.setName(p.getName());
                    existing.setDescription(p.getDescription());
                    existing.setExternalUrl(p.getExternalUrl());
                    // …etc…
                    return spotifyPlaylistRepository.save(existing);
                })
                .orElseGet(() -> spotifyPlaylistRepository.save(p));
    }
}

