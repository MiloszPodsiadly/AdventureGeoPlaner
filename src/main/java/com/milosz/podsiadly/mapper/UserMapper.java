
package com.milosz.podsiadly.mapper;

import com.milosz.podsiadly.dto.UserDto;
import com.milosz.podsiadly.model.User;

public class UserMapper {

    /** Entity → DTO : expose every field (password omitted) */
    public static UserDto mapToDto(User u) {
        return new UserDto(
                u.getId(),
                u.getEmail(),
                null,
                u.getDisplayName(),
                u.getSpotifyId(),
                u.getProvider(),
                u.getRole(),
                u.getSpotifyAccessToken(),
                u.getSpotifyRefreshToken(),
                u.getSpotifyTokenExpiresAt(),
                u.getCreatedAt(),
                u.getUpdatedAt()
        );
    }

    /** DTO → Entity : only for initial registration */
    public static User fromDtoForCreate(UserDto dto) {
        return User.builder()
                .email(dto.email())
                .password(dto.password())     // raw, will be encoded
                .displayName(dto.displayName())
                .build();
    }
}
