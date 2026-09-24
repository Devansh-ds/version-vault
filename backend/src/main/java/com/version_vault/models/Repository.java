package com.version_vault.models;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(
        name = "repositories",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_repositories_owner_name",
                        columnNames = {"owner_id", "name"}
                )
        }
)
public class Repository extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "owner_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_repositories_owner")
    )
    private User owner;

    @Column(
            nullable = false,
            length = 100
    )
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Instant updatedAt;

    protected  Repository() {
    }
    public Repository(User owner, String name, String description) {
        super(UUID.randomUUID(), Instant.now());
        this.owner = owner;
        this.name = name;
        this.description = description;
        this.updatedAt = Instant.now();
    }
}
