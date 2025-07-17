package com.milosz.podsiadly;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@Testcontainers
@SpringBootTest
class AdventureGeoPlanerApplicationTests {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16")
                    .withDatabaseName("AdventureGeoPlanner")
                    .withUsername("kodilla_user")
                    .withPassword("kodilla_password");

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        // Database
        registry.add("spring.datasource.url",      postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        // Spotify credentials
        registry.add("spotify.client-id",     () -> "test-client-id");
        registry.add("spotify.client-secret", () -> "test-client-secret");

        // Spotify endpoints
        registry.add("spotify.token-url", () -> "https://accounts.spotify.com/api/token");
        registry.add("spotify.api-url",   () -> "https://api.spotify.com/v1");
    }

    @Test
    void contextLoads() { }
}


