# Commit Diff Engine

```text
              Target Commit
                    |
                    v
             ┌─────────────┐
             │ Load Commit │
             └──────┬──────┘
                    |
              Parent exists?
               /          \
             No            Yes
             |              |
             v              v
       Empty Tree      Parent Manifest
             \              /
              \            /
               v          v
             Current Manifest
                    |
                    v
          Build union of paths
                    |
                    v
        Compare Object references
                    |
          ┌─────────┼─────────┐
          │         │         │
          v         v         v
        Added    Modified   Deleted
          |         |         |
          v         v         v
      only new   different  only old
        path      object      path
```

### Diff rule

```text
Path exists only in current
    → ADDED

Path exists only in parent
    → DELETED

Path exists in both but Object differs
    → MODIFIED

Same Object
    → no diff entry
```
