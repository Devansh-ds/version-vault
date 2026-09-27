package com.version_vault.service;

import com.version_vault.exceptions.ResourceAlreadyExistsException;
import com.version_vault.exceptions.ResourceNotFoundException;
import com.version_vault.mapper.RepositoryMapper;
import com.version_vault.models.Repository;
import com.version_vault.models.User;
import com.version_vault.repo.RepositoryRepository;
import com.version_vault.request.CreateRepositoryRequest;
import com.version_vault.response.RepositoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RepositoryService {

    private final RepositoryRepository repositoryRepository;
    private final UserService userService;
    private final BranchService branchService;
    private final RepositoryMapper repositoryMapper;

    @Transactional
    public RepositoryResponse createRepository(CreateRepositoryRequest request, User owner) {

        // get User for the repo and validate it exists
        // validated at controller and security

        // check owner + name doesn't already exist
        if (repositoryRepository.existsByOwnerIdAndName(owner.getId(), request.getName())) {
            throw new ResourceAlreadyExistsException("Repository with name " + request.getName() + " already exists for owner id " + owner.getId());
        }

        // create repo
        Repository repository = new Repository(owner, request.getName(), request.getDescription());

        // save repo
        Repository savedRepository = repositoryRepository.save(repository);

        // create 'main' branch with head commit null
        branchService.createMainBranch(savedRepository);

        // return saved repo
        return repositoryMapper.toRepositoryResponse(savedRepository, owner);
    }

    @Transactional(readOnly = true)
    public RepositoryResponse getRepositoryById(UUID id) {
        Repository repository = repositoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Repository with id " + id + " not found"));
        User user = userService.getOriginalUserById(repository.getOwner().getId());

        return repositoryMapper.toRepositoryResponse(repository, user);
    }

    @Transactional(readOnly = true)
    public RepositoryResponse getRepositoryByOwnerAndName(User owner, String name) {
        Repository repository = repositoryRepository.findByOwnerIdAndName(owner.getId(), name)
                .orElseThrow(() -> new ResourceNotFoundException("Repository with name " + name + " not found for owner id " + owner.getId()));
        User user = userService.getOriginalUserById(repository.getOwner().getId());

        return repositoryMapper.toRepositoryResponse(repository, user);
    }

    @Transactional(readOnly = true)
    public List<RepositoryResponse> getRepositoryByOwner(User owner) {
        List<Repository> repos = repositoryRepository.findAllByOwnerId(owner.getId());

        return repos.stream()
                .map(repo -> repositoryMapper.toRepositoryResponse(repo, owner))
                .toList();
    }

}
