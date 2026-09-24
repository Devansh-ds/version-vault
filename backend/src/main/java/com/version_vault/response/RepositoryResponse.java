package com.version_vault.response;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record RepositoryResponse(
        UUID id,
        String name,
        String description,
        Instant createdAt,
        Instant updatedAt,
        UUID ownerId,
        String ownerName
) {
}
