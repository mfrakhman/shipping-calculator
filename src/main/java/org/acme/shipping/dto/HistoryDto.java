package org.acme.shipping.dto;

import java.time.LocalDateTime;

public record HistoryDto(
        String id,
        String origin,
        String destination,
        int weight,
        LocalDateTime createdAt
) {}
