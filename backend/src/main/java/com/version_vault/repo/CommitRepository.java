package com.version_vault.repo;

import com.version_vault.models.Commit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CommitRepository extends JpaRepository<Commit, UUID> {

    List<Commit> findAllByRepositoryIdOrderByCreatedAtDesc(
            UUID repositoryId
    );

    List<Commit> findAllByParentCommitId(UUID parentCommitId);

    boolean existsByIdAndRepositoryId(
            UUID commitId,
            UUID repositoryId
    );
}