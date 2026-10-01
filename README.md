# VersionVault

**VersionVault** is a Git-like versioned file storage backend built with **Java, Spring Boot, PostgreSQL, and content-addressed object storage**.

It models the core mechanics behind a version control system: repositories, branches, mutable working trees, immutable commits, manifests, historical file retrieval, commit diffs, merge-base detection, three-way merges, and reachability-based garbage collection.

---

## Features

### Version Control

* Repository and branch management
* Independent working trees per branch
* Immutable commit snapshots
* Parent and two-parent merge commits
* Commit history with cursor-based pagination
* Historical file listing and retrieval
* Commit-to-parent diffs
* Branch divergence and merge-base detection
* Fast-forward merges
* Three-way merges
* Conflict detection

### Object Storage

* SHA-256 content-addressed storage
* Object deduplication
* Separate metadata and file-content storage
* Local filesystem storage
* AWS S3 storage
* Configurable storage backend through application configuration
* Path-safe local object storage

### Security

* JWT-based authentication
* Password hashing
* Authenticated write operations
* Repository ownership authorization
* System-administrator-only garbage collection
* Stateless Spring Security configuration

### Maintenance

* Reachability-based object garbage collection
* Merge-aware commit graph traversal
* Grace-period protection for recently created objects
* Working-tree protection during garbage collection
* Dry-run garbage collection scan

### Deployment

* Dockerized Spring Boot application
* Multi-stage Docker build
* Docker Compose development environment
* PostgreSQL container
* Persistent PostgreSQL volume
* Persistent local object storage
* Environment-based configuration
* Flyway database migrations
* Configurable upload limits

---

## Architecture

At a high level, VersionVault separates repository metadata, mutable working state, immutable history, and binary object storage.

```text
                         Client
                           |
                           v
                  ┌─────────────────┐
                  │   Spring Boot   │
                  │     REST API    │
                  └────────┬────────┘
                           |
          ┌────────────────┼─────────────────┐
          |                |                 |
          v                v                 v
   Repository/Branch   Commit/Merge     Object Service
   Working Tree             |                |
          |                 |                v
          |                 |         ┌──────────────┐
          |                 |         │ ObjectStorage│
          |                 |         └──────┬───────┘
          |                 |                |
          |                 |        ┌───────┴───────┐
          |                 |        |               |
          v                 v        v               v
      PostgreSQL       Commit DAG  Local            S3
                                  Storage          Bucket
```

The application uses PostgreSQL for metadata and object references while the actual file bytes are stored through the `ObjectStorage` abstraction.

---

## Core Data Model

```text
User
 └── Repository
      |
      ├── Branch
      |    |
      │    └── WorkingEntry ──> Object
      │
      ├── Commit
      |    |
      │    └── Manifest
      |         |
      │         └── ManifestEntry ──> Object
      │
      └── ...

Object
 └── ObjectStorage
      ├── LocalObjectStorage
      └── S3ObjectStorage
```

### Working Tree vs Commit

A branch's working tree is mutable:

```text
Branch
  |
  └── Working Entries
        |
        ├── README.md → Object A
        ├── src/App.java → Object B
        └── config.yml → Object C
```

Creating a commit converts the current working state into an immutable manifest:

```text
Working Tree
     |
     v
  Manifest
     |
     v
   Commit
     |
     └── parent → previous Commit
```

Later modifications affect the working tree and future manifests, not previous commits.

---

## Content-Addressed Object Storage

VersionVault does not store file bytes directly inside commits or manifests.

When content is uploaded:

```text
File Content
     |
     v
   SHA-256
     |
     v
Content Hash
     |
     v
 ObjectEntity
     |
     └── storageKey
            |
            v
       ObjectStorage
```

The database stores object metadata such as:

* Content hash
* Storage key
* Size
* Creation timestamp

The actual bytes are stored through the configured storage backend.

### Deduplication

Because objects are identified by their SHA-256 content hash, identical content can be reused:

