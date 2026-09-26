package com.version_vault.repo;

import com.version_vault.models.Branch;
import com.version_vault.models.Commit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Modifying
    @Query("""
    UPDATE Branch b
    SET b.headCommit = :newHead,
        b.updatedAt = CURRENT_TIMESTAMP
    WHERE b.id = :branchId
      AND b.headCommit IS NULL
""")
    int updateHeadFromNull(@Param("branchId") UUID branchId, @Param("newHead") Commit newHead);

    @Modifying
    @Query("""
    UPDATE Branch b
    SET b.headCommit = :newHead,
        b.updatedAt = CURRENT_TIMESTAMP
    WHERE b.id = :branchId
      AND b.headCommit = :expectedHead
""")
    int updateHead(
            @Param("branchId") UUID branchId,
            @Param("expectedHead") Commit expectedHead,
            @Param("newHead") Commit newHead
    );
}