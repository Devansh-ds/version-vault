package com.version_vault.repo;

import com.version_vault.models.WorkingEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkingEntryRepository extends JpaRepository<WorkingEntry, UUID> {

    Optional<WorkingEntry> findByBranchIdAndPath(UUID branchId, String path);

    List<WorkingEntry> findAllByBranchIdOrderByPathAsc(UUID branchId);

}