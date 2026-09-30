package com.version_vault.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MergeRequest(

        @NotBlank
        @Size(max = 200)
        String message
) {
}
