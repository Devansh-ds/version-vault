package com.version_vault.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateCommitRequest(

        @NotBlank
        @Size(min = 1, max = 500)
        String message
) {
}
