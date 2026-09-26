package com.version_vault.service;

import com.version_vault.exceptions.ResourceAlreadyExistsException;
import com.version_vault.exceptions.ResourceNotFoundException;
import com.version_vault.mapper.BranchMapper;
import com.version_vault.models.Branch;
import com.version_vault.models.Commit;
import com.version_vault.models.Repository;
import com.version_vault.repo.BranchRepository;
import com.version_vault.repo.CommitRepository;
import com.version_vault.repo.RepositoryRepository;
import com.version_vault.request.CreateBranchRequest;
import com.version_vault.response.BranchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BranchService {

    private final BranchRepository branchRepository;
    private final RepositoryRepository repositoryRepository;
    private final CommitRepository commitRepository;
    private final BranchMapper branchMapper;
    private final WorkingTreeService workingTreeService;

    public void createMainBranch(Repository repository) {
        Branch branch = new Branch(repository, "main", null);
        branchRepository.save(branch);
    }

    @Transactional
    public BranchResponse createBranch(CreateBranchRequest request, UUID repositoryId) {

        // verify repo exist
        Repository repository = repositoryRepository.findById(repositoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Repository with id " + repositoryId + " not found"));

        // verify repo name + branch name is unique
        if (branchRepository.existsByRepositoryIdAndName(repositoryId, request.getName())) {
            throw new ResourceAlreadyExistsException("Branch with name " + request.getName()
                    + " already exists for repository with id " + repositoryId);
        }

        // verify source commit belongs to the repo
        Commit sourceCommit = commitRepository.findByIdAndRepositoryId(request.getHeadCommitId(), repositoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Source commit with id " + request.getHeadCommitId()
                        + " not found for repository with id " + repositoryId));

        // create branch with head being source commit
        Branch branch = new Branch(repository, request.getName(), sourceCommit);

        // save
        Branch savedBranch = branchRepository.save(branch);

        // initialize working tree of new branch from commit's manifest
        workingTreeService.initializeFromManifestOfCommit(savedBranch.getId(), sourceCommit.getManifest().getId());

        return branchMapper.toBranchResponse(savedBranch);
    }

    @Transactional(readOnly = true)
    public BranchResponse getBranchById(UUID id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch with id " + id + " not found"));

        return branchMapper.toBranchResponse(branch);
    }

    @Transactional(readOnly = true)
    public BranchResponse getBranchByRepositoryAndName(
            UUID repositoryId,
            String name
    ) {
        Branch branch = branchRepository
                .findByRepositoryIdAndName(repositoryId, name)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Branch with name " + name
                                        + " not found in repository "
                                        + repositoryId
                        ));

        return branchMapper.toBranchResponse(branch);
    }

    @Transactional(readOnly = true)
    public List<BranchResponse> getBranchesByRepository(
            UUID repositoryId
    ) {
        repositoryRepository.findById(repositoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Repository with id " + repositoryId + " not found"));

        return branchRepository
                .findAllByRepositoryId(repositoryId)
                .stream()
                .map(branchMapper::toBranchResponse)
                .toList();
    }

}