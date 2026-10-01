package com.version_vault.storage;

import com.version_vault.configs.StorageProperties;
import com.version_vault.exceptions.ObjectStorageException;
import com.version_vault.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@ConditionalOnProperty(
        name = "application.storage.type",
        havingValue = "s3"
)
@RequiredArgsConstructor
public class S3ObjectStorage implements ObjectStorage {

    private final S3Client s3Client;
    private final StorageProperties storageProperties;

    @Override
    public void store(String storageKey, byte[] content) {

        try {

            PutObjectRequest request = PutObjectRequest.builder()
                            .bucket(storageProperties.getS3().getBucket())
                            .key(storageKey)
                            .contentLength((long) content.length)
                            .build();

            s3Client.putObject(request, RequestBody.fromBytes(content));

        } catch (S3Exception e) {
            throw new ObjectStorageException("Failed to store object in S3: " + storageKey);
        }
    }

    @Override
    public byte[] read(String storageKey) {

        try {

            GetObjectRequest request = GetObjectRequest.builder()
                            .bucket(storageProperties.getS3().getBucket())
                            .key(storageKey)
                            .build();

            ResponseBytes<GetObjectResponse> response = s3Client.getObjectAsBytes(request);

            return response.asByteArray();

        } catch (NoSuchKeyException e) {
            throw new ResourceNotFoundException("Object not found in S3: " + storageKey);
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                throw new ResourceNotFoundException("Object not found in S3: " + storageKey);
            }
            throw new ObjectStorageException("Failed to read object from S3: " + storageKey);
        }
    }

    @Override
    public void delete(String storageKey) {

        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                            .bucket(storageProperties.getS3().getBucket())
                            .key(storageKey)
                            .build();

            s3Client.deleteObject(request);
        } catch (S3Exception e) {
            throw new ObjectStorageException("Failed to delete object from S3: " + storageKey);
        }
    }

}