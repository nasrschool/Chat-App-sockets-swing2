# Java Socket Chat

A small WhatsApp-like desktop chat application built for learning Java Swing, TCP sockets, JSON, threads, JDBC, and MySQL. It supports login, stored direct conversations, named groups, conversation history, and live delivery to connected members.

The project deliberately stays simple. It does not use a web server, tokens, sessions, ORM, dependency injection, or an external UI framework.

## Architecture

The Swing client keeps one TCP socket open after login. Each JSON object is written on one line with `BufferedWriter` and read with `BufferedReader`. The server authenticates the socket, creates one `ClientHandler` for the user, and passes application messages to the existing `Manager`. `Manager` uses JDBC prepared statements and sends responses through the connected handlers.

```text
Swing frames -> ClientLogic -> TCP socket -> LoginAuthenticator / ClientHandler
                                            -> Manager -> MySQL
```

The sender also receives the server's `SEND_MSG` broadcast. The client therefore never inserts an optimistic message and has one rendering path for outgoing and incoming messages.

## Project structure

```text
src/
  Client/
    ClientApp.java       Swing entry point
    ClientLogic.java     socket connection, queues, incoming messages
    LoginFrame.java      user ID/password login
    ChatFrame.java       conversation sidebar and message view
  Server/
    Server.java
    LoginAuthenticator.java
    ClientHandler.java
    Manager.java
  Tools/
    DataToJson.java
    MsgTypes.java
    Statements.java
database/
  schema.sql
  sample_data.sql
pom.xml
```

## Conversation model

Direct messages and groups share the same database and message flow. A direct conversation is a group with exactly two membership rows and `is_private = true`. A normal group has `is_private = false` and a repeated `group_name` in its membership rows. Both store messages in `groups_chats`.

Private conversations are displayed as `User 4`, using the other member's numeric ID. The project intentionally has no username or profile subsystem.

## Client behavior

Run `Client.ClientApp` to open the login window. Enter a numeric user ID and password. Invalid credentials are displayed under the login fields. A successful login keeps the authenticated socket open and shows the main chat frame.

The left sidebar contains `+ Direct`, `+ Group`, direct conversations, and groups. Items have normal, hover, and selected backgrounds. Selecting one clears the message area and loads its stored history.

The right side displays the conversation name, scrollable messages, a text field, and Send button. Enter also sends. Empty messages are ignored. Own messages are right-aligned; other messages are left-aligned. In groups, incoming messages include a `User N` sender label.

`+ Direct` asks for another numeric user ID. The server returns the existing private conversation for that exact pair or creates it, so duplicate DMs are not created.

`+ Group` asks for a group name and comma-separated user IDs. Invalid/nonexistent IDs are ignored, the creator is always included, and the creator is inserted into `admins_of_groups`. At least one valid member besides the creator is required.

## JSON examples

Authentication uses the original boolean message type:

```json
{"msgType":true,"user_id":3,"user_password":"example"}
```

Load conversations:

```json
{"msgType":"GET_ALL_GROUP_DATA","msgSource":3}
```

The response contains each conversation's group ID, privacy flag, nullable group name, and member IDs.

```json
{"msgType":"GET_ALL_GROUP_DATA","content":[{"group_id":4,"is_private":true,"group_name":null,"users_id":[3,5]}]}
```

Load history:

```json
{"msgType":"GET_ALL_GROUP_CHAT","msgSource":3,"msgDestination":8}
```

Start or return a direct conversation:

```json
{"msgType":"INVITE_TO_DM","msgSource":3,"user_id":4}
```

Create a group:

```json
{"msgType":"MAKE_GROUP","msgSource":3,"group_name":"School Project","users_id":[2,4,7]}
```

Send a message:

```json
{"msgType":"SEND_MSG","msgSource":3,"msgDestination":8,"content":"Hello everyone"}
```

Server broadcast:

```json
{"msgType":"SEND_MSG","group_id":8,"user_id":3,"content":"Hello everyone","date":"2026-08-06T18:04:00"}
```

## Database

The schema keeps the original concepts:

- `users_table`: numeric IDs and passwords
- `users_of_groups`: membership, privacy flag, and nullable group name
- `admins_of_groups`: group administrators
- `groups_chats`: persisted messages

Create a clean local database by running `database/schema.sql` with a MySQL client. Optionally run `database/sample_data.sql`; it creates users 1, 2, and 3 with local demonstration passwords `demo1`, `demo2`, and `demo3`, plus sample conversations. These are examples, not real credentials.

For an existing database created by an older version, add the required metadata column before running the completed application:

```sql
ALTER TABLE users_of_groups ADD COLUMN group_name VARCHAR(100) NULL;
```

## Configuration

The server uses these environment variables:

- `CHAT_DB_URL` (default `jdbc:mysql://localhost:3306/chat_app_server_side`)
- `CHAT_DB_USER` (default `root`)
- `CHAT_DB_PASSWORD` (default empty)

The client optionally uses `CHAT_SERVER_HOST` (default `localhost`). The TCP port is `1234`.

PowerShell example:

```powershell
$env:CHAT_DB_URL = "jdbc:mysql://localhost:3306/chat_app_server_side"
$env:CHAT_DB_USER = "root"
$env:CHAT_DB_PASSWORD = "your-local-password"
```

## Build and run

Requirements are JDK 17, Maven, and MySQL 8.

```bash
mvn clean compile
```

Run `Server.Server` first from an IDE or with the compiled classes and Maven dependencies on the classpath. Then run one or more instances of `Client.ClientApp`. Each client should use a different sample account when testing simultaneous delivery.

Closing a client closes its socket. The server removes its `ClientHandler`, allowing the same user to log in again. Messages already stored in MySQL remain available on the next login.

## Scope

This is intentionally an educational desktop application. It excludes access and refresh tokens, persistent sessions, automatic reconnection, REST APIs, WebSockets, Spring, Hibernate/JPA, message brokers, media, encryption, reactions, receipts, typing indicators, profiles, and advanced group administration.
