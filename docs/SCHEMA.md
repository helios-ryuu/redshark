# SCHEMA.md — Lược đồ CSDL (Cloud Firestore)

Mọi collection đều dùng document ID Firestore (tự sinh UUID hoặc bằng Firebase Auth UID cho `users`). `createdAt` và `updatedAt` dùng `FieldValue.serverTimestamp()` — được Firestore server điền tự động.

---

## 1. Collection `users`

| Thuộc tính   | Kiểu Firestore  | Ràng buộc                                                    | Toàn vẹn |
|--------------|-----------------|--------------------------------------------------------------|----------|
| id           | String          | NOT NULL, = Firebase Auth UID (doc ID)                       | Strong   |
| email        | String          | NOT NULL, format email                                       | Strong   |
| username     | String          | NOT NULL, UNIQUE, 3..30 ký tự, `[a-z0-9._-]`, enforce app   | Medium   |
| displayName  | String          | NOT NULL, length 3..50                                       | Medium   |
| authProvider | String (Enum)   | {GOOGLE, EMAIL}, NOT NULL; backfill user cũ = "GOOGLE"       | Strong   |
| dateOfBirth  | Timestamp?      | NULL cho Google user cũ; NOT NULL cho email-register         | Medium   |
| avatarUrl    | String?         | NULL cho phép; URL R2                                        | Weak     |
| bio          | String?         | NULL, max 280 ký tự                                          | Weak     |
| skills       | Array\<String\> | Mảng tên skill, có thể rỗng                                  | Medium   |
| createdAt    | Timestamp       | NOT NULL, server timestamp khi tạo                           | Strong   |
| updatedAt    | Timestamp       | NOT NULL, server timestamp mỗi lần ghi                       | Strong   |

> **Lưu ý bảo mật:** Password KHÔNG lưu Firestore. Firebase Auth (`createUserWithEmailAndPassword` / `signInWithEmailAndPassword`) quản lý toàn bộ credential. `username` được enforce duy nhất ở tầng app (query Firestore trước khi tạo); không có unique constraint native trong Firestore.

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
| id                 | String (auto)    | NOT NULL                                    | Strong   |
| type               | String (Enum)    | {DIRECT}, default DIRECT                    | Strong   |
| participantIds     | Array\<String\>  | → `users`, size = 2                         | Strong   |
| lastMessageAt      | Timestamp?       | NULL đến khi có tin nhắn đầu                | Medium   |
| lastMessagePreview | String?          | NULL; tối đa 80 ký tự đầu của tin cuối cùng | Weak     |
| createdAt          | Timestamp        | Server timestamp khi tạo                    | Strong   |

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
    function isSignedIn() {
      return request.auth != null;
    }

    function isOwner(uid) {
      return request.auth.uid == uid;
    }

    function isParticipant(participantIds) {
      return request.auth.uid in participantIds;
    }

    match /users/{uid} {
      allow read: if isSignedIn();
      allow write: if isOwner(uid);
    }
    match /ideas/{ideaId} {
      allow read: if isSignedIn();
      allow create: if isSignedIn();
      allow update, delete: if isOwner(resource.data.authorId);
    }
    match /issues/{issueId} {
      allow read: if isSignedIn();
      allow create: if isSignedIn();
      allow update, delete: if isOwner(resource.data.authorId)
                            || isOwner(resource.data.assigneeId);
    }
    match /comments/{commentId} {
      allow read: if isSignedIn();
      allow create: if isSignedIn();
      allow update, delete: if isOwner(resource.data.authorId);
    }
    match /notifications/{notificationId} {
      allow read: if isOwner(resource.data.recipientId);
      allow create: if isSignedIn();
      allow update: if isOwner(resource.data.recipientId);
      allow delete: if false;
    }
    match /conversations/{conversationId} {
      allow get: if isSignedIn()
                 && (!exists(/databases/$(database)/documents/conversations/$(conversationId))
                     || isParticipant(resource.data.participantIds));
      allow list: if isSignedIn() && isParticipant(resource.data.participantIds);
      allow create: if isSignedIn()
                    && request.resource.data.type == 'DIRECT'
                    && request.resource.data.participantIds.size() == 2
                    && isParticipant(request.resource.data.participantIds);
      allow update: if isSignedIn() && isParticipant(resource.data.participantIds);
      allow delete: if false;
    }
    match /messages/{messageId} {
      allow read: if isSignedIn()
                  && exists(/databases/$(database)/documents/conversations/$(resource.data.conversationId))
                  && request.auth.uid in get(/databases/$(database)/documents/conversations/$(resource.data.conversationId)).data.participantIds;
      allow create: if isSignedIn()
                    && request.resource.data.senderId == request.auth.uid
                    && exists(/databases/$(database)/documents/conversations/$(request.resource.data.conversationId))
                    && request.auth.uid in get(/databases/$(database)/documents/conversations/$(request.resource.data.conversationId)).data.participantIds;
      allow update, delete: if false;
    }
  }
}
```

---

## 12. Composite Indexes (Firestore)

| Collection      | Fields                                            | Mục đích                              |
|-----------------|---------------------------------------------------|---------------------------------------|
| `ideas`         | `authorId` ASC, `createdAt` DESC                  | List my ideas                         |
| `issues`        | `ideaId` ASC, `createdAt` DESC                    | List issues of one idea               |
| `issues`        | `status` ASC, `createdAt` DESC                    | Filter issues by status               |
| `issues`        | `authorId` ASC, `status` ASC                      | Count active issues per user          |
| `comments`      | `ideaId` ASC, `createdAt` ASC                     | List comments in idea detail          |
| `notifications` | `recipientId` ASC, `createdAt` DESC               | Notification inbox                    |
| `notifications` | `recipientId` ASC, `isRead` ASC                   | Unread badge / read filter            |
| `conversations` | `participantIds` (array-contains), `lastMessageAt` DESC | Conversation list ordering     |
| `messages`      | `conversationId` ASC, `createdAt` ASC             | Message history ordering              |

---

## 13. Firebase Operations

### Prerequisites

```bash
npm install -g firebase-tools
firebase login
firebase use redshark-application   # xác nhận: firebase projects:list
pip install firebase-admin           # cho seed script Python
```

Lấy service account key: **Firebase Console → Project Settings → Service Accounts → Generate new private key**.
Lưu file tại `scripts/serviceAccountKey.json` (gitignored — không commit).

---

### Deploy Security Rules

Rules nằm tại `firestore.rules` (project root), `firebase.json` wires tự động.

```bash
# Validate cục bộ (tuỳ chọn)
firebase emulators:start --only firestore

