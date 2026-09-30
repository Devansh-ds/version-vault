package com.version_vault.dtos.response;

import java.util.UUID;

public record MergeConflictResponse(
        String path,
        UUID baseObjectId,
        UUID oursObjectId,
        UUID theirsObjectId
) {
}
