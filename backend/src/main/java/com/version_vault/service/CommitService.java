package com.version_vault.service;

import com.version_vault.exceptions.ResourceNotFoundException;
import com.version_vault.exceptions.UnauthorizedException;
import com.version_vault.mapper.CommitMapper;
import com.version_vault.models.*;
import com.version_vault.repo.BranchRepository;
import com.version_vault.repo.CommitRepository;
import com.version_vault.repo.ManifestEntryRepository;
import com.version_vault.repo.UserRepository;
import com.version_vault.request.CreateCommitRequest;
import com.version_vault.response.CommitHistoryResponse;
import com.version_vault.response.CommitResponse;
import com.version_vault.response.HistoricalFileResponse;
import lombok.RequiredArgsConstructor;
import org.hibernate.query.spi.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommitService {

    private final CommitRepository commitRepository;
    private final BranchRepository branchRepository;
    private final ManifestService manifestService;
    private final CommitMapper commitMapper;
    private final ManifestEntryRepository manifestEntryRepository;

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

}