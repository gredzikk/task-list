Simple task list application written in Java using JavaFX and PSQL.

Multithreaded server is connected to psql database containing users and their tasks.

Multiple clients can connect to database, each with their own unique login.

Authentication is password secured, database stores SHA-256 hashes of passwords.

On first login attempt with new username, an account with this username is created.

Communication is based on JSON files.

There is one admin account which can see list of users and their last time of login.

A task has a name, description and status (done/not done).

Each user can create, modify and remove tasks.

Server is outputting diagnostic data to terminal.
