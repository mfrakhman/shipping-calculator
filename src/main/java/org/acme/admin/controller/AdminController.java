package org.acme.admin.controller;

import io.smallrye.common.annotation.Blocking;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.acme.admin.dto.UserDto;
import org.acme.auth.entity.User;
import org.acme.common.response.ApiResponse;
import org.acme.shipping.dto.HistoryDto;
import org.acme.shipping.service.ShippingService;

import java.util.List;

@Blocking
@Path("/api/admin")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMIN")
public class AdminController {

    @Inject
    ShippingService shippingService;

    @GET
    @Path("/history")
    public ApiResponse<List<HistoryDto>> getAllHistory() {
        return ApiResponse.ok("All history retrieved", shippingService.getAllHistory());
    }

    @GET
    @Path("/users")
    public ApiResponse<List<UserDto>> getAllUsers() {
        List<UserDto> users = User.getEntityManager()
                .createQuery("from User order by createdAt desc", User.class)
                .getResultList()
                .stream()
                .map(u -> new UserDto(
                        u.id.toString(),
                        u.email,
                        u.name,
                        u.role,
                        u.oauthProvider,
                        u.createdAt))
                .toList();
        return ApiResponse.ok("Users retrieved", users);
    }
}
