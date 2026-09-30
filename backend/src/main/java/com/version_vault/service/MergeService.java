package com.version_vault.service;

import com.version_vault.dtos.MergeStatus;
import com.version_vault.dtos.request.MergeRequest;
import com.version_vault.dtos.response.MergeConflictResponse;
import com.version_vault.dtos.response.MergeResponse;
import com.version_vault.exceptions.ResourceNotFoundException;
import com.version_vault.exceptions.UnauthorizedException;
import com.version_vault.models.*;
import com.version_vault.repo.BranchRepository;
import com.version_vault.repo.CommitRepository;
import com.version_vault.repo.ManifestEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class MergeService {

    private final BranchRepository branchRepository;
    private final CommitRepository commitRepository;
    private final ManifestEntryRepository manifestEntryRepository;

    private final CommitService commitService;
    private final ManifestService manifestService;
    private final WorkingTreeService workingTreeService;

    @Transactional
    public MergeResponse merge(UUID targetBranchId, UUID sourceBranchId, MergeRequest request, User user) {

        // ---------------------------------------------------------
        // 1. Validate branch IDs
        // ---------------------------------------------------------

        if (targetBranchId.equals(sourceBranchId)) {
            throw new IllegalArgumentException(
                    "Source and target branches must be different"
            );
        }

        // ---------------------------------------------------------
        // 2. Load target branch
        // ---------------------------------------------------------

        Branch targetBranch = branchRepository.findById(targetBranchId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Target branch not found with id "
                                        + targetBranchId
                        )
                );

        // ---------------------------------------------------------
        // 3. Load source branch
        // ---------------------------------------------------------

        Branch sourceBranch = branchRepository.findById(sourceBranchId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Source branch not found with id "
                                        + sourceBranchId
                        )
                );

        // ---------------------------------------------------------
        // 4. Same repository check
        // ---------------------------------------------------------

        UUID targetRepositoryId =
                targetBranch.getRepository().getId();

        UUID sourceRepositoryId =
                sourceBranch.getRepository().getId();

        if (!targetRepositoryId.equals(sourceRepositoryId)) {
            throw new IllegalArgumentException(
                    "Source and target branches must belong to the same repository"
            );
        }

        // ---------------------------------------------------------
        // 5. Ownership check
        // ---------------------------------------------------------

        if (!targetBranch.getRepository()
                .getOwner()
                .getId()
                .equals(user.getId())) {

            throw new UnauthorizedException(
                    "You are not authorized to merge branches in this repository"
            );
        }

        // ---------------------------------------------------------
        // 6. Get current HEADs
        // ---------------------------------------------------------

        Commit ours = targetBranch.getHeadCommit();
        Commit theirs = sourceBranch.getHeadCommit();

        // ---------------------------------------------------------
        // 7. Handle empty source
        // ---------------------------------------------------------

        if (theirs == null) {

            throw new IllegalArgumentException(
                    "Source branch has no commits"
            );
        }

        // ---------------------------------------------------------
        // 8. Target has no commits
        // ---------------------------------------------------------

        if (ours == null) {

            // Target is an empty branch.
            // Fast-forward it to source.

            int updatedRows =
                    branchRepository.updateHeadFromNull(
                            targetBranchId,
                            theirs
                    );

            if (updatedRows != 1) {
                throw new ConcurrentModificationException(
                        "Target branch HEAD changed while merging"
                );
            }

            workingTreeService.replaceWithManifest(
                    targetBranchId,
                    theirs.getManifest().getId()
            );

            return new MergeResponse(
                    targetBranchId,
                    sourceBranchId,
                    null,
                    theirs.getId(),
                    null,
                    null,
                    MergeStatus.FAST_FORWARD,
                    List.of()
            );
        }

        // ---------------------------------------------------------
        // 9. Check target working tree
        // ---------------------------------------------------------

        boolean clean = workingTreeService.isClean(
                targetBranchId,
                ours.getManifest().getId()
        );

        if (!clean) {
            throw new IllegalStateException(
                    "Target branch has uncommitted changes"
            );
        }

        // ---------------------------------------------------------
        // 10. Already at same commit
        // ---------------------------------------------------------

        if (ours.getId().equals(theirs.getId())) {

            return new MergeResponse(
                    targetBranchId,
                    sourceBranchId,
                    ours.getId(),
                    theirs.getId(),
                    ours.getId(),
                    null,
                    MergeStatus.UP_TO_DATE,
                    List.of()
            );
        }

        // ---------------------------------------------------------
        // 11. Source is already contained in target
        // ---------------------------------------------------------

        if (commitService.isAncestor(theirs, ours)) {

            return new MergeResponse(
                    targetBranchId,
                    sourceBranchId,
                    ours.getId(),
                    theirs.getId(),
                    theirs.getId(),
                    null,
                    MergeStatus.UP_TO_DATE,
                    List.of()
            );
        }

        // ---------------------------------------------------------
        // 12. Target is ancestor of source
        // ---------------------------------------------------------

        if (commitService.isAncestor(ours, theirs)) {

            int updatedRows = branchRepository.updateHead(
                            targetBranchId,
                            ours,
                            theirs
                    );

            if (updatedRows != 1) {
                throw new ConcurrentModificationException(
                        "Target branch HEAD changed while performing fast-forward"
                );
            }

            workingTreeService.replaceWithManifest(
                    targetBranchId,
                    theirs.getManifest().getId()
            );

            return new MergeResponse(
                    targetBranchId,
                    sourceBranchId,
                    ours.getId(),
                    theirs.getId(),
                    ours.getId(),
                    null,
                    MergeStatus.FAST_FORWARD,
                    List.of()
            );
        }

        // ---------------------------------------------------------
        // 13. Find merge base
        // ---------------------------------------------------------

        UUID mergeBaseId =
                commitService.findMergeBaseId(ours, theirs);

        if (mergeBaseId == null) {
            throw new IllegalStateException(
                    "Unable to find a common ancestor between the branches"
            );
        }

        Commit mergeBase =
                commitRepository.findById(mergeBaseId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Merge base commit not found with id "
                                                + mergeBaseId
                                )
                        );

        // ---------------------------------------------------------
        // 14. Load manifests
        // ---------------------------------------------------------

        Map<String, ManifestEntry> baseFiles =
                loadManifest(mergeBase);

        Map<String, ManifestEntry> oursFiles =
                loadManifest(ours);

        Map<String, ManifestEntry> theirsFiles =
                loadManifest(theirs);

        // ---------------------------------------------------------
        // 15. Perform three-way merge
        // ---------------------------------------------------------

        Set<String> allPaths = new HashSet<>();

        allPaths.addAll(baseFiles.keySet());
        allPaths.addAll(oursFiles.keySet());
        allPaths.addAll(theirsFiles.keySet());

        Map<String, ObjectEntity> mergedFiles =
                new HashMap<>();

        List<MergeConflictResponse> conflicts =
                new ArrayList<>();

        for (String path : allPaths) {

            ManifestEntry baseEntry = baseFiles.get(path);
            ManifestEntry oursEntry = oursFiles.get(path);
            ManifestEntry theirsEntry = theirsFiles.get(path);

            ObjectEntity baseObject =
                    baseEntry != null
                            ? baseEntry.getObject()
                            : null;

            ObjectEntity oursObject =
                    oursEntry != null
                            ? oursEntry.getObject()
                            : null;

            ObjectEntity theirsObject =
                    theirsEntry != null
                            ? theirsEntry.getObject()
                            : null;

            ObjectEntity result =
                    resolvePath(
                            path,
                            baseObject,
                            oursObject,
                            theirsObject,
                            conflicts
                    );

            if (result != null) {
                mergedFiles.put(path, result);
            }
        }

        // ---------------------------------------------------------
        // 16. Return conflicts without modifying repository state
        // ---------------------------------------------------------

        if (!conflicts.isEmpty()) {

            conflicts.sort(
                    Comparator.comparing(MergeConflictResponse::path)
            );

            return new MergeResponse(
                    targetBranchId,
                    sourceBranchId,
                    ours.getId(),
                    theirs.getId(),
                    mergeBaseId,
                    null,
                    MergeStatus.CONFLICT,
                    conflicts
            );
        }

        // ---------------------------------------------------------
        // 17. Create merged manifest
        // ---------------------------------------------------------

        var mergedManifest =
                manifestService.createManifestFromFiles(
                        targetBranch.getRepository(),
                        mergedFiles
                );

        // ---------------------------------------------------------
        // 18. Create merge commit
        // ---------------------------------------------------------

        String message = request.message().trim();

        Commit mergeCommit = new Commit(
                targetBranch.getRepository(),
                ours,
                theirs,
                mergedManifest,
                user,
                message
        );

        Commit savedMergeCommit =
                commitRepository.save(mergeCommit);

        // ---------------------------------------------------------
        // 19. CAS update target HEAD
        // ---------------------------------------------------------

        int updatedRows =
                branchRepository.updateHead(
                        targetBranchId,
                        ours,
                        savedMergeCommit
                );

        if (updatedRows != 1) {

            throw new ConcurrentModificationException(
                    "Target branch HEAD changed while creating merge commit"
            );
        }

        // ---------------------------------------------------------
        // 20. Replace target working tree
        // ---------------------------------------------------------

        workingTreeService.replaceWithManifest(
                targetBranchId,
                mergedManifest.getId()
        );

        // ---------------------------------------------------------
        // 21. Return successful merge
        // ---------------------------------------------------------

        return new MergeResponse(
                targetBranchId,
                sourceBranchId,
                ours.getId(),
                theirs.getId(),
                mergeBaseId,
                savedMergeCommit.getId(),
                MergeStatus.MERGED,
                List.of()
        );
    }

    private Map<String, ManifestEntry> loadManifest(
            Commit commit
    ) {

        List<ManifestEntry> entries =
                manifestEntryRepository
                        .findAllByManifestIdOrderByPathAsc(
                                commit.getManifest().getId()
                        );

        Map<String, ManifestEntry> result =
                new HashMap<>();

        for (ManifestEntry entry : entries) {
            result.put(entry.getPath(), entry);
        }

        return result;
    }

    private ObjectEntity resolvePath(
            String path,
            ObjectEntity base,
            ObjectEntity ours,
            ObjectEntity theirs,
            List<MergeConflictResponse> conflicts
    ) {

        // ---------------------------------------------------------
        // Case 1:
        // Ours and theirs are identical
        // ---------------------------------------------------------

        if (sameObject(ours, theirs)) {
            return ours;
        }

        // ---------------------------------------------------------
        // Case 2:
        // Ours didn't change from base
        // ---------------------------------------------------------

        if (sameObject(base, ours)) {
            return theirs;
        }

        // ---------------------------------------------------------
        // Case 3:
        // Theirs didn't change from base
        // ---------------------------------------------------------

        if (sameObject(base, theirs)) {
            return ours;
        }

        // ---------------------------------------------------------
        // Case 4:
        // Both changed differently
        // ---------------------------------------------------------

        conflicts.add(
                new MergeConflictResponse(
                        path,
                        base != null ? base.getId() : null,
                        ours != null ? ours.getId() : null,
                        theirs != null ? theirs.getId() : null
                )
        );

        return null;
    }

    private boolean sameObject(ObjectEntity first, ObjectEntity second) {

        if (first == null && second == null) {
            return true;
        }

        if (first == null || second == null) {
            return false;
        }

        return first.getContentHash()
                .equals(second.getContentHash());
    }
}