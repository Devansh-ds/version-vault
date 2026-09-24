package com.version_vault.repo;

import com.version_vault.models.Manifest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ManifestRepository extends JpaRepository<Manifest, UUID> {

    List<Manifest> findAllByRepositoryId(UUID repositoryId);
}