# Authentication and Authorization

```text
                    Client
                       |
                       v
              POST /auth/authenticate
                       |
                       v
                Find user by
                  credentials
                       |
                       v
                  Generate JWT
                       |
                       v
                    Client
                       |
                  JWT attached
                       |
                       v
              Protected API Request
                       |
                       v
              Spring Security / JWT
                       |
                       v
                Authenticated User
                       |
                       v
               Business Operation
                       |
                       v
             Resource Authorization
                       |
                ┌──────┴──────┐
                │             │
            Authorized     Unauthorized
                │             │
                v             v
           Perform       Reject request
           operation
```
