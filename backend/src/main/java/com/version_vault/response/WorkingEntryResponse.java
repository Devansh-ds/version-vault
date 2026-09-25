package com.version_vault.response;

import java.time.Instant;
import java.util.UUID;

public record WorkingEntryResponse(
        UUID id,
        UUID branchId,
        String path,
        UUID objectId,
        Long size,
        Instant createdAt,
        Instant updatedAt
) {
}