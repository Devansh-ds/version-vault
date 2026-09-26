package com.version_vault.response;

import java.util.UUID;

public record ManifestEntryResponse(
        UUID id,
        UUID manifestId,
        String path,
        UUID objectId,
        Long size
) {
}