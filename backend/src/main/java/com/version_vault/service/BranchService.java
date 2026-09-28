package com.version_vault.service;

import com.version_vault.dtos.request.RenameBranchRequest;
import com.version_vault.exceptions.ResourceAlreadyExistsException;
import com.version_vault.exceptions.ResourceNotFoundException;
import com.version_vault.exceptions.UnauthorizedException;
import com.version_vault.mapper.BranchMapper;
import com.version_vault.models.Branch;
import com.version_vault.models.Commit;
import com.version_vault.models.Repository;
import com.version_vault.models.User;
import com.version_vault.repo.BranchRepository;
import com.version_vault.repo.CommitRepository;
import com.version_vault.repo.RepositoryRepository;
import com.version_vault.dtos.request.CreateBranchRequest;
import com.version_vault.dtos.response.BranchResponse;
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
    public BranchResponse createBranch(CreateBranchRequest request, UUID repositoryId, User owner) {

        // verify repo exist
        Repository repository = getRepositoryById(repositoryId);

        // check if the owner owns the repo
        if (!repository.getOwner().getId().equals(owner.getId())) {
            throw new UnauthorizedException("You are not the owner of this repository");
        }

        // verify repo name + branch name is unique
        if (branchRepository.existsByRepositoryIdAndName(repositoryId, request.getName())) {
            throw new ResourceAlreadyExistsException("Branch with name " + request.getName()
                    + " already exists for repository with id " + repositoryId);
        }

        // verify the source commit belongs to the repo
        Commit sourceCommit = commitRepository.findByIdAndRepositoryId(request.getHeadCommitId(), repositoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Source commit with id " + request.getHeadCommitId()
                        + " not found for repository with id " + repositoryId));

        // create a branch with head being the source commit
        Branch branch = new Branch(repository, request.getName(), sourceCommit);

        // save
        Branch savedBranch = branchRepository.save(branch);

        // initialize a working tree of the new branch from the commit's manifest
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
            String name,
            User owner
    ) {
        Repository repository = getRepositoryById(repositoryId);

        if (!repository.getOwner().getId().equals(owner.getId())) {
            throw new UnauthorizedException("You are not the owner of this repository");
        }

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

    @Transactional
    public BranchResponse renameBranch(UUID repoId, UUID branchId, RenameBranchRequest request, User owner) {

        Repository repository = getRepositoryById(repoId);

        if (!repository.getOwner().getId().equals(owner.getId())) {
            throw new UnauthorizedException("You are not the owner of this repository");
        }

        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + branchId));

        if (!branch.getRepository().getId().equals(repository.getId())) {
            throw new UnauthorizedException("Branch does not belong to this repository");
        }

        String newName = request.name().trim();

        if (branch.getName().equals("main")) {
            throw new IllegalArgumentException("'Main' Branch cannot be renamed");
        }
        if (branch.getName().equals(newName)) {
            return branchMapper.toBranchResponse(branch);
        }
        if (branchRepository.existsByRepositoryIdAndName(repoId, newName)) {
            throw new ResourceAlreadyExistsException("Branch with name " + newName + " already exists in repo with id " + repoId);
        }

        branch.setName(newName);
        return branchMapper.toBranchResponse(branchRepository.save(branch));
    }

    @Transactional
    public void deleteBranch(UUID repoId, UUID branchId, User owner) {
        Repository repository = getRepositoryById(repoId);

        if (!repository.getOwner().getId().equals(owner.getId())) {
            throw new UnauthorizedException("You are not the owner of this repository");
        }

        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + branchId));

        if (!branch.getRepository().getId().equals(repository.getId())) {
            throw new UnauthorizedException("Branch does not belong to this repository");
        }

        if (branch.getName().equals("main")) {
            throw new IllegalArgumentException("'Main' Branch cannot be deleted");
        }

        branchRepository.delete(branch);
    }

    private Repository getRepositoryById(UUID repoId) {
        return repositoryRepository.findById(repoId)
                .orElseThrow(() -> new ResourceNotFoundException("Repository with id " + repoId + " not found"));
    }

}













