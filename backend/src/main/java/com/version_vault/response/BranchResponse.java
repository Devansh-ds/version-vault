package com.version_vault.response;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record BranchResponse(
        UUID id,
        String name,
        UUID headCommitId,
        Instant createdAt,
        Instant updatedAt
) {
}