```text
file A ─────┐
            ├──────→ Object X
file B ─────┘
```

This avoids storing the same file content multiple times.

---

## Branching

Branches point to commits and maintain their own working trees.

When a branch is created from an existing commit, its working tree is initialized from that commit's manifest.

```text
             C2
            /  \
           /    \
        main   feature
         |        |
         |        └── Working Tree B
         |
         └── Working Tree A
```

The branches initially reference the same underlying objects, but subsequent working-tree changes remain independent.

This means creating a branch does not require copying the actual file contents.

---

## Commit History

Commits form a directed history graph.

Normal commits have one parent:

```text
C1 → C2 → C3
```

Merge commits can have two parents:

```text
          C2
         /  \
        C3   C4
         \   /
          C5
```

For the merge commit:

```text
C5.parent       = C3
C5.secondParent = C4
```

The second parent allows the system to preserve both sides of a merge in the commit graph.

---

## Diffs

VersionVault computes commit diffs by comparing the manifests of a commit and its parent.

Files are classified as:

```text
ADDED
MODIFIED
DELETED
```

The diff operates on file paths and object identities/content hashes rather than comparing raw file bytes.

For example:

```text
Commit A                 Commit B

README.md → Object A    README.md → Object B
config.yml → Object C   config.yml → Object C
old.txt → Object D      new.txt → Object E
```

The resulting changes can identify:

```text
README.md  → MODIFIED
old.txt    → DELETED
new.txt    → ADDED
```

---

## Three-Way Merge

VersionVault uses a three-way merge based on:

```text
             Base
            /    \
         Ours   Theirs
```

For each path, the system compares the base, target, and source versions.

The merge handles cases such as:

* Only source changed → source version
* Only target changed → target version
* Both sides have the same version → shared version
* Neither side changed → base version
* Both sides changed differently → conflict

Supported merge outcomes include:

```text
UP_TO_DATE
FAST_FORWARD
MERGED
CONFLICT
```

A successful non-fast-forward merge creates a merge commit with two parents.

---

## Merge-Base Detection

Before performing a three-way merge, VersionVault determines the common ancestor of the two branch heads.

The commit graph can contain both normal parent edges and second-parent edges from merge commits.

```text
             C1
            /  \
           C2   C3
            \   /
             C4
```

The merge-base allows the system to determine which changes were introduced independently on each side.

---

## Garbage Collection

Objects can become unreachable from the current branch history.

VersionVault uses reachability-based garbage collection:

```text
Branch HEADs
     |
     v
 Commit DAG
     |
     v
 Manifests
     |
     v
 Objects
```

An object is considered reachable when it is referenced by a manifest belonging to a commit reachable from a branch HEAD.

The traversal follows both:

```text
parentCommit
secondParentCommit
```

so objects referenced by merge history are preserved.

### Grace Period

Unreachable objects are not immediately deleted.

A configurable grace period protects recently created objects:

```text
Object
  |
  ├── Reachable → keep
  |
  └── Unreachable
        |
        ├── Within grace period → protect
        |
        └── Past grace period
              |
              ├── Used by working tree → protect
              |
              └── Otherwise → eligible for deletion
```

Garbage collection supports:

```text
POST /gc/scan
```

for a dry-run reachability analysis and:

```text
POST /gc/clean
```

for actual cleanup.

These operations are restricted to the system administrator.

---

## Optimistic Concurrency Control

Branch HEAD updates use an optimistic concurrency check.

Conceptually:

```sql
UPDATE branches
SET head_commit_id = :newHead
WHERE id = :branchId
  AND head_commit_id = :expectedHead;
```

If no row is updated, the branch HEAD changed after the operation read it.

This prevents concurrent commit operations from silently overwriting one another's branch HEAD.

This project keeps the concurrency model focused on branch-head consistency rather than attempting to provide full transactional coordination between every filesystem and database operation.

---

## Storage Backends

VersionVault separates object management from physical storage through:

