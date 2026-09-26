package com.version_vault.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateCommitRequest(

        @NotNull
        UUID authorId,

        @NotBlank
        @Size(min = 1, max = 500)
        String message
) {
}
