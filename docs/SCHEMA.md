# SCHEMA.md — Lược đồ CSDL (Firebase Data Connect / PostgreSQL)

Mọi bảng đều dùng `id: UUID` làm PK (ngoại trừ `users` dùng `auth.uid` String). `createdAt`, `updatedAt` tự động điền qua Data Connect expression.

---

## 1. Bảng `users`

| Thuộc tính     | Kiểu dữ liệu   | Khóa | Ràng buộc                                  | Toàn vẹn |
|----------------|----------------|------|--------------------------------------------|----------|
| id             | String (UID)   | PK   | NOT NULL, = Firebase `auth.uid`            | Strong   |
| email          | String         |      | UNIQUE, NOT NULL, format email             | Strong   |
| displayName    | String         |      | NOT NULL, length 3..50                     | Medium   |
| avatarUrl      | String         |      | NULL cho phép; URL R2                      | Weak     |
| bio            | String         |      | NULL, max 280 ký tự                        | Weak     |
| skillIds       | UUID[]         |  FK  | Mỗi phần tử → `skills.id`                  | Medium   |
| createdAt      | Timestamp      |      | NOT NULL, default `request.time`           | Strong   |
| updatedAt      | Timestamp      |      | NOT NULL, auto-update                      | Strong   |

---

## 2. Bảng `ideas`

| Thuộc tính      | Kiểu dữ liệu | Khóa | Ràng buộc                                   | Toàn vẹn |
|-----------------|--------------|------|---------------------------------------------|----------|
| id              | UUID         | PK   | NOT NULL                                    | Strong   |
| authorId        | String       | FK   | → `users.id`, NOT NULL                      | Strong   |
| title           | String       |      | NOT NULL, length 3..120                     | Strong   |
| description     | Text         |      | NULL, max 5000                              | Medium   |
| status          | Enum         |      | {ACTIVE, CLOSED, CANCELLED}, default ACTIVE | Strong   |
| tagIds          | UUID[]       | FK   | → `tags.id`                                 | Medium   |
| collaboratorIds | String[]     | FK   | → `users.id`                                | Medium   |
| createdAt       | Timestamp    |      | default `request.time`                      | Strong   |
| updatedAt       | Timestamp    |      | auto-update                                 | Strong   |
| deletedAt       | Timestamp    |      | NULL → chưa xóa; xóa mềm                    | Strong   |

---

## 3. Bảng `issues`

| Thuộc tính   | Kiểu dữ liệu | Khóa | Ràng buộc                                               | Toàn vẹn |
|--------------|--------------|------|---------------------------------------------------------|----------|
| id           | UUID         | PK   | NOT NULL                                                | Strong   |
| ideaId       | UUID         | FK   | → `ideas.id`, NOT NULL                                  | Strong   |
| authorId     | String       | FK   | → `users.id`, NOT NULL                                  | Strong   |
| assigneeId   | String       | FK   | → `users.id`, NULL                                      | Medium   |
| title        | String       |      | NOT NULL, 3..120                                        | Strong   |
| description  | Text         |      | NULL, max 5000                                          | Medium   |
| status       | Enum         |      | {OPEN, IN_PROGRESS, CLOSED, CANCELLED}, default OPEN    | Strong   |
| priority     | Enum         |      | {LOW, MEDIUM, HIGH}, default MEDIUM                     | Medium   |
| createdAt    | Timestamp    |      | default `request.time`                                  | Strong   |
| updatedAt    | Timestamp    |      | auto-update                                             | Strong   |
| deletedAt    | Timestamp    |      | NULL → chưa xóa                                         | Strong   |

**Business rule:** Một user không có quá 20 issue active (status ∈ {OPEN, IN_PROGRESS}) do mình tạo — enforce client-side qua query `CountMyActiveIssues`.

---

## 4. Bảng `comments`

| Thuộc tính | Kiểu dữ liệu | Khóa | Ràng buộc                         | Toàn vẹn |
|------------|--------------|------|-----------------------------------|----------|
| id         | UUID         | PK   | NOT NULL                          | Strong   |
| ideaId     | UUID         | FK   | → `ideas.id`, NOT NULL, CASCADE   | Strong   |
| authorId   | String       | FK   | → `users.id`                      | Strong   |
| content    | Text         |      | NOT NULL, 1..1000                 | Medium   |
| createdAt  | Timestamp    |      | default `request.time`            | Strong   |

---

## 5. Bảng `tags`

| Thuộc tính | Kiểu dữ liệu | Khóa | Ràng buộc           | Toàn vẹn |
|------------|--------------|------|---------------------|----------|
| id         | UUID         | PK   | NOT NULL            | Strong   |
| name       | String       |      | UNIQUE, NOT NULL    | Strong   |
| color      | String       |      | HEX #RRGGBB         | Weak     |

## 6. Bảng `skills`

| Thuộc tính | Kiểu dữ liệu | Khóa | Ràng buộc        | Toàn vẹn |
|------------|--------------|------|------------------|----------|
| id         | UUID         | PK   | NOT NULL         | Strong   |
| name       | String       |      | UNIQUE, NOT NULL | Strong   |

---

## 7. Bảng `notifications`

