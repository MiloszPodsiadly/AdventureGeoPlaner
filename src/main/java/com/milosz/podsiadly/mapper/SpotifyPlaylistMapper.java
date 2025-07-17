
package com.milosz.podsiadly.mapper;

import com.milosz.podsiadly.dto.SpotifyPlaylistDto;
import com.milosz.podsiadly.model.SpotifyPlaylist;

import java.util.List;
import java.util.stream.Collectors;

public class SpotifyPlaylistMapper {

    public static SpotifyPlaylistDto mapToDto(SpotifyPlaylist e) {
        return new SpotifyPlaylistDto(
                e.getId(),
                e.getSpotifyId(),
                e.getName(),
                e.getDescription(),
                e.getIsPublic(),
                e.getCollaborative(),
                e.getSnapshotId(),
                e.getTrackCount(),
                e.getOwnerSpotifyId(),
                e.getOwnerDisplayName(),
                e.getExternalUrl(),
                e.getImageUrl()
        );
    }

    public static List<SpotifyPlaylistDto> mapToDtoList(List<SpotifyPlaylist> list) {
        return list.stream()
                .map(SpotifyPlaylistMapper::mapToDto)
                .collect(Collectors.toList());
    }
}
