package com.version_vault.controller;

import com.version_vault.models.User;
import com.version_vault.dtos.request.CreateRepositoryRequest;
import com.version_vault.dtos.response.RepositoryResponse;
import com.version_vault.service.RepositoryService;
import com.version_vault.service.UserService;
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
    private final UserService userService;

    @PostMapping
    public ResponseEntity<RepositoryResponse> createRepository(@RequestBody @Valid CreateRepositoryRequest request,
                                                               @RequestHeader("Authorization") String token) {
        User owner = userService.findByJwtToken(token);
        return new ResponseEntity<>(repositoryService.createRepository(request, owner), HttpStatus.CREATED);
    }

    @GetMapping("/{repoId}")
    public ResponseEntity<RepositoryResponse> getRepositoryById(@PathVariable UUID repoId) {
        return ResponseEntity.ok(repositoryService.getRepositoryById(repoId));
    }

    @GetMapping("/all")
    public ResponseEntity<List<RepositoryResponse>> getAllRepositoryByUserId(@RequestHeader("Authorization") String token) {
        User owner = userService.findByJwtToken(token);
        return ResponseEntity.ok(repositoryService.getRepositoryByOwner(owner));
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<RepositoryResponse> getRepositoryByOwnerAndName(@RequestHeader("Authorization") String token,
                                                                          @PathVariable String name) {
        User owner = userService.findByJwtToken(token);
        return ResponseEntity.ok(repositoryService.getRepositoryByOwnerAndName(owner, name));
    }

}
