package com.version_vault.controller;

import com.version_vault.dtos.response.GarbageCollectionResponse;
import com.version_vault.models.User;
import com.version_vault.service.ObjectGcService;
import com.version_vault.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/gc")
@RequiredArgsConstructor
public class ObjectGcController {

    private final ObjectGcService objectGcService;
    private final UserService userService;

    @PostMapping("/scan")
    public ResponseEntity<GarbageCollectionResponse> scan(@RequestHeader("Authorization") String token) {
        User user = userService.findByJwtToken(token);
        System.out.println(user.getUsername() + ", " + user.getEmail());
        userService.validateSystemAdmin(user);

        return ResponseEntity.ok(objectGcService.scan());
    }

    @PostMapping("/clean")
    public ResponseEntity<GarbageCollectionResponse> collect(@RequestHeader("Authorization") String token) {
        User user = userService.findByJwtToken(token);
        userService.validateSystemAdmin(user);

        return ResponseEntity.ok(objectGcService.collect());
    }
}