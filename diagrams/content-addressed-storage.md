# Content-Addressed Object Storage

```text
                         File Content
                              |
                              v
                         ┌─────────┐
                         │ SHA-256 │
                         └────┬────┘
                              |
                              v
                        Content Hash
                              |
                              v
                    ┌──────────────────┐
                    │ Object by hash   │
                    │ already exists?  │
                    └────────┬─────────┘
                         Yes │   │ No
                             │   │
                    ┌────────┘   └──────────┐
                    v                       v
              Reuse Object          Create ObjectEntity
                                            |
                                            v
                                      Storage Key
                                            |
                                            v
                                     ObjectStorage
                                            |
                              ┌─────────────┴─────────────┐
                              │                           │
                              v                           v
                         Local Filesystem              AWS S3
```

### Object identity

```text
Object
 ├── contentHash
 ├── storageKey
 ├── size
 └── createdAt
```

The content hash gives the object its content-based identity.

---

# 8. Content Deduplication

```text
             README.md
                 |
                 v
          "hello world"
                 |
                 v
             SHA-256
                 |
                 v
              ABC123
                 |
                 v
          ┌─────────────┐
          │   Object A  │
          │  ABC123     │
          └─────────────┘
                 ^
                 |
                 |
             SHA-256
                 ^
                 |
          "hello world"
                 ^
                 |
              COPY.md
```

Instead of:

```text
README.md → Object A
COPY.md   → Object B
```

VersionVault can have:

```text
README.md ──┐
            ├──→ Object A (ABC123)
COPY.md  ───┘
```

The same immutable object can be referenced by multiple files, commits, and branches.

---

# 9. Local Storage vs S3

```text
                         ObjectService
                              |
                              v
                     ┌──────────────────┐
                     │  ObjectStorage   │
                     │    Interface     │
                     └────────┬─────────┘
                              |
                 ┌────────────┴────────────┐
                 │                         │
                 v                         v
       ┌──────────────────┐       ┌──────────────────┐
       │LocalObjectStorage│       │ S3ObjectStorage  │
       └─────────┬────────┘       └─────────┬────────┘
                 │                          │
                 v                          v
       ./storage/objects/...           AWS S3 Bucket
```

### Architectural point

```text
Higher-level services
        |
        v
   ObjectStorage
        |
   ┌────┴────┐
   v         v
 Local       S3
```
