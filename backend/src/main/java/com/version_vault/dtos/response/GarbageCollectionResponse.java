package com.version_vault.dtos.response;

public record GarbageCollectionResponse(
        int totalObjects,
        int reachableObjects,
        int garbageObjects,
        int eligibleObjects,
        int protectedObjects,
        int deletedObjects,
        long reclaimedBytes
) {
}