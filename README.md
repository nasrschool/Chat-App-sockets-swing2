# Java Socket Chat

A small WhatsApp-like desktop chat application built with Java 17, Swing, TCP sockets, JSON, JDBC, and PostgreSQL.

# Quick start

Requirements:

- Java 17 or newer
- Maven
- Docker Desktop, or Docker Engine with Docker Compose

From the project directory:

1. Start PostgreSQL.

   ```bash
   docker compose up -d
   ```

2. Wait until its status is `healthy`.

   ```bash
   docker compose ps
   ```

3. Compile the application.

   ```bash
   mvn clean compile
   ```

4. Start the server and leave this terminal open.

   ```bash
   mvn exec:java -Dexec.mainClass="Server.Server"
   ```

5. In a second terminal, start a client.

   ```bash
   mvn exec:java -Dexec.mainClass="Client.ClientApp"
   ```

6. Start additional clients with the same client command in additional terminals.

Demo credentials:

```text
User 1 / demo1
User 2 / demo2
User 3 / demo3
User 4 / demo4
```

The PostgreSQL container and Java server must stay running. Each client window represents one user, and the same user cannot be logged in twice at once.

Stop PostgreSQL without deleting messages:

```bash
docker compose down
```

Reset PostgreSQL completely, including all conversations and messages:

```bash
docker compose down -v
docker compose up -d
```

`down` preserves the named data volume. `down -v` deletes it, so the schema and demo data are recreated during the next startup.

## Using the application

Log in with one of the demo accounts. The left sidebar separates direct conversations from normal groups. Select a conversation to load its stored history.

- `+ Direct` asks for another user ID. An existing direct conversation is returned instead of creating a duplicate.
- `+ Group` asks for a name and comma-separated user IDs. The creator is included automatically.
- Send messages with the button or Enter. Empty messages are ignored.
- Own messages appear on the right. Incoming messages appear on the left. Incoming group messages show `User N` above the message.
- Connected participants refresh their conversation lists when someone creates a conversation containing them.

## Architecture

```text
Swing client
    -> TCP socket + line-delimited JSON
    -> Server / LoginAuthenticator / ClientHandler / Manager
    -> JDBC
    -> PostgreSQL
```

The client keeps one socket open after login. `Manager` uses prepared statements for reads and writes. Messages are stored in `groups_chats` and then broadcast through the existing connected `ClientHandler` objects, including back to the sender. The UI therefore has one rendering path and does not optimistically duplicate sent messages.

Direct and group conversations share the same model:

- a direct conversation has exactly two rows in `users_of_groups`, `is_private = true`, and no group name;
- a normal group has `is_private = false` and a repeated `group_name` for its member rows;
- both store messages in `groups_chats`.

The four database tables are `users_table`, `users_of_groups`, `admins_of_groups`, and `groups_chats`. Compose mounts `database/schema.sql` and `database/sample_data.sql` as ordered PostgreSQL initialization scripts. PostgreSQL runs them automatically only when its data directory is empty.

## Project structure

```text
src/Client/                 Swing client and socket logic
src/Server/                 socket server, authentication, handlers, manager
src/Tools/                  message types, SQL statements, JSON conversion
database/schema.sql         PostgreSQL schema
database/sample_data.sql    rerunnable demo seed data
docker-compose.yml          PostgreSQL 16 service and healthcheck
pom.xml                     Java 17 build and dependencies
```

## Configuration

The defaults match `docker-compose.yml`:

```text
CHAT_DB_URL=jdbc:postgresql://localhost:5432/chat_app_server_side
CHAT_DB_USER=chatuser
CHAT_DB_PASSWORD=chatpass
```

Set those environment variables before starting the server to override them. The client uses `CHAT_SERVER_HOST`, which defaults to `localhost`. The chat server listens on TCP port `1234`.

## JSON examples

Authentication keeps the original simple boolean message type:

```json
{"msgType":true,"user_id":1,"user_password":"demo1"}
```

Start or return a direct conversation:

```json
{"msgType":"INVITE_TO_DM","msgSource":1,"user_id":2}
```

Create a group:

```json
{"msgType":"MAKE_GROUP","msgSource":1,"group_name":"Demo Group","users_id":[2,3]}
```

Send a message:

```json
{"msgType":"SEND_MSG","msgSource":1,"msgDestination":2,"content":"Hello everyone"}
```

## Inspecting PostgreSQL

List tables and demo users without installing `psql` locally:

```bash
docker compose exec postgres psql -U chatuser -d chat_app_server_side -c "\dt"
docker compose exec postgres psql -U chatuser -d chat_app_server_side -c "SELECT * FROM users_table ORDER BY user_id;"
```

Inspect stored messages:

```bash
docker compose exec postgres psql -U chatuser -d chat_app_server_side -c "SELECT * FROM groups_chats ORDER BY message_id;"
```

## Troubleshooting

### Port 5432 already in use

Another PostgreSQL server may already use the port. Stop it, or select another host port before starting Compose. For example, in PowerShell:

```powershell
$env:CHAT_DB_PORT = "5433"
docker compose up -d
```

On Unix-like systems:

```bash
CHAT_DB_PORT=5433 docker compose up -d
```

Then start the Java server with a matching URL:

```text
CHAT_DB_URL=jdbc:postgresql://localhost:5433/chat_app_server_side
```

On Windows, `Get-NetTCPConnection -LocalPort 5432` can identify the listening process. On Unix-like systems, use `lsof -i :5432` or `ss -ltnp`.

### Port 1234 already in use

Another chat server instance is probably running. Close it before starting a new one.

### Cannot connect to the database

Check container state and initialization output:

```bash
docker compose ps
docker compose logs postgres
```

Wait for `healthy` before starting the Java server.

### Login does not work

Use one of the seeded credentials above. If a user is already connected, close that user's existing client first.

### Reset demo data

This permanently deletes the local Docker database volume and recreates the seed data:

```bash
docker compose down -v
docker compose up -d
```

## Scope

This is intentionally an educational desktop application. It does not include tokens, persistent sessions, automatic reconnection, REST, WebSockets, Spring, ORM, migrations frameworks, message brokers, or a Dockerized Java application.
