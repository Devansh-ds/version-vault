package com.version_vault.dtos.response;

import java.util.List;
import java.util.UUID;

public record CommitDiffResponse(
        UUID commitId,
        UUID parentCommitId,
        List<FileDiffResponse> changes
) {
}