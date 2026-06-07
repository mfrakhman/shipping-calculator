package org.acme.auth.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.auth.service.OAuthService;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.net.URI;

@Path("/api/auth/oauth")
@Produces(MediaType.APPLICATION_JSON)
public class OAuthController {

    @Inject
    OAuthService oauthService;

    @ConfigProperty(name = "frontend.url")
    String frontendUrl;

    @GET
    @Path("/google")
    public Response initiateGoogleLogin() {
        return Response.temporaryRedirect(URI.create(oauthService.getAuthorizationUrl())).build();
    }

    @GET
    @Path("/google/callback")
    public Response handleGoogleCallback(
            @QueryParam("code") String code,
            @QueryParam("error") String error) {

        if (error != null || code == null) {
            return Response.temporaryRedirect(URI.create(frontendUrl + "/login?error=oauth_failed")).build();
        }

        try {
            String token = oauthService.handleCallback(code);
            String redirect = frontendUrl + "/oauth/callback?token=" + token + "&expiresIn=86400";
            return Response.temporaryRedirect(URI.create(redirect)).build();
        } catch (Exception e) {
            return Response.temporaryRedirect(URI.create(frontendUrl + "/login?error=oauth_failed")).build();
        }
    }
}
