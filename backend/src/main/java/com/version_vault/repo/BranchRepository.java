package com.version_vault.repo;

import com.version_vault.models.Branch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BranchRepository extends JpaRepository<Branch, UUID> {

    Optional<Branch> findByRepositoryIdAndName(
            UUID repositoryId,
            String name
    );

    boolean existsByRepositoryIdAndName(
            UUID repositoryId,
            String name
    );

    List<Branch> findAllByRepositoryId(UUID repositoryId);
}