# MIGRATE.md — Firebase / Firestore Migration Guide

This guide covers how to modify the Firestore schema, deploy security rules and indexes, and seed test data manually.

---

## Prerequisites

### 1. Install Firebase CLI

```bash
npm install -g firebase-tools
```

### 2. Log in and select the project

```bash
firebase login
firebase use redshark-application
```

Confirm with:

```bash
firebase projects:list
```

### 3. Install Python seed dependencies

```bash
pip install firebase-admin
```

### 4. Obtain a service account key

Go to **Firebase Console → Project Settings → Service Accounts → Generate new private key**.

Save the downloaded file as `scripts/serviceAccountKey.json` (gitignored — never commit this file).

---

## Deploying Security Rules

Security rules live in `firestore.rules` at the project root. The `firebase.json` wires them automatically.

**Validate rules locally (optional):**

```bash
firebase emulators:start --only firestore
```

**Deploy to production:**

```bash
firebase deploy --only firestore:rules
```

Changes take effect within seconds. Always test with the Firestore Rules Playground in the Firebase Console before deploying to production.

---

## Deploying Composite Indexes

Composite indexes are declared in `firestore.indexes.json`. Add or remove entries there, then deploy.

**Deploy indexes:**

```bash
firebase deploy --only firestore:indexes
```

Index builds run asynchronously. Monitor build progress in **Firebase Console → Firestore → Indexes**. Queries that require an in-progress index will return an error until the build completes (typically 1–5 minutes for small datasets).

**Current indexes** (`firestore.indexes.json`):

| Collection | Fields | Purpose |
|---|---|---|
| `ideas` | `authorId` ASC, `createdAt` DESC | My ideas list |
| `ideas` | `deletedAt` ASC, `createdAt` DESC | Active-only feed |
| `issues` | `ideaId` ASC, `createdAt` DESC | Issues per idea |
| `issues` | `status` ASC, `createdAt` DESC | Filter by status |
| `issues` | `authorId` ASC, `status` ASC | Active issue count per user |
| `comments` | `ideaId` ASC, `createdAt` ASC | Comment thread |
| `notifications` | `recipientId` ASC, `createdAt` DESC | Notification inbox |
| `notifications` | `recipientId` ASC, `isRead` ASC | Unread filter |
| `conversations` | `participantIds` ARRAY_CONTAINS, `lastMessageAt` DESC | Conversation list |
| `messages` | `conversationId` ASC, `createdAt` ASC | Message history |

---

## Adding a New Field to a Collection

Firestore is schemaless — adding a field requires no migration DDL. Follow this checklist:

### Step 1: Update `SCHEMA.md`

Add the new field row to the relevant collection table in `docs/SCHEMA.md`.

### Step 2: Update the DTO

Add the field to the corresponding DTO in `data/remote/firestore/dto/`:

```kotlin
// Example: adding `lastMessageSenderId` to ConversationDto
data class ConversationDto(
    val id: String = "",
    val participantIds: List<String> = emptyList(),
    val lastMessageAt: Timestamp? = null,
    val lastMessagePreview: String? = null,
    val lastMessageSenderId: String? = null,  // ← new
    val createdAt: Timestamp? = null,
)
```

### Step 3: Update the domain model

Add the field to the corresponding model in `domain/model/`:

```kotlin
data class Conversation(
    val id: UUID,
    val participantIds: List<String>,
    val lastMessageAt: Instant?,
    val lastMessagePreview: String?,
    val lastMessageSenderId: String?,  // ← new
)
```

### Step 4: Update the mapper

Update `data/mapper/<Collection>Mapper.kt` to map the new field in both directions (DTO → domain and domain → DTO).

### Step 5: Update write operations

Add the field to any `set()` / `update()` calls in the repository implementation.

### Step 6: Deploy a new index if needed

If the new field will be used in a `where()` or `orderBy()` compound query, add the composite index to `firestore.indexes.json` and deploy:

```bash
firebase deploy --only firestore:indexes
```

### Step 7: Update security rules if needed

If the new field needs read/write restrictions, update `firestore.rules` and deploy:

```bash
firebase deploy --only firestore:rules
```

### Step 8: Handle existing documents (backfill)

Firestore documents created before the new field was added will not contain it. The app must handle `null` gracefully (use nullable types and safe defaults). If you need all existing documents to have the field, run a one-off backfill script:

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
    batch.update(doc.reference, {"lastMessageSenderId": None})
    count += 1
    if count % 400 == 0:
        batch.commit()
        batch = db.batch()
batch.commit()
print(f"Backfilled {count} documents")
```

---

## Seeding Test Data

The seed script at `scripts/seed_firestore.py` **deletes all documents** in the target collections and writes fresh test data. Run it only against a development/staging project.

### Configuration

The script auto-detects credentials in this order:

1. `scripts/serviceAccountKey.json` (preferred for local dev)
2. `GOOGLE_APPLICATION_CREDENTIALS` environment variable

The target project ID is hardcoded at the top of the script:

```python
PROJECT_ID = "redshark-application"
```

Change this if you have a separate staging project.

### Running the seed script

```bash
# From the project root
python scripts/seed_firestore.py
```

The script will:
1. Delete all documents from: `users`, `ideas`, `issues`, `comments`, `notifications`, `conversations`, `messages`
2. Write 5 seed users, ~10 ideas, ~20 issues, comments, notifications, conversations, and messages

### Seed user IDs

| Variable | Firestore UID | Display name |
|---|---|---|
| `USER_SY` | `seed_user_sy` | Ngô Tiến Sỹ |
| `USER_HAI` | `seed_user_hai` | Nguyễn Tiến Hải |
| `USER_NAM` | `seed_user_nam` | ... |
| `USER_LAN` | `seed_user_lan` | ... |
| `USER_MINH` | `seed_user_minh` | ... |

These IDs are **not real Firebase Auth UIDs**. To test with a real account, sign in once with the app, then manually copy your Auth UID from **Firebase Console → Authentication → Users** and replace one of the seed user IDs in the script.

### Partial seed (specific collections only)

Comment out unwanted `delete_collection()` and `write_batch()` calls at the bottom of `seed_firestore.py` before running.

---

## Removing a Field

1. Remove the field from the DTO and domain model.
2. Remove from the mapper.
3. Remove from any write operations.
4. Remove any composite indexes that include the field from `firestore.indexes.json`, then deploy.
5. Update security rules if the field was referenced.
6. Existing documents retain the field in Firestore (harmless — Firestore ignores unmapped fields). Run a cleanup script if storage is a concern.

---

## Renaming a Collection

Firestore does not support renaming collections. The process is:

1. Write a migration script that reads from the old collection and writes to the new one.
2. Deploy the new app version pointing to the new collection name.
3. After confirming data integrity, delete the old collection documents.

---

## Environment Checklist

Before deploying to production, verify:

- [ ] `firebase.json` points to the correct rules and indexes files
- [ ] `firestore.rules` has been reviewed and tested in the Rules Playground
- [ ] New composite indexes have been added to `firestore.indexes.json`
- [ ] `firebase deploy --only firestore:rules,firestore:indexes` ran without errors
- [ ] Index build status is **Enabled** in Firebase Console before shipping the release
- [ ] `serviceAccountKey.json` is in `.gitignore` and was **not** committed
