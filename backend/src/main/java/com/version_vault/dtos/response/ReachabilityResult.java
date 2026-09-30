package com.version_vault.dtos.response;

import java.util.Set;
import java.util.UUID;

public record ReachabilityResult(
        Set<UUID> reachableCommitIds,
        Set<UUID> reachableManifestIds,
        Set<UUID> reachableObjectIds
) {
}