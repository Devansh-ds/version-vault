1. Objects are immutable.

2. A content hash uniquely identifies an object.

3. A repository can contain multiple branches.

4. A branch belongs to exactly one repository.

5. A branch points to zero or one commit.

6. A working tree belongs to a branch.

7. A branch cannot contain duplicate paths.

8. A commit belongs to exactly one repository.

9. A commit has zero or one parent in MVP.

10. A commit references exactly one immutable manifest.

11. A manifest belongs to exactly one repository.

12. A manifest cannot contain duplicate paths.

13. A manifest entry references exactly one object.

14. Deleting a working-tree entry does not delete its object.

15. Historical commits and manifests cannot be modified.

16. Objects may be shared across repositories and commits.

17. Branch head updates must use optimistic concurrency control.

18. An object becomes eligible for garbage collection only when it is no
    longer reachable from retained repository state.