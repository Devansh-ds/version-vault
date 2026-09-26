package com.version_vault.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(
        name = "manifests",
        indexes = {
                @Index(
                        name = "idx_manifests_repository",
                        columnList = "repository_id"
                )
        }
)
public class Manifest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "repository_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_manifests_repository")
    )
    private Repository repository;

    protected  Manifest() {
    }

    public Manifest(Repository repository) {
        super(UUID.randomUUID(), Instant.now());
        this.repository = repository;
    }

}