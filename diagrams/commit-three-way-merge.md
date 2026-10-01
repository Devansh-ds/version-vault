# Three-Way Merge

```text
                         Common Base
                             |
                       ┌─────┴─────┐
                       │           │
                       v           v
                    Ours         Theirs
                       │           │
                       └─────┬─────┘
                             |
                             v
                    Three-Way Compare
                             |
                 ┌───────────┼───────────┐
                 │           │           │
                 v           v           v
             Base=Ours   Base=Theirs  Ours=Theirs
                 │           │           │
                 v           v           v
            Take Theirs   Take Ours   Take Either
                             |
                             |
                         Otherwise
                             |
                             v
                         CONFLICT
```

### Per-path merge rules

```text
Base == Ours
    ↓
Ours did not change
    ↓
Take Theirs


Base == Theirs
    ↓
Theirs did not change
    ↓
Take Ours


Ours == Theirs
    ↓
Both reached same state
    ↓
Take either


Otherwise
    ↓
Both changed differently
    ↓
Conflict
```

The user currently resolves conflicts. This project doesn't provide a way for resolving conflicts.

---

# Full Merge Lifecycle

```text
       Merge source branch
        into target branch
                 |
                 v
      Validate branches +
          authorization
                 |
                 v
      Target working tree clean?
             /          \
           No            Yes
           |              |
           v              v
        Reject        Read target HEAD
                           |
                           v
                     Read source HEAD
                           |
                           v
                    Find merge base
                           |
                           v
              Load Base / Ours / Theirs
                    manifests
                           |
                           v
                  Three-way compare
                           |
                    ┌──────┴──────┐
                    │             │
                 conflict       clean
                    │             │
                    v             v
              Return conflict   Build merged
                                 snapshot
                                     |
                                     v
                            Replace target
                            Working Tree
                                     |
                                     v
                              Create merge
                                 commit
                                     |
                                     v
                           parent = Ours
                           secondParent = Theirs
                                     |
                                     v
                             Update target HEAD
                                     |
                                     v
                                Merge complete
```

---

# Merge Commit Structure

```text
                         Common Base
                              |
                    ┌─────────┴─────────┐
                    |                   |
                    v                   v
               Target HEAD          Source HEAD
                  Ours                 Theirs
                    \                   /
                     \                 /
                      v               v
                       ┌─────────────┐
                       │ Merge Commit│
                       └─────────────┘
```

The merge commit stores:

```text
parentCommit = target HEAD

secondParentCommit = source HEAD
```

Only the target branch moves.

```text
Target branch
    HEAD ─────────→ Merge Commit

Source branch
    HEAD ─────────→ Source Commit
                    (unchanged)
```

---

# Commit History Traversal

```text
       GET /branch/{id}/commit/history
                    |
                    v
             Read Branch HEAD
                    |
                    v
              Cursor provided?
                 /        \
               No          Yes
               |            |
               v            v
             HEAD       Cursor Commit
                \          /
                 \        /
                  v      v
                 Current Commit
                       |
                       v
                 Add to response
                       |
                       v
                  Move to parent
                       |
                       v
                 More commits?
                  /          \
                Yes           No
                 |             |
                 v             v
             Continue      Return page
```