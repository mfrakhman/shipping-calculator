package org.acme.tracking.dto;

import java.time.LocalDateTime;
import java.util.List;

public record TrackingHistoryItemDto(
        String id,
        String awb,
        String courier,
        String courierName,
        String status,
        boolean delivered,
        List<TrackResultDto.ManifestEntry> manifest,
        LocalDateTime createdAt
) {}
