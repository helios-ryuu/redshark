# SCHEMA.md — Lược đồ CSDL (Cloud Firestore)

Mọi collection đều dùng document ID Firestore (tự sinh UUID hoặc bằng Firebase Auth UID cho `users`). `createdAt` và `updatedAt` dùng `FieldValue.serverTimestamp()` — được Firestore server điền tự động.

---

## 1. Collection `users`

| Thuộc tính  | Kiểu Firestore | Ràng buộc                               | Toàn vẹn |
|-------------|----------------|-----------------------------------------|----------|
| id          | String         | NOT NULL, = Firebase Auth UID (doc ID)  | Strong   |
| email       | String         | NOT NULL, format email                  | Strong   |
| displayName | String         | NOT NULL, length 3..50                  | Medium   |
| avatarUrl   | String?        | NULL cho phép; URL R2                   | Weak     |
| bio         | String?        | NULL, max 280 ký tự                     | Weak     |
| skills      | Array\<String\>| Mảng tên skill, có thể rỗng             | Medium   |
| createdAt   | Timestamp      | NOT NULL, server timestamp khi tạo      | Strong   |
| updatedAt   | Timestamp      | NOT NULL, server timestamp mỗi lần ghi  | Strong   |

---

## 2. Collection `ideas`

| Thuộc tính      | Kiểu Firestore  | Ràng buộc                                        | Toàn vẹn |
|-----------------|-----------------|--------------------------------------------------|----------|
| id              | String (auto)   | NOT NULL, doc ID tự sinh                         | Strong   |
| authorId        | String          | → `users/{id}`, NOT NULL                         | Strong   |
| title           | String          | NOT NULL, length 3..120                          | Strong   |
| description     | String?         | NULL, max 5000                                   | Medium   |
| status          | String (Enum)   | {ACTIVE, CLOSED, CANCELLED}, default ACTIVE      | Strong   |
| tags            | Array\<String\> | Mảng tên tag                                     | Medium   |
| collaboratorIds | Array\<String\> | Mảng UID → `users`                               | Medium   |
| createdAt       | Timestamp       | Server timestamp khi tạo                         | Strong   |
| updatedAt       | Timestamp       | Server timestamp khi cập nhật                    | Strong   |
| deletedAt       | Timestamp?      | NULL → chưa xóa; soft delete                     | Strong   |

---

## 3. Collection `issues`

| Thuộc tính  | Kiểu Firestore | Ràng buộc                                                   | Toàn vẹn |
|-------------|----------------|-------------------------------------------------------------|----------|
| id          | String (auto)  | NOT NULL, doc ID tự sinh                                    | Strong   |
| ideaId      | String         | → `ideas/{id}`, NOT NULL                                    | Strong   |
| authorId    | String         | → `users/{id}`, NOT NULL                                    | Strong   |
| assigneeId  | String?        | → `users/{id}`, NULL                                        | Medium   |
| title       | String         | NOT NULL, 3..120                                            | Strong   |
| description | String?        | NULL, max 5000                                              | Medium   |
| status      | String (Enum)  | {OPEN, IN_PROGRESS, CLOSED, CANCELLED}, default OPEN        | Strong   |
| priority    | String (Enum)  | {LOW, MEDIUM, HIGH}, default MEDIUM                         | Medium   |
| createdAt   | Timestamp      | Server timestamp khi tạo                                    | Strong   |
| updatedAt   | Timestamp      | Server timestamp khi cập nhật                               | Strong   |
| deletedAt   | Timestamp?     | NULL → chưa xóa                                             | Strong   |

**Business rule:** Một user không có quá 20 issue active (status ∈ {OPEN, IN_PROGRESS}) do mình tạo — enforce client-side qua Firestore query `where authorId == uid && status in [OPEN, IN_PROGRESS]`.

---

## 4. Collection `comments`

| Thuộc tính | Kiểu Firestore | Ràng buộc                        | Toàn vẹn |
|------------|----------------|----------------------------------|----------|
| id         | String (auto)  | NOT NULL, doc ID tự sinh         | Strong   |
| ideaId     | String         | → `ideas/{id}`, NOT NULL         | Strong   |
| authorId   | String         | → `users/{id}`, NOT NULL         | Strong   |
| content    | String         | NOT NULL, 1..1000                | Medium   |
| createdAt  | Timestamp      | Server timestamp khi tạo         | Strong   |

---

## 5. Collection `tags`

| Thuộc tính | Kiểu Firestore | Ràng buộc        | Toàn vẹn |
|------------|----------------|------------------|----------|
| id         | String (auto)  | NOT NULL         | Strong   |
| name       | String         | UNIQUE, NOT NULL | Strong   |
| color      | String?        | HEX #RRGGBB      | Weak     |

## 6. Collection `skills`

| Thuộc tính | Kiểu Firestore | Ràng buộc        | Toàn vẹn |
|------------|----------------|------------------|----------|
| id         | String (auto)  | NOT NULL         | Strong   |
| name       | String         | UNIQUE, NOT NULL | Strong   |

---

## 7. Collection `notifications`

