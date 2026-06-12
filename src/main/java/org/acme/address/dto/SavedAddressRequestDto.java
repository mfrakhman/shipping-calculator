package org.acme.address.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SavedAddressRequestDto(
        @NotBlank String label,
        @NotBlank String provinceId,
        @NotBlank String provinceLabel,
        @NotBlank String cityId,
        @NotBlank String cityLabel,
        @NotBlank String districtId,
        @NotBlank String districtLabel,
        @NotNull Integer subdistrictId,
        @NotBlank String subdistrictLabel,
        String street
) {}
