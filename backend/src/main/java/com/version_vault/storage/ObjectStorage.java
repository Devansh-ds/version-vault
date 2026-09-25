package com.version_vault.storage;

public interface ObjectStorage {

    void store(String storageKey, byte[] content);

    byte[] read(String storageKey);

    void delete(String storageKey);

}

