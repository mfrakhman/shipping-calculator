package org.acme.shipping.controller;

import io.smallrye.common.annotation.Blocking;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.common.response.ApiResponse;
import org.acme.shipping.dto.CostRequestDto;
import org.acme.shipping.dto.CostResultDto;
import org.acme.shipping.dto.HistoryDto;
import org.acme.shipping.service.ShippingService;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.List;
import java.util.UUID;

@Blocking
@Path("/api/shipping")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ShippingController {

    @Inject
    ShippingService shippingService;

    @Inject
    JsonWebToken jwt;

    @POST
    @Path("/cost")
    @RolesAllowed({"USER", "ADMIN"})
    public Response calculateCost(@Valid CostRequestDto dto) {
        UUID userId = UUID.fromString(jwt.getSubject());
        CostResultDto result = shippingService.calculateCost(userId, dto);
        return Response.ok(ApiResponse.ok("Shipping cost calculated", result)).build();
    }

    @GET
    @Path("/history")
    @RolesAllowed({"USER", "ADMIN"})
    public ApiResponse<List<HistoryDto>> getHistory() {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ApiResponse.ok("History retrieved", shippingService.getHistory(userId));
    }

    @GET
    @Path("/history/{id}")
    @RolesAllowed({"USER", "ADMIN"})
    public ApiResponse<CostResultDto> getHistoryById(@PathParam("id") UUID id) {
        UUID userId = UUID.fromString(jwt.getSubject());
        boolean isAdmin = jwt.getGroups().contains("ADMIN");
        return ApiResponse.ok("History retrieved", shippingService.getHistoryById(userId, id, isAdmin));
    }

    @DELETE
    @Path("/history/{id}")
    @RolesAllowed({"USER", "ADMIN"})
    public ApiResponse<Void> deleteHistory(@PathParam("id") UUID id) {
        UUID userId = UUID.fromString(jwt.getSubject());
        boolean isAdmin = jwt.getGroups().contains("ADMIN");
        shippingService.deleteHistory(userId, id, isAdmin);
        return ApiResponse.ok("History deleted");
    }
}
