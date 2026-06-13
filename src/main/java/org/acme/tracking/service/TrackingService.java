package org.acme.tracking.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.client.RajaOngkirClient;
import org.acme.tracking.dto.TrackRequestDto;
import org.acme.tracking.dto.TrackResultDto;
import org.acme.tracking.dto.TrackingHistoryItemDto;
import org.acme.tracking.entity.TrackingHistory;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class TrackingService {

    @Inject
    RajaOngkirClient client;

    @Inject
    ObjectMapper mapper;

    @Transactional
    public TrackResultDto track(UUID userId, TrackRequestDto dto) {
        String encodedAwb = URLEncoder.encode(dto.awb(), StandardCharsets.UTF_8);
        String encodedCourier = URLEncoder.encode(dto.courier(), StandardCharsets.UTF_8);

        String path = "/track/waybill?awb=" + encodedAwb + "&courier=" + encodedCourier;

        StringBuilder body = new StringBuilder()
                .append("awb=").append(encodedAwb)
                .append("&courier=").append(encodedCourier);

        if (dto.lastPhoneNumber() != null && !dto.lastPhoneNumber().isBlank()) {
            body.append("&last_phone_number=")
                .append(URLEncoder.encode(dto.lastPhoneNumber(), StandardCharsets.UTF_8));
        }

        JsonNode data = client.post(path, body.toString());
        TrackResultDto result = mapResult(data);

        save(userId, dto, result);

        return result;
    }

    public List<TrackingHistoryItemDto> getHistory(UUID userId) {
        return TrackingHistory.findByUser(userId).stream()
                .map(this::toHistoryItem)
                .toList();
    }

    private TrackingHistoryItemDto toHistoryItem(TrackingHistory h) {
        try {
            TrackResultDto result = mapper.readValue(h.result, TrackResultDto.class);
            return new TrackingHistoryItemDto(
                    h.id.toString(), h.awb, h.courier,
                    h.courierName != null ? h.courierName : h.courier,
                    h.status != null ? h.status : "",
                    h.delivered, result.manifest(), h.createdAt);
        } catch (Exception e) {
            return new TrackingHistoryItemDto(
                    h.id.toString(), h.awb, h.courier,
                    h.courierName != null ? h.courierName : h.courier,
                    h.status != null ? h.status : "",
                    h.delivered, List.of(), h.createdAt);
        }
    }

    private void save(UUID userId, TrackRequestDto dto, TrackResultDto result) {
        try {
            TrackingHistory history = new TrackingHistory();
            history.userId = userId;
            history.awb = dto.awb();
            history.courier = dto.courier();
            history.courierName = result.summary().courierName();
            history.status = result.summary().status();
            history.delivered = result.delivered();
            history.result = mapper.writeValueAsString(result);
            history.persist();
        } catch (Exception ignored) {}
    }

    private TrackResultDto mapResult(JsonNode data) {
        JsonNode s = data.path("summary");
        JsonNode ds = data.path("delivery_status");
        JsonNode manifestNode = data.path("manifest");

        TrackResultDto.Summary summary = new TrackResultDto.Summary(
                s.path("courier_code").asText(""),
                s.path("courier_name").asText(""),
                s.path("waybill_number").asText(""),
                s.path("service_code").asText(""),
                s.path("waybill_date").asText(""),
                s.path("shipper_name").asText(""),
                s.path("receiver_name").asText(""),
                s.path("origin").asText(""),
                s.path("destination").asText(""),
                s.path("status").asText("")
        );

        TrackResultDto.DeliveryStatus deliveryStatus = new TrackResultDto.DeliveryStatus(
                ds.path("status").asText(""),
                ds.path("pod_receiver").asText(""),
                ds.path("pod_date").asText(""),
                ds.path("pod_time").asText("")
        );

        List<TrackResultDto.ManifestEntry> manifest = new ArrayList<>();
        if (manifestNode.isArray()) {
            for (JsonNode e : manifestNode) {
                manifest.add(new TrackResultDto.ManifestEntry(
                        e.path("manifest_code").asText(""),
                        e.path("manifest_description").asText(""),
                        e.path("manifest_date").asText(""),
                        e.path("manifest_time").asText(""),
                        e.path("city_name").asText("")
                ));
            }
        }

        return new TrackResultDto(
                data.path("delivered").asBoolean(false),
                summary,
                deliveryStatus,
                manifest
        );
    }
}