# Deploy production
firebase deploy --only firestore:rules
```

Kiểm tra bằng Firestore Rules Playground trong Firebase Console trước khi deploy.

---

### Deploy Composite Indexes

Indexes khai báo trong `firestore.indexes.json`. Thêm/xóa entry rồi deploy:

```bash
firebase deploy --only firestore:indexes
```

Index build bất đồng bộ — theo dõi tại **Firebase Console → Firestore → Indexes** (thường 1–5 phút cho dataset nhỏ).

---

### Thêm field mới vào collection

Firestore schemaless — không cần migration DDL. Checklist 8 bước:

1. **SCHEMA.md** — thêm dòng field mới vào bảng collection tương ứng.
2. **DTO** — thêm field vào `data/remote/firestore/dto/<Collection>Dto.kt` (nullable với default).
3. **Domain model** — thêm field vào `domain/model/<Model>.kt`.
4. **Mapper** — cập nhật `data/mapper/<Collection>Mapper.kt` cả 2 chiều (DTO ↔ domain).
5. **Write ops** — thêm field vào các lệnh `set()` / `update()` trong repository impl.
6. **Index** — nếu field dùng trong `where()` / `orderBy()` compound, thêm vào `firestore.indexes.json` và deploy.
7. **Security rules** — nếu field cần kiểm soát read/write riêng, cập nhật `firestore.rules` và deploy.
8. **Backfill** — document cũ không có field mới sẽ trả `null`; app phải xử lý nullable. Nếu cần điền giá trị cho document cũ:

```python
# backfill_example.py
import firebase_admin
from firebase_admin import credentials, firestore

firebase_admin.initialize_app(credentials.Certificate("scripts/serviceAccountKey.json"),
                              {"projectId": "redshark-application"})
db = firestore.client()

docs = db.collection("conversations").stream()
batch = db.batch()
count = 0
for doc in docs:
    batch.update(doc.reference, {"newField": None})
    count += 1
    if count % 400 == 0:
        batch.commit()
        batch = db.batch()
batch.commit()
print(f"Backfilled {count} documents")
```

---

### Seeding Test Data

Script `scripts/seed_firestore.py` **xóa toàn bộ document** trong các collection target rồi ghi data mới. Chỉ chạy với dev/staging project.

```bash
python scripts/seed_firestore.py
```

Script tự detect credentials theo thứ tự: (1) `scripts/serviceAccountKey.json`, (2) `GOOGLE_APPLICATION_CREDENTIALS`.

| Biến | Firestore UID | Tên hiển thị |
|---|---|---|
| `USER_SY` | `seed_user_sy` | Ngô Tiến Sỹ |
| `USER_HAI` | `seed_user_hai` | Nguyễn Tiến Hải |
| `USER_NAM` | `seed_user_nam` | — |
| `USER_LAN` | `seed_user_lan` | — |
| `USER_MINH` | `seed_user_minh` | — |

> Seed UID không phải Firebase Auth UID thật. Để test với tài khoản thật: đăng nhập app → copy Auth UID từ **Firebase Console → Authentication → Users** → thay vào script.

---

### Xóa field

1. Xóa khỏi DTO và domain model.
2. Xóa khỏi mapper.
3. Xóa khỏi write ops.
4. Xóa composite index liên quan khỏi `firestore.indexes.json` → deploy.
5. Cập nhật security rules nếu field được tham chiếu.
6. Document cũ vẫn giữ field trong Firestore (vô hại — Firestore bỏ qua field không mapped). Chạy cleanup script nếu cần tiết kiệm storage.

---

### Đổi tên collection

Firestore không hỗ trợ rename. Quy trình:
1. Viết migration script đọc collection cũ → ghi collection mới.
2. Deploy app version mới trỏ sang collection name mới.
3. Sau khi xác nhận data integrity → xóa document collection cũ.

---

### Environment checklist trước khi deploy production

- [ ] `firebase.json` trỏ đúng rules và indexes file
- [ ] `firestore.rules` đã test trong Rules Playground
- [ ] Composite indexes mới đã thêm vào `firestore.indexes.json`
- [ ] `firebase deploy --only firestore:rules,firestore:indexes` chạy không lỗi
- [ ] Status index là **Enabled** trong Firebase Console trước khi ship release
- [ ] `serviceAccountKey.json` có trong `.gitignore` và **không** bị commit
