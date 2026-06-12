package org.acme.shipping.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.acme.client.RajaOngkirClient;
import org.acme.shipping.dto.CostRequestDto;
import org.acme.shipping.dto.CostResultDto;
import org.acme.shipping.dto.HistoryDto;
import org.acme.shipping.entity.CalculationHistory;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ShippingService {

    private static final String COURIERS = "jne:sicepat:jnt:anteraja:tiki:pos";

    @Inject
    RajaOngkirClient client;

    @Inject
    ObjectMapper mapper;

    @Transactional
    public CostResultDto calculateCost(UUID userId, CostRequestDto dto) {
        // 1. Check cache
        var cached = CalculationHistory.findCached(
                userId, dto.originId(), dto.destinationId(), dto.weight());

        if (cached.isPresent()) {
            return toResult(cached.get(), true);
        }

        // 2. Rate limit — 10 new calculations per user per day
        if (CalculationHistory.countTodayByUser(userId) >= 10) {
            throw new WebApplicationException("Daily calculation limit reached", Response.Status.TOO_MANY_REQUESTS);
        }

        // 3. Call RajaOngkir
        JsonNode couriers = fetchCouriers(dto.originId(), dto.destinationId(), dto.weight());

        // 4. Save to history
        CalculationHistory history = new CalculationHistory();
        history.userId = userId;
        history.originId = dto.originId();
        history.originLabel = dto.originLabel();
        history.originStreet = dto.originStreet();
        history.destinationId = dto.destinationId();
        history.destinationLabel = dto.destinationLabel();
        history.destinationStreet = dto.destinationStreet();
        history.weight = dto.weight();
        history.result = couriers.toString();
        history.persist();

        return toResult(history, false);
    }

    public List<HistoryDto> getHistory(UUID userId) {
        return CalculationHistory.findByUser(userId).stream()
                .map(this::toHistoryDto)
                .toList();
    }

    public List<HistoryDto> getAllHistory() {
        return CalculationHistory.findAllSorted().stream()
                .map(this::toHistoryDto)
                .toList();
    }

    public CostResultDto getHistoryById(UUID userId, UUID historyId, boolean isAdmin) {
        CalculationHistory history = (CalculationHistory) CalculationHistory.findByIdOptional(historyId)
                .orElseThrow(() -> new WebApplicationException("History not found", Response.Status.NOT_FOUND));

        if (!isAdmin && !history.userId.equals(userId)) {
            throw new WebApplicationException("Forbidden", Response.Status.FORBIDDEN);
        }

        return toResult(history, true);
    }

    @Transactional
    public void deleteHistory(UUID userId, UUID historyId, boolean isAdmin) {
        CalculationHistory history = (CalculationHistory) CalculationHistory.findByIdOptional(historyId)
                .orElseThrow(() -> new WebApplicationException("History not found", Response.Status.NOT_FOUND));

        if (!isAdmin && !history.userId.equals(userId)) {
            throw new WebApplicationException("Forbidden", Response.Status.FORBIDDEN);
        }

        history.delete();
    }

    private JsonNode fetchCouriers(int originId, int destinationId, int weight) {
        try {
            String body = "origin=" + originId +
                    "&destination=" + destinationId +
                    "&weight=" + weight +
                    "&courier=" + URLEncoder.encode(COURIERS, StandardCharsets.UTF_8) +
                    "&price=lowest";

            return client.post("/calculate/domestic-cost", body);
        } catch (WebApplicationException e) {
            throw e;
        } catch (Exception e) {
            throw new WebApplicationException("Upstream service unavailable", Response.Status.BAD_GATEWAY);
        }
    }

    private CostResultDto toResult(CalculationHistory history, boolean fromCache) {
        try {
            JsonNode couriers = mapper.readTree(history.result);
            return new CostResultDto(
                    history.id.toString(),
                    fromCache,
                    history.originLabel,
                    history.destinationLabel,
                    history.weight,
                    couriers);
        } catch (Exception e) {
            throw new WebApplicationException("Failed to parse result", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    private HistoryDto toHistoryDto(CalculationHistory h) {
        return new HistoryDto(h.id.toString(), h.originLabel, h.destinationLabel, h.weight, h.createdAt);
    }
}
