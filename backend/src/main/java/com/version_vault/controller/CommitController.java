package com.version_vault.controller;

import com.version_vault.dtos.response.*;
import com.version_vault.models.User;
import com.version_vault.dtos.request.CreateCommitRequest;
import com.version_vault.service.CommitService;
import com.version_vault.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/branch/{branchId}/commit")
@RequiredArgsConstructor
public class CommitController {

    private final CommitService commitService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<CommitResponse> createCommit(@RequestBody @Valid CreateCommitRequest request,
                                                       @PathVariable UUID branchId,
                                                       @RequestHeader("Authorization") String token) {
        User author = userService.findByJwtToken(token);
        return new ResponseEntity<>(commitService.createCommit(request, branchId, author), HttpStatus.CREATED);
    }

    @GetMapping("/{commitId}")
    public ResponseEntity<CommitResponse> getCommitById(@PathVariable UUID commitId) {
        return ResponseEntity.ok(commitService.getCommit(commitId));
    }

    @GetMapping("/history")
    public ResponseEntity<CommitHistoryResponse> getBranchHistory(@PathVariable UUID branchId,
                                                                  @RequestParam(required = false) UUID cursor,
                                                                  @RequestParam(defaultValue = "20") Integer limit) {
        return ResponseEntity.ok(commitService.getBranchHistory(branchId, cursor, limit));
    }

    @GetMapping("/{commitId}/files")
    public ResponseEntity<List<HistoricalFileResponse>> getFilesAtCommit(@PathVariable UUID commitId) {
        return ResponseEntity.ok(commitService.getFilesAtCommit(commitId));
    }

    @GetMapping("/{commitId}/files/{*path}")
    public ResponseEntity<byte[]> readFileAtCommit(@PathVariable UUID commitId, @PathVariable String path) {
        path = normalizePath(path);
        byte[] content = commitService.readFileAtCommit(commitId, path);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(content.length)
                .body(content);
    }

    @GetMapping("/{commitId}/diff")
    public ResponseEntity<CommitDiffResponse> getCommitDiff(@PathVariable UUID commitId) {
        return ResponseEntity.ok(commitService.getDiff(commitId));
    }

    private String normalizePath(String path) {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("Path cannot be empty");
        }

        while (path.startsWith("/")) {
            path = path.substring(1);
        }

        return path;
    }

    @GetMapping("/{commitAId}/merge-base/{commitBId}")
    public ResponseEntity<MergeBaseResponse> findMergeBase(@PathVariable UUID commitAId,
                                                           @PathVariable UUID commitBId,
                                                           @RequestHeader("Authorization") String token) {
        User owner = userService.findByJwtToken(token);
        return ResponseEntity.ok(commitService.findMergeBase(commitAId, commitBId, owner));
    }
}