```java
ObjectStorage
```

Current implementations:

```text
ObjectStorage
    |
    ├── LocalObjectStorage
    |
    └── S3ObjectStorage
```

The selected backend is controlled through configuration:

```yaml
application:
  storage:
    type: local
```

or:

```yaml
application:
  storage:
    type: s3
```

The same object-management logic works with either backend.

---

## Authentication & Authorization

The API uses JWT-based authentication with Spring Security.

Authentication endpoints include:

```text
POST /auth/register
POST /auth/authenticate
```

Authenticated operations use a bearer token:

```text
Authorization: Bearer <JWT>
```

Repository mutations verify the authenticated user against repository ownership.

Garbage collection is restricted to the system administrator.

---

## API Documentation

Swagger/OpenAPI documentation is available when the application is running.

```text
http://localhost:8080/swagger-ui/index.html
```

OpenAPI JSON:

```text
http://localhost:8080/api-docs
```

The project keeps internal/test-only endpoints available for development while hiding them from the public Swagger documentation.

---

## Tech Stack

### Backend

* Java 21
* Spring Boot
* Spring Web
* Spring Data JPA
* Hibernate
* Spring Security
* Bean Validation
* Lombok

### Database

* PostgreSQL
* Flyway

### Storage

* Local filesystem
* AWS S3
* AWS SDK for Java
* SHA-256 content addressing

### API Documentation

* Springdoc OpenAPI / Swagger UI

### Deployment

* Docker
* Docker Compose

### Development & Testing

* Maven
* Postman
* Git
* IntelliJ IDEA

---

# Running Locally

## Prerequisites

For the non-Docker setup:

* Java 21+
* PostgreSQL
* Maven

For the Docker setup:

* Docker
* Docker Compose

---

## Option 1 — Docker Compose

The recommended local setup is Docker Compose.

The Compose environment contains:

```text
Docker Compose
│
├── PostgreSQL
│
└── VersionVault Backend
```

### 1. Configure environment variables

Create a `.env` file in the project root:

```env
JWT_SECRET_KEY=your-secret-key
```

The `.env` file should not be committed to Git.

### 2. Start the application

From the project root:

```bash
docker compose up --build
```

The backend will be available at:

```text
http://localhost:8080
```

PostgreSQL runs on:

```text
localhost:5432
```

The database is:

```text
vault
```

Flyway automatically applies the database migrations when the application starts.

### 3. Stop the environment

```bash
docker compose down
```

The PostgreSQL data is persisted through the Docker volume.

To intentionally remove the database volume and recreate the database from migrations:

```bash
docker compose down -v
docker compose up --build
```

---

# Local Filesystem Storage

The Docker Compose configuration uses local object storage by default.

```yaml
application:
  storage:
    type: local
```

The host directory:

```text
./storage
```

is mounted into the backend container as:

```text
/app/storage
```

Objects are stored under:

```text
storage/objects/
```

The storage directory is persisted independently from the backend container.

---

# Using AWS S3 Storage

VersionVault can use S3 instead of the local filesystem.

Set:

```yaml
application:
  storage:
    type: s3
    s3:
      bucket: your-bucket-name
      region: ap-south-1
```

or configure the equivalent environment variables:

```env
STORAGE_TYPE=s3
S3_BUCKET=your-bucket-name
AWS_REGION=ap-south-1
```

AWS credentials are provided through the standard AWS SDK credential configuration.

No application code changes are required to switch from local storage to S3.

---

# Configuration

Important configuration values can be supplied through environment variables.

