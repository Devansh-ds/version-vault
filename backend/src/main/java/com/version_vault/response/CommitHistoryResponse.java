package com.version_vault.response;

import java.util.List;
import java.util.UUID;

public record CommitHistoryResponse(
        List<CommitResponse> commits,
        UUID nextCursor,
        boolean hasMore
) {
}