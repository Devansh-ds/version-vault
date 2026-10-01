# Commit Structure

```text
                         ┌─────────────────┐
                         │     Branch      │
                         └────────┬────────┘
                                  │
                                  │ HEAD
                                  v
                         ┌─────────────────┐
                         │      Commit     │
                         └────────┬────────┘
                                  │
                ┌─────────────────┼─────────────────┐
                │                 │                 │
                v                 v                 v
        ┌──────────────┐  ┌──────────────┐  ┌──────────────┐
        │    Parent    │  │ Second Parent│  │   Manifest   │
        │   Commit     │  │ (merge only) │  │   Snapshot   │
        └──────────────┘  └──────────────┘  └──────┬───────┘
                                                   │
                                                   v
                                          ┌──────────────────┐
                                          │ Manifest Entries │
                                          └────────┬─────────┘
                                                   │
                                             path → Object
                                                   │
                                                   v
                                            ┌────────────┐
                                            │   Object   │
                                            └────────────┘
```

---

# 5. Working Tree → Commit Flow

```text
             User modifies files
                     |
                     v
        ┌───────────────────────────┐
        │ PUT /branches/{id}/files  │
        └─────────────┬─────────────┘
                      │
                      v
             Validate branch
             + authorization
                      |
                      v
              Store file content
                      |
                      v
                 SHA-256 hash
                      |
                      v
              ┌───────────────┐
              │ Object exists?│
              └───────┬───────┘
                  Yes │   │ No
                      │   │
             ┌────────┘   └───────────┐
             v                        v
       Reuse Object          Create Object + store bytes
             │                        │
             └────────────┬───────────┘
                          v
                  Create / update
                   WorkingEntry
                          |
                          |
              User requests commit
                          |
                          v
               ┌──────────────────┐
               │ Read WorkingTree │
               └────────┬─────────┘
                        │
                        v
                Build Manifest
                        |
                        v
              Create ManifestEntries
                  path → Object
                        |
                        v
                 Create Commit
                        |
                        v
             parent = current HEAD
                        |
                        v
               Conditional HEAD
                    update
                        |
                 ┌──────┴──────┐
                 │             │
              success        failure
                 │             │
                 v             v
          HEAD → new       stale HEAD /
             commit        concurrent update
```

### Core idea

```text
File changes
     ↓
WorkingEntry
     ↓
Mutable working tree
     ↓
Commit request
     ↓
Immutable Manifest
     ↓
Commit
     ↓
Branch HEAD
```

Editing a file does not automatically create a commit.

---

# 6. Full Commit Lifecycle

```text
  1. Modify file
         |
         v
  2. Upload content
         |
         v
  3. Calculate SHA-256
         |
         v
  4. Check content hash
         |
      ┌──┴──┐
      │     │
    exists  new
      │     │
      v     v
    reuse  store
      │     │
      └──┬──┘
         v
  5. Update WorkingEntry
         |
         v
  6. Request commit
         |
         v
  7. Read WorkingEntries
         |
         v
  8. Build Manifest
         |
         v
  9. Build ManifestEntries
         |
         v
 10. Create Commit
         |
         v
 11. Conditional HEAD update
         |
      ┌──┴──┐
      │     │
    success failure
      │     │
      v     v
   New HEAD  Reject stale
```