| Variable                   | Purpose                   | Default                                  |
| -------------------------- | ------------------------- |------------------------------------------|
| `DB_URL`                   | PostgreSQL JDBC URL       | `jdbc:postgresql://localhost:5432/vault` |
| `DB_USERNAME`              | Database username         | `postgres`                               |
| `DB_PASSWORD`              | Database password         | `password`                               |
| `STORAGE_TYPE`             | `local` or `s3`           | `local`                                  |
| `STORAGE_ROOT`             | Local object-storage root | `./storage`                              |
| `S3_BUCKET`                | S3 bucket name            | —                                        |
| `AWS_REGION`               | AWS region                | `ap-south-1`                             |
| `JWT_SECRET_KEY`           | JWT signing secret        | —                                        |
| `JWT_EXPIRATION`           | JWT lifetime              | `86400000` (1 day)                       |
| `REFRESH_TOKEN_EXPIRATION` | Refresh-token lifetime    | `604800000`                              |
| `MAX_FILE_SIZE`            | Maximum individual upload | `5MB`                                    |
| `MAX_REQUEST_SIZE`         | Maximum multipart request | `6MB`                                    |
| `GC_GRACE_PERIOD_HOURS`    | GC grace period           | `1`                                      |

For anything beyond local development, sensitive values such as database passwords and JWT secrets should be supplied through the deployment environment rather than committed configuration.

---

# Upload Limits

The application limits uploads to:

```text
Maximum file size:    5 MB
Maximum request size: 6 MB
```

These values are configurable through:

```env
MAX_FILE_SIZE
MAX_REQUEST_SIZE
```

---

# Project Structure

```text
VersionVault/
│
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/version_vault/
│   │   │   │   ├── controller/
│   │   │   │   ├── service/
│   │   │   │   ├── models/
│   │   │   │   ├── repo/
│   │   │   │   ├── dtos/
│   │   │   │   ├── mapper/
│   │   │   │   ├── storage/
│   │   │   │   ├── security/
│   │   │   │   ├── configs/
│   │   │   │   ├── exceptions/
│   │   │   │   └── utils/
│   │   │   │
│   │   │   └── resources/
│   │   │       ├── application.yaml
│   │   │       └── db/migration/
│   │   │
│   │   └── test/
│   │
│   └── Dockerfile
│
├── postman/
│   └── postman_collection.json
│
├── storage/
│   └── objects/
│
├── docker-compose.yml
├── .env
└── README.md
```

---

# Example Workflow

A typical VersionVault workflow is:

```text
Register / Authenticate
        |
        v
Create Repository
        |
        v
Create / use main branch
        |
        v
Add or update files
        |
        v
Create Commit
        |
        v
Create Feature Branch
        |
        v
Make Independent Changes
        |
        v
Create Feature Commit
        |
        v
Find Merge Base
        |
        v
Three-Way Merge
        |
        v
Merge Commit
```

Historical commits remain readable even after later changes because committed manifests and objects are immutable.

---

# Engineering Decisions

### Immutable history

Commits references immutable manifests instead of the mutable working tree. This keeps historical snapshots stable.

### Content-addressed storage

SHA-256 content addressing allows identical file contents to be stored once and referenced multiple times.

### Storage abstraction

The `ObjectStorage` interface separates object-management logic from the physical storage backend, allowing local filesystem and S3 implementations.

### Relational metadata + object storage

PostgreSQL stores relationships and metadata, while object storage handles file bytes.

### Optimistic branch updates

Conditional branch HEAD updates prevent concurrent commits from silently replacing each other's history.

### Two-parent merge commits

Merge commits preserve both sides of a merge in the commit graph, enabling future history traversal and garbage-collection reachability analysis.

### Reachability-based garbage collection

Objects are retained when reachable from any branch's commit history. A grace period and working-tree protection reduce the risk of deleting objects that are still needed.

---

# Known Limitations

VersionVault intentionally focuses on the core backend mechanics rather than attempting to reproduce the entire Git feature set.

Current limitations include:

* Local/S3 object storage rather than a distributed object-storage service
* No remote repository synchronization
* No merge-conflict resolution workflow; conflicts are detected and returned
* Object storage and PostgreSQL updates are not globally atomic
* Concurrent working-tree modifications use the application's current consistency model rather than full file-level locking
* No Kubernetes or multi-instance deployment configuration
