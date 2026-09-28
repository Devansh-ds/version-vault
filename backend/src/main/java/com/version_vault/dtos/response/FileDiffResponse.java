package com.version_vault.dtos.response;

import com.version_vault.dtos.DiffStatus;

import java.util.UUID;

public record FileDiffResponse(
        String path,
        DiffStatus status,
        UUID oldObjectId,
        UUID newObjectId
) {
}