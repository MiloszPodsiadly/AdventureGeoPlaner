// src/main/java/com/milosz/podsiadly/dto/UserDto.java
package com.milosz.podsiadly.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

public record UserDto(

        Long id,

        @Email
        @NotBlank(message = "Email is required")
        String email,

        @NotBlank(message = "Password is required")
        @JsonProperty(access = Access.WRITE_ONLY)
        String password,

        @NotBlank(message = "Display name is required")
        String displayName,

        // all Spotify fields are populated later via OAuth2,
        // so we only read them back in responses:
        @JsonProperty(access = Access.READ_ONLY)
        String spotifyId,

        @JsonProperty(access = Access.READ_ONLY)
        String provider,

        @JsonProperty(access = Access.READ_ONLY)
        String role,

        @JsonProperty(access = Access.READ_ONLY)
        String spotifyAccessToken,

        @JsonProperty(access = Access.READ_ONLY)
        String spotifyRefreshToken,

        @JsonProperty(access = Access.READ_ONLY)
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Instant spotifyTokenExpiresAt,

        @JsonProperty(access = Access.READ_ONLY)
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Instant createdAt,

        @JsonProperty(access = Access.READ_ONLY)
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Instant updatedAt
) {}
