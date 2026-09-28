package com.version_vault.dtos.response;

import com.version_vault.models.ObjectEntity;

public record ObjectContent(
        ObjectEntity object,
        byte[] content
) {
}