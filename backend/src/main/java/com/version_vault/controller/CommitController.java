package com.version_vault.controller;

import com.version_vault.request.CreateCommitRequest;
import com.version_vault.response.CommitResponse;
import com.version_vault.service.CommitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/branch/{branchId}/commit")
@RequiredArgsConstructor
public class CommitController {

    private final CommitService commitService;

    @PostMapping
    public ResponseEntity<CommitResponse> createCommit(@RequestBody @Valid CreateCommitRequest request,
                                                       @PathVariable UUID branchId) {
        return new ResponseEntity<>(commitService.createCommit(request,branchId), HttpStatus.CREATED);
    }

    @GetMapping("/{commitId}")
    public ResponseEntity<CommitResponse> getCommitById(@PathVariable UUID commitId) {
        return ResponseEntity.ok(commitService.getCommit(commitId));
    }

}
