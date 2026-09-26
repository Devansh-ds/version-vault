package com.version_vault.response;

import java.time.Instant;
import java.util.UUID;

public record CommitResponse(
        UUID id,
        UUID repositoryId,
        UUID parentCommitId,
        UUID manifestId,
        UUID authorId,
        String message,
        Instant createdAt
) {
}
