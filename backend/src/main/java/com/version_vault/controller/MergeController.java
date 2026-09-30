package com.version_vault.controller;

import com.version_vault.dtos.request.MergeRequest;
import com.version_vault.dtos.response.MergeResponse;
import com.version_vault.models.User;
import com.version_vault.service.MergeService;
import com.version_vault.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/branch/{targetBranchId}/merge/{sourceBranchId}")
@RequiredArgsConstructor
public class MergeController {

    private final MergeService mergeService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<MergeResponse> mergeBranches(@PathVariable UUID targetBranchId,
                                                       @PathVariable UUID sourceBranchId,
                                                       @RequestBody @Valid MergeRequest request,
                                                       @RequestHeader("Authorization") String token) {
        User user = userService.findByJwtToken(token);
        return ResponseEntity.ok(mergeService.merge(targetBranchId, sourceBranchId, request, user));
    }

}