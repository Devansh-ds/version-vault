package com.version_vault.dtos.response;

import com.version_vault.dtos.MergeStatus;

import java.util.List;
import java.util.UUID;

public record MergeResponse(
        UUID targetBranchId,
        UUID sourceBranchId,
        UUID oursCommitId,
        UUID theirsCommitId,
        UUID mergeBaseCommitId,
        UUID mergeCommitId,
        MergeStatus status,
        List<MergeConflictResponse> conflicts
) {
}
