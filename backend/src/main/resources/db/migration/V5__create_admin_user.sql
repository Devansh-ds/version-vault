INSERT INTO users (
    id,
    username,
    email,
    password,
    created_at
)
VALUES (
   gen_random_uuid(),
   'admin',
   'admin@gmail.com',
   '$2a$12$DtPIxoZcRy7Z/lkdWoTEWemnq8GDLbGyEIB9lGjvrCSiW7kj9IqdK',
   CURRENT_TIMESTAMP
);