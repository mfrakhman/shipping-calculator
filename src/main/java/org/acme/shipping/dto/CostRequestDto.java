package org.acme.shipping.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CostRequestDto(
        @NotNull Integer originId,
        @NotBlank String originLabel,
        @NotNull Integer destinationId,
        @NotBlank String destinationLabel,
        @NotNull @Min(1) Integer weight
) {}
