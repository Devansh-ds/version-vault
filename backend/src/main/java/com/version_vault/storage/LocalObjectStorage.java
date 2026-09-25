package com.version_vault.storage;

import com.version_vault.configs.StorageProperties;
import com.version_vault.exceptions.ObjectStorageException;
import com.version_vault.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;

@Component
public class LocalObjectStorage implements  ObjectStorage {

    private final Path rootPath;

    public LocalObjectStorage(StorageProperties storageProperties) {

        this.rootPath = Paths.get(storageProperties.getRoot())
                .toAbsolutePath()
                .normalize();

        System.out.println("======================================");
        System.out.println("Object storage root: " + this.rootPath);
        System.out.println("======================================");
    }

    @Override
    public void store(String storageKey, byte[] content) {
        Path objectPath = resolvePath(storageKey);

        try {
            Files.createDirectories(objectPath.getParent());

            Files.write(
                    objectPath,
                    content,
                    StandardOpenOption.CREATE_NEW
            );

        } catch (FileAlreadyExistsException e) {
            // Object already exists.
            System.out.println("-------------------------- File " + objectPath + " already exists " + "--------------------------");

        } catch (IOException e) {
            throw new ObjectStorageException(
                    "Failed to store object: " + storageKey
            );
        }
    }

    @Override
    public byte[] read(String storageKey) {
        Path objectPath = resolvePath(storageKey);

        try {
            if (!Files.exists(objectPath)) {
                throw new ResourceNotFoundException(
                        "Object not found in storage: " + storageKey
                );
            }

            return Files.readAllBytes(objectPath);

        } catch (IOException e) {
            throw new ObjectStorageException(
                    "Failed to read object: " + storageKey
            );
        }
    }

    @Override
    public void delete(String storageKey) {
        Path objectPath = resolvePath(storageKey);

        try {
            Files.deleteIfExists(objectPath);

        } catch (IOException e) {
            throw new ObjectStorageException(
                    "Failed to delete object: " + storageKey
            );
        }
    }

    private Path resolvePath(String storageKey) {
        Path path = rootPath.resolve(storageKey)
                .normalize();

        if (!path.startsWith(rootPath)) {
            throw new IllegalArgumentException(storageKey + " is not a valid path");
        }
        return path;
    }
}
