package com.version_vault.dtos.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ManifestResponse(
        UUID id,
        UUID repositoryId,
        Instant createdAt,
        List<ManifestEntryResponse> entries
) {
}