| Thuộc tính  | Kiểu dữ liệu | Khóa | Ràng buộc                                                   | Toàn vẹn |
|-------------|--------------|------|-------------------------------------------------------------|----------|
| id          | UUID         | PK   | NOT NULL                                                    | Strong   |
| recipientId | String       | FK   | → `users.id`, NOT NULL                                      | Strong   |
| actorId     | String       | FK   | → `users.id`, NULL (system)                                 | Medium   |
| type        | Enum         |      | {ISSUE_CREATED, COLLAB_REQUEST, COLLAB_ACCEPTED, COLLAB_REJECTED, COMMENT} | Strong   |
| targetType  | Enum         |      | {IDEA, ISSUE, COMMENT}                                      | Strong   |
| targetId    | UUID         |      | NOT NULL, polymorphic                                       | Weak     |
| message     | String       |      | NOT NULL                                                    | Medium   |
| isRead      | Boolean      |      | default false                                               | Strong   |
| createdAt   | Timestamp    |      | default `request.time`                                      | Strong   |

---

## 8. Bảng `conversations`

| Thuộc tính     | Kiểu dữ liệu | Khóa | Ràng buộc                                 | Toàn vẹn |
|----------------|--------------|------|-------------------------------------------|----------|
| id             | UUID         | PK   | NOT NULL                                  | Strong   |
| type           | Enum         |      | {DIRECT}, default DIRECT                  | Strong   |
| participantIds | String[]     | FK   | → `users.id`, size = 2                    | Strong   |
| lastMessageAt  | Timestamp    |      | NULL đến khi có tin nhắn đầu              | Medium   |
| createdAt      | Timestamp    |      | default `request.time`                    | Strong   |

## 9. Bảng `messages`

| Thuộc tính     | Kiểu dữ liệu | Khóa | Ràng buộc                                         | Toàn vẹn |
|----------------|--------------|------|---------------------------------------------------|----------|
| id             | UUID         | PK   | NOT NULL                                          | Strong   |
| conversationId | UUID         | FK   | → `conversations.id`, CASCADE                     | Strong   |
| senderId       | String       | FK   | → `users.id`, NOT NULL                            | Strong   |
| content        | Text         |      | NOT NULL, 1..2000                                 | Medium   |
| createdAt      | Timestamp    |      | default `request.time`                            | Strong   |

---

## 10. Queries / Mutations GraphQL cốt lõi

### Queries (`dataconnect/redshark/queries.gql`)
```graphql
query GetMe @auth(level: USER) { ... }
query GetIdea($id: UUID!) @auth(level: USER) { ... }
query ListOpenIssuesFromOthers @auth(level: USER) { ... }
query ListMyIdeas @auth(level: USER) { ... }
query CountMyActiveIssues @auth(level: USER) { ... }
query ListCommentsByIdea($ideaId: UUID!) @auth(level: USER) { ... }
query ListMyNotifications @auth(level: USER) { ... }
query GetConversation($id: UUID!) @auth(level: USER) { ... }
query FindDirectConversation($peerId: String!) @auth(level: USER) { ... }
query ListTags @auth(level: PUBLIC) { ... }
query ListSkills @auth(level: PUBLIC) { ... }
```

### Mutations (`dataconnect/redshark/mutations.gql`)
```graphql
mutation UpsertUser($input: UserInput!) @auth(level: USER) { ... }
mutation UpdateProfile($input: ProfileInput!) @auth(level: USER) { ... }
mutation CreateIdea($input: IdeaInput!) @auth(level: USER) { ... }
mutation UpdateIdea($id: UUID!, $input: IdeaInput!) @auth(level: USER)
  where: { authorId: { eq_expr: "auth.uid" } }
mutation SoftDeleteIdea($id: UUID!) @auth(level: USER)
  set: { deletedAt_expr: "request.time" }
mutation CreateIssue($input: IssueInput!) @auth(level: USER) { ... }
mutation UpdateIssueStatus($id: UUID!, $status: IssueStatus!) @auth(level: USER)
mutation SoftDeleteIssue($id: UUID!) @auth(level: USER)
mutation CreateComment($input: CommentInput!) @auth(level: USER) { ... }
mutation MarkNotificationRead($id: UUID!) @auth(level: USER) { ... }
mutation CreateNotification($input: NotificationInput!) @auth(level: USER)
mutation CreateDirectConversation($peerId: String!) @auth(level: USER) { ... }
mutation SendMessage($conversationId: UUID!, $content: String!) @auth(level: USER)
```

## 11. Triggers / Functions (Data Connect expressions)

| Trigger                                        | Mô tả                                 |
|------------------------------------------------|---------------------------------------|
| `createdAt_expr: "request.time"`               | Tự gán thời gian server khi insert    |
| `updatedAt_expr: "request.time"`               | Auto refresh khi update               |
| `authorId_expr: "auth.uid"`                    | Bắt buộc gán author = người đăng nhập |
| `deletedAt_expr: "request.time"`               | Soft-delete mutation                  |
| `where: { authorId: { eq_expr: "auth.uid" } }` | Ownership check cho update/delete     |

## 12. Indexes đề xuất

- `ideas(authorId, deletedAt)` — list my ideas
- `issues(ideaId, deletedAt)` — list issues của 1 idea
- `issues(authorId, status, deletedAt)` — count active
- `notifications(recipientId, isRead, createdAt DESC)` — inbox
- `messages(conversationId, createdAt DESC)` — chat history
- `conversations(participantIds GIN, lastMessageAt DESC)` — danh sách hội thoại
