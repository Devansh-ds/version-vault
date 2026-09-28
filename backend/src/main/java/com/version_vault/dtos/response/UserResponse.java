package com.version_vault.dtos.response;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String username,
        String email
) {
}
