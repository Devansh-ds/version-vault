package com.version_vault.service;

import com.version_vault.models.Branch;
import com.version_vault.models.Repository;
import com.version_vault.repo.BranchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BranchService {

    private final BranchRepository branchRepository;

    public void createMainBranch(Repository repository) {
        Branch branch = new Branch(repository, "main", null);
        branchRepository.save(branch);
    }

}
