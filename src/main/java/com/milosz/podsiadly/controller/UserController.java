package com.milosz.podsiadly.controller;

import com.milosz.podsiadly.dto.UserDto;
import com.milosz.podsiadly.mapper.UserMapper;
import com.milosz.podsiadly.model.User;
import com.milosz.podsiadly.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users.stream().map(UserMapper::mapToDto).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(UserMapper.mapToDto(user));
    }

    @GetMapping("/spotify/{spotifyId}")
    public ResponseEntity<UserDto> getBySpotifyId(@PathVariable String spotifyId) {
        User user = userService.getUserBySpotifyId(spotifyId);
        return ResponseEntity.ok(UserMapper.mapToDto(user));
    }

    @PostMapping
    public ResponseEntity<UserDto> register(@Valid @RequestBody UserDto dto) {
        // 1) DTO → Entity
        User toCreate = UserMapper.fromDtoForCreate(dto);

        // 2) Service works purely on the User entity
        User created  = userService.register(toCreate);

        // 3) Entity → DTO for the response
        UserDto responseDto = UserMapper.mapToDto(created);
        return ResponseEntity.ok(responseDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id, @RequestBody User user) {
        User updated = userService.updateUser(id, user);
        return ResponseEntity.ok(UserMapper.mapToDto(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
