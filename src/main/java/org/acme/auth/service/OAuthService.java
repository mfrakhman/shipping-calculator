package org.acme.auth.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.acme.auth.entity.User;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

@ApplicationScoped
public class OAuthService {

    @ConfigProperty(name = "google.client-id")
    String clientId;

    @ConfigProperty(name = "google.client-secret")
    String clientSecret;

    @ConfigProperty(name = "google.redirect-uri")
    String redirectUri;

    @Inject
    AuthService authService;

    @Inject
    ObjectMapper objectMapper;

    private HttpClient httpClient;

    @PostConstruct
    void init() {
        this.httpClient = HttpClient.newHttpClient();
    }

    public String getAuthorizationUrl() {
        return "https://accounts.google.com/o/oauth2/v2/auth" +
                "?client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8) +
                "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8) +
                "&response_type=code" +
                "&scope=" + URLEncoder.encode("openid email profile", StandardCharsets.UTF_8);
    }

    public String handleCallback(String code) {
        try {
            String idToken = exchangeCode(code);
            JsonNode userInfo = decodeIdToken(idToken);
            User user = findOrCreateUser(
                    userInfo.get("sub").asText(),
                    userInfo.get("email").asText(),
                    userInfo.get("name").asText()
            );
            return authService.generateToken(user);
        } catch (WebApplicationException e) {
            throw e;
        } catch (Exception e) {
            throw new WebApplicationException("OAuth authentication failed", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    private String exchangeCode(String code) throws IOException, InterruptedException {
        String body = "code=" + URLEncoder.encode(code, StandardCharsets.UTF_8) +
                "&client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8) +
                "&client_secret=" + URLEncoder.encode(clientSecret, StandardCharsets.UTF_8) +
                "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8) +
                "&grant_type=authorization_code";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://oauth2.googleapis.com/token"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new WebApplicationException("Failed to exchange OAuth code", Response.Status.BAD_GATEWAY);
        }

        return objectMapper.readTree(response.body()).get("id_token").asText();
    }

    private JsonNode decodeIdToken(String idToken) throws IOException {
        String[] parts = idToken.split("\\.");
        byte[] payload = Base64.getUrlDecoder().decode(parts[1]);
        return objectMapper.readTree(payload);
    }

    @Transactional
    User findOrCreateUser(String googleId, String email, String name) {
        Optional<User> existing = User.findByEmail(email);

        if (existing.isPresent()) {
            User user = existing.get();
            if (user.oauthProvider == null) {
                user.oauthProvider = "google";
                user.oauthId = googleId;
            }
            return user;
        }

        User user = new User();
        user.email = email;
        user.name = name;
        user.oauthProvider = "google";
        user.oauthId = googleId;
        user.role = "USER";
        user.persist();

        return user;
    }
}
