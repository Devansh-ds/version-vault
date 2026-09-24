package com.version_vault.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(
        name = "commits",
        indexes = {
                @Index(
                        name = "idx_commits_repository_created_at",
                        columnList = "repository_id, created_at"
                )
        }
)
@Getter
@Setter
public class Commit extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "repository_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_commits_repository")
    )
    private Repository repository;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "parent_commit_id",
            foreignKey = @ForeignKey(name = "fk_commits_parent")
    )
    private Commit parentCommit;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "manifest_id",
            nullable = false,
            unique = true,
            foreignKey = @ForeignKey(name = "fk_commits_manifest")
    )
    private Manifest manifest;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "author_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_commits_author")
    )
    private User author;

    @Column(
            nullable = false,
            length = 500
    )
    private String message;

    protected Commit() {
    }

    public Commit(
            Repository repository,
            Commit parentCommit,
            Manifest manifest,
            User author,
            String message
    ) {
        super(UUID.randomUUID(), java.time.Instant.now());
        this.repository = repository;
        this.parentCommit = parentCommit;
        this.manifest = manifest;
        this.author = author;
        this.message = message;
    }
}