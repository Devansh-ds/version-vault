package com.version_vault.service;

import com.version_vault.exceptions.ResourceNotFoundException;
import com.version_vault.mapper.CommitMapper;
import com.version_vault.models.Branch;
import com.version_vault.models.Commit;
import com.version_vault.models.Manifest;
import com.version_vault.models.User;
import com.version_vault.repo.BranchRepository;
import com.version_vault.repo.CommitRepository;
import com.version_vault.repo.UserRepository;
import com.version_vault.request.CreateCommitRequest;
import com.version_vault.response.CommitResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ConcurrentModificationException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommitService {

    private final CommitRepository commitRepository;
    private final BranchRepository branchRepository;
    private final UserRepository userRepository;
    private final ManifestService manifestService;
    private final CommitMapper commitMapper;

    @Transactional
    public CommitResponse createCommit(CreateCommitRequest request, UUID branchId) {

        // Find branch existence
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id " + branchId));

        // capture current head
        Commit parentCommit = branch.getHeadCommit();

        // Find author/owner
        User author = userRepository.findById(request.authorId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + request.authorId()));

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

        // updating branch head to latest commit
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

}