package org.acme.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@ApplicationScoped
public class RajaOngkirClient {

    @ConfigProperty(name = "rajaongkir.api-key")
    String apiKey;

    @ConfigProperty(name = "rajaongkir.base-url")
    String baseUrl;

    @Inject
    ObjectMapper mapper;

    private HttpClient httpClient;

    @PostConstruct
    void init() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    private HttpResponse<String> sendWithRetry(HttpRequest request) throws IOException, InterruptedException {
        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            // Stale pooled connection reset by server — retry once on a fresh connection
            Log.debugf("Retrying request after connection reset: %s", e.getMessage());
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        }
    }

    public JsonNode get(String path) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + path))
                    .header("key", apiKey)
                    .GET()
                    .build();

            HttpResponse<String> response = sendWithRetry(request);

            if (response.statusCode() != 200) {
                String msg = extractErrorMessage(response.body(), "RajaOngkir API error");
                Response.Status status = response.statusCode() < 500
                        ? Response.Status.BAD_REQUEST
                        : Response.Status.BAD_GATEWAY;
                throw new WebApplicationException(msg, status);
            }

            return mapper.readTree(response.body()).path("data");
        } catch (WebApplicationException e) {
            throw e;
        } catch (Exception e) {
            Log.errorf(e, "RajaOngkir call failed: %s", e.getMessage());
            throw new WebApplicationException("Upstream service unavailable", Response.Status.BAD_GATEWAY);
        }
    }

    public JsonNode post(String path, String body) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + path))
                    .header("key", apiKey)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = sendWithRetry(request);

            if (response.statusCode() != 200) {
                String msg = extractErrorMessage(response.body(), "RajaOngkir API error");
                Response.Status status = response.statusCode() < 500
                        ? Response.Status.BAD_REQUEST
                        : Response.Status.BAD_GATEWAY;
                throw new WebApplicationException(msg, status);
            }

            return mapper.readTree(response.body()).path("data");
        } catch (WebApplicationException e) {
            throw e;
        } catch (Exception e) {
            Log.errorf(e, "RajaOngkir call failed: %s", e.getMessage());
            throw new WebApplicationException("Upstream service unavailable", Response.Status.BAD_GATEWAY);
        }
    }

    private String extractErrorMessage(String body, String fallback) {
        try {
            JsonNode root = mapper.readTree(body);
            JsonNode msg = root.path("meta").path("message");
            if (!msg.isMissingNode() && !msg.asText().isBlank()) return msg.asText();
        } catch (Exception ignored) {}
        return fallback;
    }
}
