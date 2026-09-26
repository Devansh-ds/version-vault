package com.version_vault.mapper;

import com.version_vault.models.Commit;
import com.version_vault.response.CommitResponse;
import org.springframework.stereotype.Component;

@Component
public class CommitMapper {

    public CommitResponse toCommitResponse(Commit commit) {
        return new CommitResponse(
                commit.getId(),
                commit.getRepository().getId(),
                commit.getParentCommit() != null? commit.getParentCommit().getId(): null,
                commit.getManifest().getId(),
                commit.getAuthor().getId(),
                commit.getMessage(),
                commit.getCreatedAt()
        );
    }

}
