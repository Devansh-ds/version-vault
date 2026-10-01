package com.version_vault.configs;

import com.version_vault.storage.LocalObjectStorage;
import com.version_vault.storage.ObjectStorage;
import com.version_vault.storage.S3ObjectStorage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class StorageConfiguration {

    @Bean
    @ConditionalOnProperty(
            name = "application.storage.type",
            havingValue = "local",
            matchIfMissing = true
    )
    public ObjectStorage localObjectStorage(StorageProperties storageProperties) {
        return new LocalObjectStorage(storageProperties);
    }

    @Bean
    @ConditionalOnProperty(
            name = "application.storage.type",
            havingValue = "s3"
    )
    public ObjectStorage s3ObjectStorage(StorageProperties storageProperties, S3Client s3Client) {
        return new S3ObjectStorage(s3Client, storageProperties);
    }
}