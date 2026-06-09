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
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

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
        this.httpClient = HttpClient.newHttpClient();
    }

    public JsonNode get(String path) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + path))
                    .header("key", apiKey)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new WebApplicationException("RajaOngkir API error", Response.Status.BAD_GATEWAY);
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

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new WebApplicationException("RajaOngkir API error", Response.Status.BAD_GATEWAY);
            }

            return mapper.readTree(response.body()).path("data");
        } catch (WebApplicationException e) {
            throw e;
        } catch (Exception e) {
            Log.errorf(e, "RajaOngkir call failed: %s", e.getMessage());
            throw new WebApplicationException("Upstream service unavailable", Response.Status.BAD_GATEWAY);
        }
    }
}
