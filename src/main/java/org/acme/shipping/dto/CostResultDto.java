package org.acme.shipping.dto;

import com.fasterxml.jackson.databind.JsonNode;

public record CostResultDto(
        String historyId,
        boolean fromCache,
        String origin,
        String destination,
        int weight,
        JsonNode couriers
) {}
