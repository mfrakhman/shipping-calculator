package org.acme.address.controller;

import io.smallrye.common.annotation.Blocking;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.address.dto.SavedAddressDto;
import org.acme.address.dto.SavedAddressRequestDto;
import org.acme.address.service.AddressService;
import org.acme.common.response.ApiResponse;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.List;
import java.util.UUID;

@Blocking
@Path("/api/address")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"USER", "ADMIN"})
public class AddressController {

    @Inject
    AddressService addressService;

    @Inject
    JsonWebToken jwt;

    @GET
    public ApiResponse<List<SavedAddressDto>> getAddresses() {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ApiResponse.ok("Addresses retrieved", addressService.getAddresses(userId));
    }

    @POST
    public Response saveAddress(@Valid SavedAddressRequestDto dto) {
        UUID userId = UUID.fromString(jwt.getSubject());
        SavedAddressDto saved = addressService.saveAddress(userId, dto);
        return Response.status(Response.Status.CREATED)
                .entity(ApiResponse.ok("Address saved", saved))
                .build();
    }

    @DELETE
    @Path("/{id}")
    public ApiResponse<Void> deleteAddress(@PathParam("id") UUID id) {
        UUID userId = UUID.fromString(jwt.getSubject());
        addressService.deleteAddress(userId, id);
        return ApiResponse.ok("Address deleted");
    }
}
