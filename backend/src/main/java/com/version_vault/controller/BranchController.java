package com.version_vault.controller;

import com.version_vault.dtos.request.RenameBranchRequest;
import com.version_vault.models.User;
import com.version_vault.dtos.request.CreateBranchRequest;
import com.version_vault.dtos.response.BranchResponse;
import com.version_vault.service.BranchService;
import com.version_vault.service.UserService;
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
    private final UserService userService;

    @PostMapping
    public ResponseEntity<BranchResponse> createBranch(@RequestBody @Valid CreateBranchRequest request,
                                                       @PathVariable UUID repoId,
                                                       @RequestHeader("Authorization") String token) {
        User owner = userService.findByJwtToken(token);
        return new ResponseEntity<>(branchService.createBranch(request, repoId, owner), HttpStatus.CREATED);
    }

    @GetMapping("/{branchId}")
    public ResponseEntity<BranchResponse> getBranchById(@PathVariable UUID branchId,
                                                        @PathVariable UUID repoId) {
        return new ResponseEntity<>(branchService.getBranchById(branchId), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<BranchResponse> getByRepoIdAndName(@PathVariable UUID repoId,
                                                             @RequestParam String branchName,
                                                             @RequestHeader("Authorization") String token) {
        User owner = userService.findByJwtToken(token);
        return ResponseEntity.ok(branchService.getBranchByRepositoryAndName(repoId, branchName, owner));
    }

    @GetMapping("/all")
    public ResponseEntity<List<BranchResponse>> getAllBranches(@PathVariable UUID repoId) {
        return ResponseEntity.ok(branchService.getBranchesByRepository(repoId));
    }

    @PutMapping("/{branchId}/rename")
    public ResponseEntity<BranchResponse> renameBranch(@PathVariable UUID repoId,
                                                       @PathVariable UUID branchId,
                                                       @RequestBody @Valid RenameBranchRequest renameBranchRequest,
                                                       @RequestHeader("Authorization") String token) {
        User owner = userService.findByJwtToken(token);
        return ResponseEntity.ok(branchService.renameBranch(repoId, branchId, renameBranchRequest, owner));
    }

    @DeleteMapping("/{branchId}")
    public ResponseEntity<Void> deleteBranch(@PathVariable UUID repoId,
                                             @PathVariable UUID branchId,
                                             @RequestHeader("Authorization") String token) {
        User owner = userService.findByJwtToken(token);
        branchService.deleteBranch(repoId, branchId, owner);
        return ResponseEntity.noContent().build();
    }

}
