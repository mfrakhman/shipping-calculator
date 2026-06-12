package org.acme.location.service;

import com.fasterxml.jackson.databind.JsonNode;
import io.quarkus.cache.CacheResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.client.RajaOngkirClient;
import org.acme.location.dto.CityDto;
import org.acme.location.dto.DistrictDto;
import org.acme.location.dto.ProvinceDto;
import org.acme.location.dto.SubdistrictDto;

import java.util.List;
import java.util.stream.StreamSupport;

@ApplicationScoped
public class LocationService {

    @Inject
    RajaOngkirClient client;

    @CacheResult(cacheName = "location-provinces")
    public List<ProvinceDto> getProvinces() {
        JsonNode data = client.get("/destination/province");
        return StreamSupport.stream(data.spliterator(), false)
                .map(n -> new ProvinceDto(
                        n.get("id").asText(),
                        toTitleCase(n.get("name").asText())))
                .toList();
    }

    @CacheResult(cacheName = "location-cities")
    public List<CityDto> getCities(String provinceId) {
        JsonNode data = client.get("/destination/city/" + provinceId);
        return StreamSupport.stream(data.spliterator(), false)
                .map(n -> new CityDto(
                        n.get("id").asText(),
                        toTitleCase(n.get("name").asText()),
                        n.has("type") ? n.get("type").asText() : ""))
                .toList();
    }

    @CacheResult(cacheName = "location-districts")
    public List<DistrictDto> getDistricts(String cityId) {
        JsonNode data = client.get("/destination/district/" + cityId);
        return StreamSupport.stream(data.spliterator(), false)
                .map(n -> new DistrictDto(
                        n.get("id").asText(),
                        toTitleCase(n.get("name").asText())))
                .toList();
    }

    @CacheResult(cacheName = "location-subdistricts")
    public List<SubdistrictDto> getSubdistricts(String districtId) {
        JsonNode data = client.get("/destination/sub-district/" + districtId);
        return StreamSupport.stream(data.spliterator(), false)
                .map(n -> new SubdistrictDto(
                        n.get("id").asText(),
                        toTitleCase(n.get("name").asText()),
                        n.has("zip_code") ? n.get("zip_code").asText() : null))
                .toList();
    }

    private String toTitleCase(String name) {
        if (name == null || name.isBlank()) return name;
        String[] words = name.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (word.startsWith("(") && word.endsWith(")")) {
                sb.append(word);
            } else {
                sb.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase());
            }
            sb.append(" ");
        }
        return sb.toString().trim();
    }
}
