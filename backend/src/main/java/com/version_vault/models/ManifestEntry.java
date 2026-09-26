package com.version_vault.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "manifest_entries",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_manifest_entries_manifest_path",
                        columnNames = {"manifest_id", "path"}
                )
        }
)
@Getter
@Setter
public class ManifestEntry extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "manifest_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_manifest_entries_manifest")
    )
    private Manifest manifest;

    @Column(
            nullable = false,
            length = 1000
    )
    private String path;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "object_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_manifest_entries_object")
    )
    private ObjectEntity object;

    protected ManifestEntry() {
    }

    public ManifestEntry(
            Manifest manifest,
            String path,
            ObjectEntity object
    ) {
        super(UUID.randomUUID(), Instant.now());
        this.manifest = manifest;
        this.path = path;
        this.object = object;
    }
}