package com.version_vault.mapper;

import com.version_vault.models.Branch;
import com.version_vault.response.BranchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BranchMapper {

    public BranchResponse toBranchResponse(Branch branch) {
        return BranchResponse.builder()
                .id(branch.getId())
                .headCommitId(branch.getHeadCommit() != null? branch.getHeadCommit().getId():null)
                .name(branch.getName())
                .createdAt(branch.getCreatedAt())
                .updatedAt(branch.getUpdatedAt())
                .build();
    }

}
