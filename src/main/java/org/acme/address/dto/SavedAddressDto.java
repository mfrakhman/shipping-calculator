package org.acme.address.dto;

import java.time.LocalDateTime;

public record SavedAddressDto(
        String id,
        String label,
        String provinceId,
        String provinceLabel,
        String cityId,
        String cityLabel,
        String districtId,
        String districtLabel,
        Integer subdistrictId,
        String subdistrictLabel,
        String street,
        LocalDateTime createdAt
) {}
