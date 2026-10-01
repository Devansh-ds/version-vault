# Garbage Collection

```text
                    All Branches
                         |
                         v
                   Branch HEADs
                         |
                         v
                  Commit Graph
                         |
              ┌──────────┴──────────┐
              │                     │
              v                     v
        parentCommit         secondParentCommit
              │                     │
              └──────────┬──────────┘
                         v
                Reachable Commits
                         |
                         v
                   Reachable
                   Manifests
                         |
                         v
                Reachable Objects
                         |
                         v
                 ┌──────────────┐
                 │ Compare with │
                 │ all Objects  │
                 └──────┬───────┘
                        |
                        v
                 Unreachable Objects
                        |
                        v
                  Apply Grace Period
                        |
                  ┌─────┴─────┐
                  │           │
               protected   eligible
                  │           │
                  v           v
                 KEEP       DELETE
```

### Reachability model

```text
Branch HEAD
     |
     v
   Commit
     |
     v
  Manifest
     |
     v
ManifestEntry
     |
     v
  Object
```

For merge commits:

```text
                  Commit
                 /      \
                v        v
       parentCommit   secondParentCommit
             |              |
             v              v
          history        history
```

Therefore GC must traverse both parent directions.

---

# Garbage Collection Safety

```text
                         Stored Object
                              |
                              v
                 Reachable from any branch HEAD?
                         /             \
                       Yes              No
                        |                |
                        v                v
                       KEEP          Apply grace period
                                         |
                                  ┌──────┴──────┐
                                  │             │
                               recent         old
                                  │             │
                                  v             v
                                KEEP         DELETE
```

```text
Unreachable
    ↓
Grace Period
    ↓
Eligible
    ↓
GC Cleanup
    ↓
Deleted
```
---

# Object Lifecycle

```text
                         ┌───────────┐
                         │  Created  │
                         └─────┬─────┘
                               |
                               v
                        ┌─────────────┐
                        │  Reachable  │
                        └──────┬──────┘
                               |
                    No reachable references
                               |
                               v
                       ┌──────────────┐
                       │ Unreachable  │
                       └──────┬───────┘
                              |
                       Grace period
                              |
                 ┌────────────┴────────────┐
                 │                         │
              recent                      old
                 │                         │
                 v                         v
             Protected                  Eligible
                 │                         │
                 └────────────┬────────────┘
                              |
                              v
                           Deleted
```
