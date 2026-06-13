package org.acme.tracking.dto;

import jakarta.validation.constraints.NotBlank;

public record TrackRequestDto(
        @NotBlank String awb,
        @NotBlank String courier,
        String lastPhoneNumber
) {}
