package com.version_vault.controller;

import com.version_vault.request.CreateRepositoryRequest;
import com.version_vault.response.RepositoryResponse;
import com.version_vault.service.RepositoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/repo")
@RequiredArgsConstructor
public class RepositoryController {

    private final RepositoryService repositoryService;

    @PostMapping("/user/{userId}")
    public ResponseEntity<RepositoryResponse> createRepository(@RequestBody @Valid CreateRepositoryRequest request,
                                                               @PathVariable UUID userId) {
        return new ResponseEntity<>(repositoryService.createRepository(request, userId), HttpStatus.CREATED);
    }

    @GetMapping("/{repoId}")
    public ResponseEntity<RepositoryResponse> getRepositoryById(@PathVariable UUID repoId) {
        return ResponseEntity.ok(repositoryService.getRepositoryById(repoId));
    }

    @GetMapping("/user/{userId}/all")
    public ResponseEntity<List<RepositoryResponse>> getAllRepositoryByUserId(@PathVariable UUID userId) {
        return ResponseEntity.ok(repositoryService.getRepositoryByOwner(userId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<RepositoryResponse> getRepositoryByOwnerAndName(@PathVariable UUID userId,
                                                                          @RequestParam String name) {
        return ResponseEntity.ok(repositoryService.getRepositoryByOwnerAndName(userId, name));
    }

}
