# VersionVault

VersionVault is a Git-like versioned file storage system built with **Java and Spring Boot**.

The project focuses on the core ideas behind version control: immutable file objects, snapshots, commits, branches, and independent working trees. Instead of storing file contents directly inside commits, VersionVault stores content-addressed objects and uses manifests to represent the state of a repository at a particular point in time.

The goal is to understand and implement the backend architecture behind a version control system rather than build another traditional file CRUD application.

## Key Features

* **Content-addressed object storage** using SHA-256 hashes
* **Immutable commits** with parent commit relationships
* **Manifest-based snapshots** of a repository's working tree
* **Branching** from any existing commit
* **Independent working trees** for each branch
* File creation, updates, reads, and deletion
* Reuse of identical file contents through content-addressed objects
* Optimistic concurrency control when updating branch HEADs
* Local filesystem-based object storage with an abstraction that can support other storage backends later

## How It Works

A file is not stored directly inside a commit. The system separates file contents, working state, and committed history.

### File Storage

When a file is added or updated:

```text
File Content
     │
     ▼
  SHA-256
     │
     ▼
 ObjectEntity
     │
     ├── contentHash
     ├── storageKey
     └── size
          │
          ▼
     ObjectStorage
```

The actual bytes are stored on the filesystem, while PostgreSQL stores the object's metadata.

Because the object is identified by its content hash, two files containing identical content can reference the same object.

### Commits

A commit represents a snapshot of the working tree at a specific point in time:

```text
Working Tree
     │
     ▼
  Manifest
     │
     ├── README.md → Object A
     ├── src/App.java → Object B
     └── config.yml → Object C
             │
             ▼
          Commit
             │
             └── parent → previous Commit
```

The manifest is immutable once associated with a commit. This means modifying a file in a later commit does not change the files represented by earlier commits.

### Branches

A branch points to a commit and has its own working tree.

When a branch is created from a commit, its working tree is initialized from that commit's manifest:

```text
              C2
             /  \
            /    \
         main   feature
          │        │
          │        └── Working Tree
          │
          └── Working Tree
```

Both branches initially contain the same snapshot, but their working trees are independent. Changes made on one branch do not modify the other.

The underlying objects are still shared, so creating a branch does not require copying file contents.

## Concurrency

Updating a branch HEAD uses an optimistic concurrency check.

When creating a commit, the service records the branch's current HEAD and updates it only if the HEAD has not changed:

```sql
UPDATE branches
SET head_commit_id = :newCommit
WHERE id = :branchId
  AND head_commit_id = :expectedHead;
```

If no row is updated, another commit changed the branch in the meantime and the operation is rejected.

This prevents two concurrent commits from silently overwriting each other's branch HEAD.

## Architecture

The application is organized around the main domain concepts:

```text
User
 │
 └── Repository
       │
       ├── Branch
       │     │
       │     └── WorkingEntry ──→ Object
       │
       ├── Commit
       │     │
       │     └── Manifest
       │           │
       │           └── ManifestEntry ──→ Object
       │
       └── Manifest

Object
  │
  └── ObjectStorage
        │
        └── Local Filesystem
```

### Main Services

| Service              | Responsibility                               |
| -------------------- | -------------------------------------------- |
| `RepositoryService`  | Repository management                        |
| `BranchService`      | Branch creation and branch management        |
| `WorkingTreeService` | File operations on a branch                  |
| `ObjectService`      | Content hashing, object metadata and storage |
| `ManifestService`    | Creating and retrieving snapshots            |
| `CommitService`      | Creating commits and updating branch HEAD    |

The storage layer is separated behind an `ObjectStorage` abstraction. The current implementation uses the local filesystem, while the service layer does not depend directly on filesystem operations.

## Tech Stack

**Backend**

* Java
* Spring Boot
* Spring Data JPA / Hibernate
* Maven

**Database**

* PostgreSQL

**Storage**

* Local filesystem
* SHA-256 content addressing

**Development**

* IntelliJ IDEA
* Postman
* Git

## Example Workflow

A typical workflow looks like:

```text
1. Create repository
        ↓
2. Add files to main working tree
        ↓
3. Create initial commit
        ↓
4. Modify / add / delete files
        ↓
5. Create another commit
        ↓
6. Create feature branch from an existing commit
        ↓
7. Feature working tree is initialized from that commit
        ↓
8. Make independent changes on feature
        ↓
9. Create feature commit
```

For example:

```text
C1: Initial commit
 │
 ▼
C2: Added configuration
 │
 ├─────────────── main
 │
 └── feature
       │
       ▼
      C3: Updated README
```

`main` continues to point to `C2`, while `feature` points to `C3`.

## Current Status

The core MVP is implemented and tested.

### Implemented

* Repository management
* Branch management
* Working tree operations
* Content-addressed object storage
* Manifest creation
* Commit creation and history
* Branch creation from existing commits
* Branch working-tree initialization
* Branch divergence
* Historical commit snapshots
* Optimistic branch HEAD updates
* Basic validation and error handling

### Planned

Some features are intentionally outside the current MVP:

* Commit history browsing APIs
* Reading a file from an arbitrary historical commit
* Commit-to-commit diff
* Merge and conflict handling
* Remote object storage
* Garbage collection of unreachable objects
* Authentication and authorization

These will be added only where they make sense for the core version-control model.

## Running Locally

### Prerequisites

* Java 21+
* PostgreSQL
* Maven

Configure your database connection and storage location in `application.yml`.

Example storage configuration:

```yaml
application:
  storage:
    root: './storage'
```

Then start the application:

```bash
./mvnw spring-boot:run
```

On Windows:

```bash
mvnw.cmd spring-boot:run
```

The application creates its object storage directory under the configured storage root.

## Why I Built This

Most backend projects demonstrate CRUD operations around an existing business domain. VersionVault is intended to explore a different set of problems:

* How immutable versions can be represented in a relational database
* How content-addressed storage works
* How snapshots can be built from mutable working state
* How branches can share history while maintaining independent state
* How concurrent updates to a branch can be handled safely
* How database metadata can be separated from binary object storage