package com.version_vault.controller;

import com.version_vault.request.CreateBranchRequest;
import com.version_vault.response.BranchResponse;
import com.version_vault.service.BranchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/repo/{repoId}/branch")
@RequiredArgsConstructor
public class BranchController {

    private final BranchService branchService;

    @PostMapping
    public ResponseEntity<BranchResponse> createBranch(@RequestBody @Valid CreateBranchRequest request,
                                                       @PathVariable UUID repoId) {
        return new ResponseEntity<>(branchService.createBranch(request, repoId), HttpStatus.CREATED);
    }

    @GetMapping("/{branchId}")
    public ResponseEntity<BranchResponse> getBranchById(@PathVariable UUID branchId,
                                                        @PathVariable UUID repoId) {
        return new ResponseEntity<>(branchService.getBranchById(branchId), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<BranchResponse> getByRepoIdAndName(@PathVariable UUID repoId,
                                                             @RequestParam String branchName) {
        return ResponseEntity.ok(branchService.getBranchByRepositoryAndName(repoId, branchName));
    }

    @GetMapping("/all")
    public ResponseEntity<List<BranchResponse>> getAllBranches(@PathVariable UUID repoId) {
        return ResponseEntity.ok(branchService.getBranchesByRepository(repoId));
    }

}