| Thuộc tính  | Kiểu Firestore | Ràng buộc                                                           | Toàn vẹn |
|-------------|----------------|---------------------------------------------------------------------|----------|
| id          | String (auto)  | NOT NULL                                                            | Strong   |
| recipientId | String         | → `users/{id}`, NOT NULL                                            | Strong   |
| actorId     | String?        | → `users/{id}`, NULL nếu system                                     | Medium   |
| type        | String (Enum)  | {ISSUE_CREATED, COLLAB_REQUEST, COLLAB_ACCEPTED, COLLAB_REJECTED, COMMENT} | Strong   |
| targetType  | String (Enum)  | {IDEA, ISSUE, COMMENT}                                              | Strong   |
| targetId    | String         | NOT NULL, polymorphic ID                                            | Weak     |
| message     | String         | NOT NULL                                                            | Medium   |
| isRead      | Boolean        | default false                                                       | Strong   |
| createdAt   | Timestamp      | Server timestamp khi tạo                                            | Strong   |

---

## 8. Collection `conversations`

| Thuộc tính     | Kiểu Firestore  | Ràng buộc                               | Toàn vẹn |
|----------------|------------------|-----------------------------------------|----------|
| id             | String (auto)    | NOT NULL                                | Strong   |
| type           | String (Enum)    | {DIRECT}, default DIRECT               | Strong   |
| participantIds | Array\<String\>  | → `users`, size = 2                    | Strong   |
| directKey      | String           | NOT NULL, format `<uidA>_<uidB>` (sorted) | Strong |
| lastMessage    | String?          | NULL đến khi có tin nhắn đầu           | Medium   |
| lastMessageAt  | Timestamp?       | NULL đến khi có tin nhắn đầu           | Medium   |
| createdAt      | Timestamp        | Server timestamp khi tạo               | Strong   |

## 9. Collection `messages`

| Thuộc tính     | Kiểu Firestore | Ràng buộc                             | Toàn vẹn |
|----------------|----------------|---------------------------------------|----------|
| id             | String (auto)  | NOT NULL                              | Strong   |
| conversationId | String         | → `conversations/{id}`, NOT NULL      | Strong   |
| senderId       | String         | → `users/{id}`, NOT NULL              | Strong   |
| content        | String         | NOT NULL, 1..2000                     | Medium   |
| createdAt      | Timestamp      | Server timestamp khi tạo              | Strong   |
| status         | String (Enum)  | {SENDING, SENT, FAILED}, default SENT | Medium   |

---

## 10. Firestore Operations (pattern CRUD)

### users
```kotlin
// Create / upsert (merge)
firestore.collection("users").document(uid)
    .set(data, SetOptions.merge()).await()

// Read
firestore.collection("users").document(uid).get().await()

// Update fields
firestore.collection("users").document(uid)
    .update(mapOf("displayName" to name, "updatedAt" to FieldValue.serverTimestamp())).await()
```

### ideas / issues / comments
```kotlin
// Create (auto ID)
firestore.collection("ideas").add(data).await()

// List with filter
firestore.collection("issues")
    .whereEqualTo("ideaId", ideaId)
    .whereIn("status", listOf("OPEN", "IN_PROGRESS"))
    .orderBy("createdAt", Query.Direction.DESCENDING)
    .get().await()

// Soft delete
firestore.collection("ideas").document(ideaId)
    .update("deletedAt", FieldValue.serverTimestamp()).await()
```

---

## 11. Security Rules (cơ bản)

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{uid} {
      allow read: if request.auth != null;
      allow write: if request.auth.uid == uid;
    }
    match /ideas/{ideaId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null;
      allow update, delete: if request.auth.uid == resource.data.authorId;
    }
    match /issues/{issueId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null;
      allow update, delete: if request.auth.uid == resource.data.authorId;
    }
    match /comments/{commentId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null;
      allow delete: if request.auth.uid == resource.data.authorId;
    }
    match /notifications/{notifId} {
      allow read, write: if request.auth.uid == resource.data.recipientId;
    }
    match /conversations/{convId} {
      allow read, write: if request.auth.uid in resource.data.participantIds;
    }
    match /messages/{msgId} {
      allow read, write: if request.auth.uid == resource.data.senderId;
    }
    match /tags/{tagId} { allow read: if true; }
    match /skills/{skillId} { allow read: if true; }
  }
}
```

---

## 12. Composite Indexes (Firestore)

| Collection      | Fields                                   | Mục đích                           |
|-----------------|------------------------------------------|------------------------------------|
| `ideas`         | `authorId` ASC, `deletedAt` ASC          | List my ideas (chưa xóa)           |
| `issues`        | `ideaId` ASC, `deletedAt` ASC            | List issues của 1 idea             |
| `issues`        | `authorId` ASC, `status` ASC             | Count active issues của user       |
| `notifications` | `recipientId` ASC, `isRead` ASC, `createdAt` DESC | Inbox                   |
| `messages`      | `conversationId` ASC, `createdAt` DESC   | Chat history                       |
| `conversations` | `participantIds` (array-contains), `lastMessageAt` DESC | Danh sách hội thoại |
