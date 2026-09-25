package com.version_vault.utils;

public final class StorageKeyGenerator {

    private StorageKeyGenerator() {
    }

    public static String generate(String contentHash) {

        return "objects/"
                + contentHash.substring(0, 2)
                + "/"
                + contentHash.substring(2, 4)
                + "/"
                + contentHash;
    }
}
