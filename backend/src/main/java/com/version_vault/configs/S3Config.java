package com.version_vault.configs;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@ConditionalOnProperty(
        name = "application.storage.type",
        havingValue = "s3"
)
public class S3Config {

    @Bean
    public S3Client s3Client(StorageProperties storageProperties) {
        return S3Client.builder()
                .region(Region.of(storageProperties.getS3().getRegion()))
                .build();
    }
}