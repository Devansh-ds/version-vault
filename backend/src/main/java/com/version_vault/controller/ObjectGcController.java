package com.version_vault.controller;

import com.version_vault.dtos.response.GarbageCollectionResponse;
import com.version_vault.service.ObjectGcService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/gc")
@RequiredArgsConstructor
public class ObjectGcController {

    private final ObjectGcService objectGcService;

    @PostMapping("/scan")
    public ResponseEntity<GarbageCollectionResponse> scan() {
        return ResponseEntity.ok(objectGcService.scan());
    }

    @PostMapping("/clean")
    public ResponseEntity<GarbageCollectionResponse> collect() {
        return ResponseEntity.ok(objectGcService.collect());
    }
}