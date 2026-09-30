package com.version_vault.repo;

import com.version_vault.models.WorkingEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkingEntryRepository extends JpaRepository<WorkingEntry, UUID> {

    Optional<WorkingEntry> findByBranchIdAndPath(UUID branchId, String path);

    List<WorkingEntry> findAllByBranchIdOrderByPathAsc(UUID branchId);

    @Modifying
    @Query("""
    DELETE FROM WorkingEntry w
    WHERE w.branch.id = :branchId
""")
    void deleteAllByBranchId(@Param("branchId") UUID branchId);

}