package com.version_vault.repo;

import com.version_vault.models.ObjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ObjectRepository extends JpaRepository<ObjectEntity, UUID> {

    Optional<ObjectEntity> findByContentHash(String contentHash);

    boolean existsByContentHash(String contentHash);

    Optional<ObjectEntity> findByStorageKey(String storageKey);
}