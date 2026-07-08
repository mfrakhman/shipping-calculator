package org.acme.auth.controller;

import io.smallrye.common.annotation.Blocking;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.auth.dto.AuthResponseDto;
import org.acme.auth.dto.LoginRequestDto;
import org.acme.auth.dto.RegisterRequestDto;
import org.acme.auth.service.AuthService;
import org.acme.common.response.ApiResponse;

@Blocking
@Path("/api/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthController {

    @Inject
    AuthService authService;

    @POST
    @Path("/register")
    public Response register(@Valid RegisterRequestDto dto) {
        String token = authService.register(dto);
        return Response.status(201)
                .entity(ApiResponse.ok("Registration successful", new AuthResponseDto(token, 86400)))
                .build();
    }

    @POST
    @Path("/login")
    public Response login(@Valid LoginRequestDto dto) {
        String token = authService.login(dto);
        return Response.ok(ApiResponse.ok("Login successful", new AuthResponseDto(token, 86400))).build();
    }
}
