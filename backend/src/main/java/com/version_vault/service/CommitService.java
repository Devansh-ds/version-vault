package com.version_vault.service;

import com.version_vault.dtos.DiffStatus;
import com.version_vault.dtos.response.*;
import com.version_vault.exceptions.ResourceNotFoundException;
import com.version_vault.exceptions.UnauthorizedException;
import com.version_vault.mapper.CommitMapper;
import com.version_vault.models.*;
import com.version_vault.repo.BranchRepository;
import com.version_vault.repo.CommitRepository;
import com.version_vault.repo.ManifestEntryRepository;
import com.version_vault.dtos.request.CreateCommitRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommitService {

    private final CommitRepository commitRepository;
    private final BranchRepository branchRepository;
    private final ManifestService manifestService;
    private final CommitMapper commitMapper;
    private final ManifestEntryRepository manifestEntryRepository;
    private final ObjectService objectService;

    @Transactional
    public CommitResponse createCommit(CreateCommitRequest request, UUID branchId, User author) {

        // Find branch existence
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id " + branchId));

        if (!branch.getRepository().getOwner().getId().equals(author.getId())) {
            throw new UnauthorizedException("You doesn't own this repository to create a commit");
        }

        // capture current head
        Commit parentCommit = branch.getHeadCommit();

        // Find author/owner
        // provided by jwt

        // create snapshot of current working tree
        Manifest manifest = manifestService.createManifestEntity(branchId);

        // create commit
        Commit commit = new Commit(
                branch.getRepository(),
                parentCommit,
                manifest,
                author,
                request.message().trim()
        );

        Commit savedCommit = commitRepository.save(commit);

        // updating branch head to the latest commit
        int updatedRows;

        if (parentCommit == null) {
            updatedRows = branchRepository.updateHeadFromNull(branchId, savedCommit);
        } else {
            updatedRows = branchRepository.updateHead(branchId, parentCommit, savedCommit);
        }

        // only 1 row should have changed
        if (updatedRows != 1) {
            throw new ConcurrentModificationException("Branch HEAD changed while creating commit");
        }

        return commitMapper.toCommitResponse(savedCommit);
    }

    @Transactional(readOnly = true)
    public CommitResponse getCommit(UUID commitId) {
        Commit savedCommit = commitRepository.findById(commitId)
                .orElseThrow(() -> new ResourceNotFoundException("Commit not found with id " + commitId));
        return commitMapper.toCommitResponse(savedCommit);
    }

    @Transactional(readOnly = true)
    public CommitHistoryResponse getBranchHistory(UUID branchId, UUID cursor, int limit) {

        if (limit <= 0 || limit > 100) {
            throw new IllegalArgumentException("Limit must be between 1 and 100");
        }

        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id " + branchId));

        Commit current;

        if (cursor == null) {
            current = branch.getHeadCommit();
        } else {
            current = commitRepository.findById(cursor)
                    .orElseThrow(() -> new ResourceNotFoundException("Commit not found with id " + cursor));

            if (!current.getRepository().getId().equals(branch.getRepository().getId())) {
                throw new ResourceNotFoundException("Cursor commit does not belong to this repository");
            }
        }

        List<CommitResponse> history = new ArrayList<>();

        while (current != null &&  history.size() < limit) {
            history.add(commitMapper.toCommitResponse(current));
            current = current.getParentCommit();
        }

        UUID nextCursor = current != null ? current.getId() : null;
        boolean hasMore = current != null;

        return new CommitHistoryResponse(history, nextCursor, hasMore);
    }

    @Transactional(readOnly = true)
    public List<HistoricalFileResponse> getFilesAtCommit(UUID commitId) {
        Commit commit = commitRepository.findById(commitId)
                .orElseThrow(() -> new ResourceNotFoundException("Commit not found with id " + commitId));

        UUID manifestId = commit.getManifest().getId();

        List<ManifestEntry> entries = manifestEntryRepository.findAllByManifestIdOrderByPathAsc(manifestId);

        return entries.stream()
                .map(entry -> new HistoricalFileResponse(
                        entry.getPath(),
                        entry.getObject().getId(),
                        entry.getObject().getContentHash(),
                        entry.getObject().getSize()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public byte[] readFileAtCommit(UUID commitId, String path) {

        validatePath(path);

        Commit commit = commitRepository.findById(commitId)
                .orElseThrow(() -> new ResourceNotFoundException("Commit not found with id " + commitId));

        UUID manifestId = commit.getManifest().getId();

        ManifestEntry manifestEntry = manifestEntryRepository.findByManifestIdAndPath(manifestId, path)
                .orElseThrow(() -> new ResourceNotFoundException("File not found with path " + path));

        return objectService.readContent(manifestEntry.getObject().getId());
    }

    @Transactional(readOnly = true)
    public CommitDiffResponse getDiff(UUID commitId) {
        Commit commit = commitRepository.findById(commitId)
                .orElseThrow(() -> new ResourceNotFoundException("Commit not found with id " + commitId));

        // current commit's manifest
        UUID currentManifestId = commit.getManifest().getId();

        List<ManifestEntry> currentEntries = manifestEntryRepository.findAllByManifestIdOrderByPathAsc(currentManifestId);

        /* Initial commit:
         *
         * There is no parent snapshot, so we can't get the diff.
         * Hence, All the files are considered as ADDED
         * */
        if (commit.getParentCommit() == null) {
            List<FileDiffResponse> changes = currentEntries.stream()
                    .map(entry -> new FileDiffResponse(
                            entry.getPath(),
                            DiffStatus.ADDED,
                            null,
                            entry.getObject().getId()
                    )).toList();

            return new CommitDiffResponse(
                    commit.getId(),
                    null,
                    changes
            );
        }

        // parent commit
        Commit parentCommit = commit.getParentCommit();
        UUID parentManifestId = parentCommit.getManifest().getId();

        List<ManifestEntry> parentEntries = manifestEntryRepository.findAllByManifestIdOrderByPathAsc(parentManifestId);

        // Convert both manifests into: path -> ManifestEntry
        Map<String, ManifestEntry> oldFiles = parentEntries.stream()
                .collect(Collectors.toMap(
                                ManifestEntry::getPath,
                                Function.identity()
                        ));

        Map<String, ManifestEntry> newFile = currentEntries.stream()
                .collect(Collectors.toMap(
                        ManifestEntry::getPath,
                        Function.identity()
                ));

        // union of all paths appearing in either snapshot
        Set<String> allPaths = new HashSet<>();

        allPaths.addAll(oldFiles.keySet());
        allPaths.addAll(newFile.keySet());

        List<FileDiffResponse> changes = new ArrayList<>();

        for (String path : allPaths) {
            ManifestEntry oldEntry = oldFiles.get(path);
            ManifestEntry newEntry = newFile.get(path);

            // the file was added
            if (oldEntry == null) {
                changes.add(new FileDiffResponse(
                        path,
                        DiffStatus.ADDED,
                        null,
                        newEntry.getObject().getId()
                ));
                continue;
            }

            // the file was deleted
            if (newEntry == null) {
                changes.add(new FileDiffResponse(
                        path,
                        DiffStatus.DELETED,
                        oldEntry.getObject().getId(),
                        null
                ));
                continue;
            }

            /*
             * File exists in both commits.
             *
             * Same content hash -> unchanged.
             * Different content hash -> modified.
             */
            String oldContentHash = oldEntry.getObject().getContentHash();
            String newContentHash = newEntry.getObject().getContentHash();

            if (!oldContentHash.equals(newContentHash)) {
                changes.add(new FileDiffResponse(
                        path,
                        DiffStatus.MODIFIED,
                        oldEntry.getObject().getId(),
                        newEntry.getObject().getId()
                ));
            }
        }

        // making response deterministic
        changes.sort(Comparator.comparing(FileDiffResponse::path));

        return new CommitDiffResponse(
                commit.getId(),
                parentCommit.getId(),
                changes
        );
    }

    private void validatePath(String path) {

        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException(
                    "Path cannot be empty"
            );
        }

        if (path.contains("\\")) {
            throw new IllegalArgumentException(
                    "Path must use '/' as separator"
            );
        }

        if (path.contains("..")) {
            throw new IllegalArgumentException(
                    "Path cannot contain '..'"
            );
        }
    }
}