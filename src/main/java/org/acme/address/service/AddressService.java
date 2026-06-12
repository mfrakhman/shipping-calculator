package org.acme.address.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.acme.address.dto.SavedAddressDto;
import org.acme.address.dto.SavedAddressRequestDto;
import org.acme.address.entity.SavedAddress;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class AddressService {

    public List<SavedAddressDto> getAddresses(UUID userId) {
        return SavedAddress.findByUser(userId).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public SavedAddressDto saveAddress(UUID userId, SavedAddressRequestDto dto) {
        SavedAddress address = new SavedAddress();
        address.userId = userId;
        address.label = dto.label();
        address.provinceId = dto.provinceId();
        address.provinceLabel = dto.provinceLabel();
        address.cityId = dto.cityId();
        address.cityLabel = dto.cityLabel();
        address.districtId = dto.districtId();
        address.districtLabel = dto.districtLabel();
        address.subdistrictId = dto.subdistrictId();
        address.subdistrictLabel = dto.subdistrictLabel();
        address.street = dto.street();
        address.persist();
        return toDto(address);
    }

    @Transactional
    public void deleteAddress(UUID userId, UUID addressId) {
        SavedAddress address = (SavedAddress) SavedAddress.findByIdOptional(addressId)
                .orElseThrow(() -> new WebApplicationException("Address not found", Response.Status.NOT_FOUND));

        if (!address.userId.equals(userId)) {
            throw new WebApplicationException("Forbidden", Response.Status.FORBIDDEN);
        }

        address.delete();
    }

    private SavedAddressDto toDto(SavedAddress a) {
        return new SavedAddressDto(
                a.id.toString(),
                a.label,
                a.provinceId, a.provinceLabel,
                a.cityId, a.cityLabel,
                a.districtId, a.districtLabel,
                a.subdistrictId, a.subdistrictLabel,
                a.street, a.createdAt);
    }
}
