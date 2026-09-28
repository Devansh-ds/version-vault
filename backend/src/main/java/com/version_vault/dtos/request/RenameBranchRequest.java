package com.version_vault.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RenameBranchRequest(

        @NotBlank(message = "branch name can't be null")
        @Size(max = 100, message = "Branch name cannot exceed 100 chars")
        String name
) {
}
