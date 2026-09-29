package com.version_vault.dtos.response;

import java.util.UUID;

public record MergeBaseResponse(
        UUID commitAId,
        UUID commitBId,
        UUID mergeBaseCommitId
) {
}