package org.acme.tracking.dto;

import java.util.List;

public record TrackResultDto(
        boolean delivered,
        Summary summary,
        DeliveryStatus deliveryStatus,
        List<ManifestEntry> manifest
) {
    public record Summary(
            String courierCode,
            String courierName,
            String waybillNumber,
            String serviceCode,
            String waybillDate,
            String shipperName,
            String receiverName,
            String origin,
            String destination,
            String status
    ) {}

    public record DeliveryStatus(
            String status,
            String podReceiver,
            String podDate,
            String podTime
    ) {}

    public record ManifestEntry(
            String code,
            String description,
            String date,
            String time,
            String cityName
    ) {}
}
