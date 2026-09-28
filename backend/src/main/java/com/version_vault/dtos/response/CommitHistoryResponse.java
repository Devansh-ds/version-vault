package com.version_vault.dtos.response;

import java.util.List;
import java.util.UUID;

public record CommitHistoryResponse(
        List<CommitResponse> commits,
        UUID nextCursor,
        boolean hasMore
) {
}