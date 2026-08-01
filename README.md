# Chat App — socket and database prototype

Java client/server chat prototype using sockets, JSON messages, MySQL-backed login,
group metadata, and stored group conversations.

## Local configuration

Set these environment variables before starting `Server.Server`:

- `CHAT_DB_URL` (defaults to `jdbc:mysql://localhost:3306/chat_app_server_side`)
- `CHAT_DB_USER` (defaults to `root`)
- `CHAT_DB_PASSWORD` (required when the database has a password)
- `CHAT_USER_PASSWORD` (optional password used by the current demo client)

The repository currently represents a backend prototype. Several message operations
in `Server.Manager` remain unfinished and there is no packaged build yet.
