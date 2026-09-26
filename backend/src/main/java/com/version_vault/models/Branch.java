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
        name = "branches",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_branches_repository_name",
                        columnNames = {"repository_id", "name"}
                )
        }
)
public class Branch extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY,  optional = false)
    @JoinColumn(
            name = "repository_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_branches_repository")
    )
    private Repository repository;

    @Column(
            nullable = false,
            length = 100
    )
    private String name;

    @ManyToOne(fetch = FetchType.LAZY,  optional = false)
    @JoinColumn(
            name = "head_commit_id",
            foreignKey = @ForeignKey(name = "fk_branches_head_commit")
    )
    private Commit headCommit;

    @Column(nullable = false)
    private Instant updatedAt;

    protected Branch() {}

    public Branch(Repository repository, String name, Commit headCommit) {
        super(UUID.randomUUID(), Instant.now());
        this.repository = repository;
        this.name = name;
        this.headCommit = headCommit;
        this.updatedAt = Instant.now();
    }

    public void setHeadCommit(Commit headCommit) {
        this.headCommit = headCommit;
        this.updatedAt = Instant.now();
    }
}