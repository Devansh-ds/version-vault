# Branch HEAD Optimistic Concurrency

```text
                     PostgreSQL
                          |
              ┌───────────┴───────────┐
              │                       │
              v                       v
          Request A              Request B
              |                       |
              | Read HEAD=C1          | Read HEAD=C1
              |                       |
              v                       v
           Create C2               Create C3
              |                       |
              v                       v
     UPDATE HEAD=C2          UPDATE HEAD=C3
     WHERE HEAD=C1            WHERE HEAD=C1
              |                       |
              v                       v
          1 row updated          0 rows updated
              |                       |
              v                       v
        HEAD → C2             Detect stale HEAD
                                      |
                                      v
                                   Reject
```

The conditional update is conceptually:

```text
UPDATE branches
SET head_commit_id = newHead
WHERE id = branchId
AND head_commit_id = expectedHead
```

If zero rows are updated, the request knows that another operation already moved the branch HEAD.
