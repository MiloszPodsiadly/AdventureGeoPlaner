package com.milosz.podsiadly.mapper;

import com.milosz.podsiadly.dto.SpotifyPlaylistDto;
import com.milosz.podsiadly.external.dto.SpotifyPlaylistResponse;
import com.milosz.podsiadly.model.SpotifyPlaylist;

import java.util.List;
import java.util.stream.Collectors;

public class SpotifyPlaylistMapper {

    public static SpotifyPlaylistDto mapToDto(SpotifyPlaylist entity) {
        return new SpotifyPlaylistDto(
                entity.getId(),
                entity.getSpotifyId(),
                entity.getName(),
                entity.getDescription(),
                entity.getImageUrl(),
                entity.getExternalUrl(),
                entity.getUser() != null ? entity.getUser().getId() : null
        );
    }

    public static List<SpotifyPlaylistDto> mapToDtoList(List<SpotifyPlaylist> list) {
        return list.stream().map(SpotifyPlaylistMapper::mapToDto).toList();
    }

    public static SpotifyPlaylist toEntity(
            SpotifyPlaylistResponse.Item item
    ) {
        return SpotifyPlaylist.builder()
                .spotifyId(item.id())
                .name(item.name())
                .description(item.description())
                .imageUrl(item.images().isEmpty() ? null : item.images().getFirst().url())
                .externalUrl(item.externalUrls().get("spotify"))
                .build();
    }
}
