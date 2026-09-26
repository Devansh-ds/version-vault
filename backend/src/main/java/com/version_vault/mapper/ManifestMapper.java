package com.version_vault.mapper;

import com.version_vault.models.Manifest;
import com.version_vault.models.ManifestEntry;
import com.version_vault.response.ManifestEntryResponse;
import com.version_vault.response.ManifestResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ManifestMapper {

    public ManifestResponse toManifestResponse(Manifest manifest, List<ManifestEntry> entries) {
        List<ManifestEntryResponse> entryResponses = entries.stream()
                .map(this::toManifestEntryResponse)
                .toList();

        return new ManifestResponse(
                manifest.getId(),
                manifest.getRepository().getId(),
                manifest.getCreatedAt(),
                entryResponses
        );
    }

    public ManifestEntryResponse toManifestEntryResponse(ManifestEntry entry) {
        return new ManifestEntryResponse(
                entry.getId(),
                entry.getManifest().getId(),
                entry.getPath(),
                entry.getObject().getId(),
                entry.getObject().getSize()
        );
    }

}
