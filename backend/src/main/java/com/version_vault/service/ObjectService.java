package com.version_vault.service;

import com.version_vault.exceptions.ResourceNotFoundException;
import com.version_vault.models.ObjectEntity;
import com.version_vault.repo.ObjectRepository;
import com.version_vault.response.ObjectContent;
import com.version_vault.storage.ObjectStorage;
import com.version_vault.utils.HashUtils;
import com.version_vault.utils.StorageKeyGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ObjectService {

    private final ObjectRepository objectRepository;
    private final ObjectStorage objectStorage;

    @Transactional
    public ObjectEntity store(byte[] content) {
        String contentHash = HashUtils.sha256(content);

        Optional<ObjectEntity> existing = objectRepository.findByContentHash(contentHash);

        if (existing.isPresent()) {
            return existing.get();
        }

        String storageKey = StorageKeyGenerator.generate(contentHash);

        objectStorage.store(storageKey, content);

        ObjectEntity entity = new ObjectEntity(
                contentHash,
                storageKey,
                (long) content.length
        );

        return objectRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public byte[] readContent(UUID objectId) {

        ObjectEntity object = getById(objectId);

        return objectStorage.read(object.getStorageKey());
    }

    @Transactional(readOnly = true)
    public ObjectEntity getByContentHash(String contentHash) {

        return objectRepository
                .findByContentHash(contentHash)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Object not found: " + contentHash
                        )
                );
    }

    @Transactional(readOnly = true)
    public ObjectEntity getById(UUID objectId) {

        return objectRepository
                .findById(objectId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Object not found: " + objectId
                        )
                );
    }

    @Transactional
    public void delete(UUID objectId) {

        ObjectEntity object = getById(objectId);

        objectStorage.delete(object.getStorageKey());

        objectRepository.delete(object);
    }

    public ObjectContent getContent(UUID objectId) {

        ObjectEntity object = getById(objectId);

        byte[] content =
                objectStorage.read(object.getStorageKey());

        return new ObjectContent(
                object,
                content
        );
    }
}
