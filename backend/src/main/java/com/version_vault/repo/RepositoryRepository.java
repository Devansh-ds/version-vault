package com.version_vault.repo;

import com.version_vault.models.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RepositoryRepository extends JpaRepository<Repository, UUID> {

    Optional<Repository> findByOwnerIdAndName(UUID ownerId, String name);

    boolean existsByOwnerIdAndName(UUID ownerId, String name);

    List<Repository> findAllByOwnerId(UUID ownerId);
}