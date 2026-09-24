package com.version_vault.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "objects",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_objects_content_hash", columnNames = "content_hash"),
                @UniqueConstraint(name = "uk_objects_storage_key", columnNames = "storage_key")
        }
)
@Getter
@Setter
public class ObjectEntity extends BaseEntity {

    @Column(nullable = false,
            updatable = false,
            name = "content_hash",
            length = 64)
    private String contentHash;

    @Column(name = "storage_key",
            length = 500,
            nullable = false,
            updatable = false)
    private String storageKey;

    @Column(nullable = false,
            updatable = false)
    private Long size;

    protected ObjectEntity() {}

    public ObjectEntity(String contentHash, String storageKey, Long size) {
        this.contentHash = contentHash;
        this.storageKey = storageKey;
        this.size = size;
    }

}
