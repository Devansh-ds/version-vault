package com.version_vault.exceptions;

import java.util.HashMap;

public record ErrorResponse(
        HashMap<String, String> errors
) {
}
