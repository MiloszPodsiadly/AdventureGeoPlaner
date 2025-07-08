package com.milosz.podsiadly.mapper;

import com.milosz.podsiadly.dto.UserDto;
import com.milosz.podsiadly.model.User;

import java.util.List;

public class UserMapper {

    public static UserDto mapToDto(User user) {
        return new UserDto(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getSpotifyId(),
                user.getProvider(),
                user.getRole(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    public static List<UserDto> mapToDtoList(List<User> users) {
        return users.stream()
                .map(UserMapper::mapToDto)
                .toList();
    }
}
