package com.version_vault.dtos.response;

import java.time.Instant;
import java.util.UUID;

public record ObjectResponse(
        UUID id,
        String contentHash,
        String storageKey,
        Long size,
        Instant createdAt
) {
}