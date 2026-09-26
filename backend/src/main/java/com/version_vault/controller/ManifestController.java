package com.version_vault.controller;

import com.version_vault.response.ManifestResponse;
import com.version_vault.service.ManifestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/repo/{repoId}/branch/{branchId}/manifest")
@RequiredArgsConstructor
public class ManifestController {

    private final ManifestService manifestService;

    @PostMapping
    public ResponseEntity<ManifestResponse> createManifest(
            @PathVariable UUID repoId,
            @PathVariable UUID branchId
            ) {
        return new ResponseEntity<>(manifestService.createManifest(branchId), HttpStatus.CREATED);
    }

    @GetMapping("/{manifestId}")
    public ResponseEntity<ManifestResponse> getManifest(@PathVariable UUID manifestId) {
        return new ResponseEntity<>(manifestService.getManifest(manifestId), HttpStatus.OK);
    }

}