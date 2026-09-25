package com.version_vault.controller;

import com.version_vault.models.ObjectEntity;
import com.version_vault.response.ObjectContent;
import com.version_vault.response.ObjectResponse;
import com.version_vault.service.ObjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/objects")
@RequiredArgsConstructor
public class ObjectController {

    private final ObjectService objectService;

    @PostMapping
    public ResponseEntity<ObjectResponse> upload(@RequestParam("file") MultipartFile file) throws IOException {
        ObjectEntity objectEntity = objectService.store(file.getBytes());

        ObjectResponse objectResponse = new ObjectResponse(
                objectEntity.getId(),
                objectEntity.getContentHash(),
                objectEntity.getStorageKey(),
                objectEntity.getSize(),
                objectEntity.getCreatedAt()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(objectResponse);
    }

    @GetMapping("/{objectId}")
    public ResponseEntity<byte[]> download(@PathVariable UUID objectId) {

        ObjectContent result =
                objectService.getContent(objectId);

        ObjectEntity object = result.object();

        return ResponseEntity.ok()
                .contentLength(object.getSize())
                .body(result.content());
    }

    @GetMapping("/{objectId}/metadata")
    public ResponseEntity<ObjectResponse> getMetadata(@PathVariable UUID objectId) {

        ObjectEntity object =
                objectService.getById(objectId);

        return ResponseEntity.ok(
                new ObjectResponse(
                        object.getId(),
                        object.getContentHash(),
                        object.getStorageKey(),
                        object.getSize(),
                        object.getCreatedAt()
                )
        );
    }

}













