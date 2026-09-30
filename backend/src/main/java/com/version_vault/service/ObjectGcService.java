package com.version_vault.service;

import com.version_vault.configs.GcProperties;
import com.version_vault.dtos.response.GarbageCollectionResponse;
import com.version_vault.dtos.response.ReachabilityResult;
import com.version_vault.models.Branch;
import com.version_vault.models.Commit;
import com.version_vault.models.ManifestEntry;
import com.version_vault.models.ObjectEntity;
import com.version_vault.repo.BranchRepository;
import com.version_vault.repo.ManifestEntryRepository;
import com.version_vault.repo.ObjectRepository;
import com.version_vault.repo.WorkingEntryRepository;
import com.version_vault.storage.ObjectStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ObjectGcService {

    private final BranchRepository branchRepository;
    private final ManifestEntryRepository manifestEntryRepository;
    private final ObjectRepository objectRepository;
    private final GcProperties gcProperties;
    private final ObjectStorage objectStorage;
    private final WorkingEntryRepository workingEntryRepository;

    @Transactional(readOnly = true)
    public GarbageCollectionResponse scan() {

        ReachabilityResult reachability = calculateReachability();

        List<ObjectEntity> allObjects = objectRepository.findAll();

        Instant graceCutoff = Instant.now().minus(gcProperties.getGracePeriodHours(), ChronoUnit.HOURS);

        int totalObjects = allObjects.size();
        int reachableObjects = 0;
        int garbageObjects = 0;
        int eligibleObjects = 0;

        for (ObjectEntity object : allObjects) {

            if (reachability.reachableObjectIds().contains(object.getId())) {
                reachableObjects++;
                continue;
            }

            /*
             * Object is unreachable from all branch HEADs.
             */
            garbageObjects++;

            /*
             * It becomes eligible only after the grace period.
             */
            if (object.getCreatedAt().isBefore(graceCutoff)) {
                eligibleObjects++;
            }
        }

        return new GarbageCollectionResponse(
                totalObjects,
                reachableObjects,
                garbageObjects,
                eligibleObjects,
                garbageObjects - eligibleObjects,
                0,
                0
        );
    }

    @Transactional(readOnly = true)
    public ReachabilityResult calculateReachability() {

        Set<UUID> reachableCommitIds = new HashSet<>();
        Set<UUID> reachableManifestIds = new HashSet<>();
        Set<UUID> reachableObjectIds = new HashSet<>();

        Deque<Commit> commitsToVisit = new ArrayDeque<>();

        /*
         * Start from every branch HEAD.
         */
        List<Branch> branches = branchRepository.findAll();

        for (Branch branch : branches) {

            Commit headCommit = branch.getHeadCommit();

            if (headCommit != null) {
                commitsToVisit.push(headCommit);
            }
        }

        /*
         * Traverse the complete commit DAG.
         */
        while (!commitsToVisit.isEmpty()) {

            Commit commit = commitsToVisit.pop();

            UUID commitId = commit.getId();

            /*
             * Already visited?
             *
             * - multiple branches can point to the same commit
             * - merge commits have two parents
             * - both paths can eventually reach the same ancestor
             */
            if (!reachableCommitIds.add(commitId)) {
                continue;
            }

            /*
             * Every reachable commit makes its manifest reachable.
             */
            if (commit.getManifest() != null) {

                UUID manifestId = commit.getManifest().getId();

                reachableManifestIds.add(manifestId);

                /*
                 * Every object referenced by this manifest is reachable.
                 */
                List<ManifestEntry> entries = manifestEntryRepository
                                .findAllByManifestIdOrderByPathAsc(manifestId);

                for (ManifestEntry entry : entries) {

                    if (entry.getObject() != null) {
                        reachableObjectIds.add(
                                entry.getObject().getId()
                        );
                    }
                }
            }

            /*
             * Follow the first parent.
             */
            Commit parentCommit = commit.getParentCommit();

            if (parentCommit != null) {
                commitsToVisit.push(parentCommit);
            }

            /*
             * Follow the second parent.
             *
             * This is essential for merge commits.
             */
            Commit secondParentCommit = commit.getSecondParentCommit();

            if (secondParentCommit != null) {
                commitsToVisit.push(secondParentCommit);
            }
        }

        return new ReachabilityResult(
                reachableCommitIds,
                reachableManifestIds,
                reachableObjectIds
        );
    }

    @Transactional
    public GarbageCollectionResponse collect() {

        ReachabilityResult reachability = calculateReachability();
        List<ObjectEntity> allObjects = objectRepository.findAll();

        Instant graceCutoff = Instant.now().minus(gcProperties.getGracePeriodHours(), ChronoUnit.HOURS);

        int totalObjects = allObjects.size();
        int reachableObjects = 0;
        int garbageObjects = 0;
        int eligibleObjects = 0;
        int protectedObjects = 0;
        int deletedObjects = 0;

        long reclaimedBytes = 0;

        for (ObjectEntity object : allObjects) {

            /*
             * Step 1:
             * Reachable objects can NEVER be collected.
             */
            if (reachability.reachableObjectIds().contains(object.getId())) {
                reachableObjects++;
                continue;
            }

            /*
             * Object is not reachable from committed history.
             */
            garbageObjects++;

            /*
             * Step 2:
             * Grace period protection.
             */
            if (!object.getCreatedAt().isBefore(graceCutoff)) {
                continue;
            }

            eligibleObjects++;

            /*
             * Step 3:
             * Working tree protection.
             */
            if (workingEntryRepository.existsByObjectId(object.getId())) {
                protectedObjects++;
                continue;
            }

            /*
             * Step 4:
             * Object is safe to collect.
             */
            objectRepository.delete(object);
            objectRepository.flush();
            objectStorage.delete(object.getStorageKey());
            deletedObjects++;
            reclaimedBytes += object.getSize();
        }

        return new GarbageCollectionResponse(
                totalObjects,
                reachableObjects,
                garbageObjects,
                eligibleObjects,
                protectedObjects,
                deletedObjects,
                reclaimedBytes
        );
    }
}