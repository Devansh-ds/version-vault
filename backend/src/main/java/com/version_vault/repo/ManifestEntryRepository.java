package com.version_vault.repo;

import com.version_vault.models.ManifestEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ManifestEntryRepository
        extends JpaRepository<ManifestEntry, UUID> {

    Optional<ManifestEntry> findByManifestIdAndPath(
            UUID manifestId,
            String path
    );

    List<ManifestEntry> findAllByManifestId(UUID manifestId);

    boolean existsByManifestIdAndPath(
            UUID manifestId,
            String path
    );
}