package com.milosz.podsiadly.security.oauth2;
import com.milosz.podsiadly.model.User;
import com.milosz.podsiadly.security.jwt.JwtUtil;
import com.milosz.podsiadly.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String spotifyId = oAuth2User.getAttribute("id");
        String email = oAuth2User.getAttribute("email");

        // 🔁 Szukamy użytkownika lub tworzymy nowego
        User user = userRepository.findBySpotifyId(spotifyId)
                .orElseGet(() -> userRepository.save(User.builder()
                        .spotifyId(spotifyId)
                        .email(email)
                        .displayName(oAuth2User.getAttribute("display_name"))
                        .provider("SPOTIFY")
                        .role("USER")
                        .build()));

        // 🔐 Tworzymy JWT
        String jwt = jwtUtil.generateToken(
                new org.springframework.security.core.userdetails.User(
                        user.getEmail(),
                        "",
                        List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
                )
        );

        // 📨 Odesłanie tokena do frontu (np. redirect z tokenem w URL lub cookie)
        response.sendRedirect("http://localhost:3000/oauth-success?token=" + jwt);
    }
}

