package com.version_vault.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "working_entries",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_working_entries_branch_path",
                        columnNames = {"branch_id", "path"}
                )
        }
)
@Getter
@Setter
public class WorkingEntry extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "branch_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_working_entries_branch")
    )
    private Branch branch;

    @Column(
            nullable = false,
            length = 1000
    )
    private String path;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "object_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_working_entries_object")
    )
    private ObjectEntity object;

    @Column(nullable = false)
    private Instant updatedAt;

    protected WorkingEntry() {
    }

    public WorkingEntry(Branch branch, String path, ObjectEntity object) {
        super(UUID.randomUUID(), Instant.now());
        this.branch = branch;
        this.path = path;
        this.object = object;
        this.updatedAt = Instant.now();
    }

    public void setObject(ObjectEntity object) {
        this.object = object;
        this.updatedAt = Instant.now();
    }

    public void setPath(String path) {
        this.path = path;
        this.updatedAt = Instant.now();
    }
}