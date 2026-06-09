package org.acme.admin.dto;

import java.time.LocalDateTime;

public record UserDto(
        String id,
        String email,
        String name,
        String role,
        String oauthProvider,
        LocalDateTime createdAt
) {}
