package com.version_vault.controller;

import com.version_vault.response.WorkingEntryResponse;
import com.version_vault.service.WorkingTreeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/branches/{branchId}/files")
@RequiredArgsConstructor
public class WorkingTreeController {

    private final WorkingTreeService workingTreeService;

    @PutMapping("/{*path}")
    public ResponseEntity<WorkingEntryResponse> addOrUpdateFile(@PathVariable UUID branchId,
                                                                @PathVariable String path,
                                                                @RequestParam("file")MultipartFile file
    ) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        path = normalizePath(path);

        WorkingEntryResponse response = workingTreeService.addOrUpdateFile(branchId, path, file.getBytes());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{*path}")
    public ResponseEntity<byte[]> readFile(@PathVariable UUID branchId, @PathVariable String path) {
        path = normalizePath(path);

        byte[] content =  workingTreeService.readFile(branchId, path);

        return ResponseEntity.ok()
                .contentLength(content.length)
                .body(content);
    }

    @GetMapping
    public ResponseEntity<List<WorkingEntryResponse>> getAllFiles(@PathVariable UUID branchId) {
        return ResponseEntity.ok(workingTreeService.listFiles(branchId));
    }

    @DeleteMapping("/{*path}")
    public ResponseEntity<Void> deleteFile(@PathVariable UUID branchId, @PathVariable String path) {
        path = normalizePath(path);

        workingTreeService.deleteFile(branchId, path);
        return ResponseEntity.noContent().build();
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

}