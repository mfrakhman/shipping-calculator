package org.acme.auth.dto;

public record AuthResponseDto(String token, long expiresIn) {}
