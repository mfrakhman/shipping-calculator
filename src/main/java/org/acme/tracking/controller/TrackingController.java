package org.acme.tracking.controller;

import io.smallrye.common.annotation.Blocking;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.acme.common.response.ApiResponse;
import org.acme.tracking.dto.TrackRequestDto;
import org.acme.tracking.dto.TrackResultDto;
import org.acme.tracking.dto.TrackingHistoryItemDto;
import org.acme.tracking.service.TrackingService;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.List;
import java.util.UUID;

@Blocking
@Path("/api/tracking")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TrackingController {

    @Inject
    TrackingService trackingService;

    @Inject
    JsonWebToken jwt;

    @POST
    @RolesAllowed({"USER", "ADMIN"})
    public ApiResponse<TrackResultDto> track(@Valid TrackRequestDto dto) {
        UUID userId = UUID.fromString(jwt.getSubject());
        TrackResultDto result = trackingService.track(userId, dto);
        return ApiResponse.ok("Tracking result retrieved", result);
    }

    @GET
    @Path("/history")
    @RolesAllowed({"USER", "ADMIN"})
    public ApiResponse<List<TrackingHistoryItemDto>> getHistory() {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ApiResponse.ok("Tracking history retrieved", trackingService.getHistory(userId));
    }
}
