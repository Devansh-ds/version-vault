package com.version_vault.response;

import java.util.UUID;

public record HistoricalFileResponse(
        String path,
        UUID objectId,
        String contentHash,
        Long size
) {
}
