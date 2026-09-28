package com.version_vault.controller;

import com.version_vault.models.User;
import com.version_vault.request.CreateCommitRequest;
import com.version_vault.response.CommitHistoryResponse;
import com.version_vault.response.CommitResponse;
import com.version_vault.response.HistoricalFileResponse;
import com.version_vault.service.CommitService;
import com.version_vault.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

}
