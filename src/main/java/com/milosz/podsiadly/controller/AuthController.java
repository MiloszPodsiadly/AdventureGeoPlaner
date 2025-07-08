package com.milosz.podsiadly.controller;
import com.milosz.podsiadly.model.User;
import com.milosz.podsiadly.security.jwt.JwtUtil;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.milosz.podsiadly.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @GetMapping("/spotify/success")
    public ResponseEntity<String> handleSuccess(Authentication authentication) {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String spotifyId = oAuth2User.getAttribute("id");
        String email = oAuth2User.getAttribute("email");

        // Rejestracja lub logowanie
        User user = userRepository.findBySpotifyId(spotifyId)
                .orElseGet(() -> userRepository.save(User.builder()
                        .spotifyId(spotifyId)
                        .email(email)
                        .provider("SPOTIFY")
                        .displayName(oAuth2User.getAttribute("display_name"))
                        .role("USER")
                        .build()
                ));

        String jwt = jwtUtil.generateToken(new org.springframework.security.core.userdetails.User(
                user.getEmail(), "", List.of(new SimpleGrantedAuthority("ROLE_USER"))
        ));

        return ResponseEntity.ok(jwt); // frontend zapisuje token
    }
}

