#!/usr/bin/env python3
"""
Reset production data for RedShark.

This script deploys no application code. It only deletes runtime data:
- Cloud Firestore documents, including nested subcollections.
- Firebase Authentication users.
- Cloudflare R2 objects in the configured bucket.

Required confirmation for destructive reset:
    python scripts/reset_production.py reset --project redshark-application --confirm REDSHARK_PRODUCTION_RESET

Non-destructive checks:
    python scripts/reset_production.py dry-run --project redshark-application
    python scripts/reset_production.py verify --project redshark-application
"""

from __future__ import annotations

import argparse
import os
import sys
from pathlib import Path
from typing import Iterable

import firebase_admin
from firebase_admin import auth, credentials, firestore

try:
    import boto3
except ImportError:  # pragma: no cover - optional until R2 cleanup is used
    boto3 = None


PROJECT_ID = "redshark-application"
CONFIRM_TOKEN = "REDSHARK_PRODUCTION_RESET"
BATCH_SIZE = 400
AUTH_DELETE_BATCH_SIZE = 1000

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")
if hasattr(sys.stderr, "reconfigure"):
    sys.stderr.reconfigure(encoding="utf-8")


def repo_root() -> Path:
    return Path(__file__).resolve().parents[1]


def load_local_properties() -> dict[str, str]:
    path = repo_root() / "local.properties"
    values: dict[str, str] = {}
    if not path.exists():
        return values
    for raw_line in path.read_text(encoding="utf-8").splitlines():
        line = raw_line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, value = line.split("=", 1)
        values[key.strip()] = value.strip()
    return values


def config_value(name: str, local: dict[str, str]) -> str:
    return os.environ.get(name) or local.get(name, "")


def init_firebase(project_id: str) -> firestore.Client:
    key_path = Path(__file__).with_name("serviceAccountKey.json")
    if key_path.exists():
        cred = credentials.Certificate(str(key_path))
        print(f"[auth] Dùng service account: {key_path}")
    elif os.environ.get("GOOGLE_APPLICATION_CREDENTIALS"):
        cred = credentials.ApplicationDefault()
        print("[auth] Dùng GOOGLE_APPLICATION_CREDENTIALS")
    else:
        sys.exit(
            "Thiếu Firebase credentials. Đặt scripts/serviceAccountKey.json "
            "hoặc GOOGLE_APPLICATION_CREDENTIALS."
        )

    if not firebase_admin._apps:
        firebase_admin.initialize_app(cred, {"projectId": project_id})
    return firestore.client()


def iter_root_collections(db: firestore.Client):
    yield from db.collections()


def count_collection_tree(collection_ref) -> int:
    total = 0
    for doc in collection_ref.stream():
        total += 1
        for child in doc.reference.collections():
            total += count_collection_tree(child)
    return total


def delete_collection_tree(collection_ref, dry_run: bool) -> int:
    deleted = 0
    while True:
        docs = list(collection_ref.limit(BATCH_SIZE).stream())
        if not docs:
            return deleted

        for doc in docs:
            for child in doc.reference.collections():
                deleted += delete_collection_tree(child, dry_run)

        if dry_run:
            deleted += len(docs)
            return deleted

        batch = collection_ref._client.batch()
        for doc in docs:
            batch.delete(doc.reference)
        batch.commit()
        deleted += len(docs)
        print(f"  [{collection_ref.id}] đã xóa {deleted} document...")


def count_auth_users() -> int:
    return sum(1 for _ in auth.list_users().iterate_all())


def chunked(items: list[str], size: int) -> Iterable[list[str]]:
    for index in range(0, len(items), size):
        yield items[index : index + size]


def delete_auth_users(dry_run: bool) -> int:
    user_ids = [user.uid for user in auth.list_users().iterate_all()]
    if dry_run:
        return len(user_ids)
    for chunk in chunked(user_ids, AUTH_DELETE_BATCH_SIZE):
        result = auth.delete_users(chunk)
        if result.failure_count:
            for error in result.errors:
                print(f"  [auth] lỗi xóa uid index={error.index}: {error.reason}")
            raise RuntimeError("Xóa Firebase Auth users chưa hoàn tất.")
    return len(user_ids)


def r2_client(local: dict[str, str]):
    if boto3 is None:
        raise RuntimeError("Thiếu boto3. Cài đặt bằng: pip install boto3")

    endpoint = config_value("CLOUDFLARE_R2_ENDPOINT", local)
    access_key = config_value("CLOUDFLARE_R2_ACCESS_KEY_ID", local)
    secret_key = config_value("CLOUDFLARE_R2_SECRET_ACCESS_KEY", local)
    bucket = config_value("CLOUDFLARE_R2_BUCKET", local)

    missing = [
        name
        for name, value in {
            "CLOUDFLARE_R2_ENDPOINT": endpoint,
            "CLOUDFLARE_R2_ACCESS_KEY_ID": access_key,
            "CLOUDFLARE_R2_SECRET_ACCESS_KEY": secret_key,
            "CLOUDFLARE_R2_BUCKET": bucket,
        }.items()
        if not value
    ]
    if missing:
        raise RuntimeError(f"Thiếu cấu hình R2: {', '.join(missing)}")

    client = boto3.client(
        "s3",
        endpoint_url=endpoint,
        aws_access_key_id=access_key,
        aws_secret_access_key=secret_key,
        region_name="auto",
    )
    return client, bucket


def list_r2_keys(client, bucket: str) -> list[str]:
    keys: list[str] = []
    paginator = client.get_paginator("list_objects_v2")
    for page in paginator.paginate(Bucket=bucket):
        keys.extend(item["Key"] for item in page.get("Contents", []))
    return keys


def delete_r2_objects(local: dict[str, str], dry_run: bool) -> int:
    client, bucket = r2_client(local)
    keys = list_r2_keys(client, bucket)
    if dry_run:
        return len(keys)
    for chunk in chunked(keys, 1000):
        client.delete_objects(
            Bucket=bucket,
            Delete={"Objects": [{"Key": key} for key in chunk], "Quiet": True},
        )
        print(f"  [r2] đã xóa {len(chunk)} object...")
    return len(keys)


def dry_run(args) -> None:
    db = init_firebase(args.project)
    local = load_local_properties()
    print(f"[dry-run] Project: {args.project}")
    firestore_total = sum(count_collection_tree(col) for col in iter_root_collections(db))
    auth_total = count_auth_users()
    try:
        r2_total = delete_r2_objects(local, dry_run=True)
    except Exception as exc:
        r2_total = -1
        print(f"[dry-run] Không đọc được R2: {exc}")
    print(f"Firestore documents sẽ xóa: {firestore_total}")
    print(f"Firebase Auth users sẽ xóa: {auth_total}")
    print(f"R2 objects sẽ xóa: {r2_total if r2_total >= 0 else 'không xác định'}")


def reset(args) -> None:
    if args.confirm != CONFIRM_TOKEN:
        sys.exit(f"Thiếu xác nhận. Chạy lại với --confirm {CONFIRM_TOKEN}")
    if args.project != PROJECT_ID:
        sys.exit(f"Project phải là {PROJECT_ID}; nhận được {args.project}")

    db = init_firebase(args.project)
    local = load_local_properties()
    print(f"[reset] Project: {args.project}")

    deleted_docs = 0
    for collection in list(iter_root_collections(db)):
        deleted_docs += delete_collection_tree(collection, dry_run=False)
    print(f"[reset] Firestore đã xóa {deleted_docs} document.")

    deleted_users = delete_auth_users(dry_run=False)
    print(f"[reset] Firebase Auth đã xóa {deleted_users} user.")

    deleted_objects = delete_r2_objects(local, dry_run=False)
    print(f"[reset] R2 đã xóa {deleted_objects} object.")

    verify(args)


def verify(args) -> None:
    db = init_firebase(args.project)
    local = load_local_properties()
    firestore_total = sum(count_collection_tree(col) for col in iter_root_collections(db))
    auth_total = count_auth_users()
    r2_total = delete_r2_objects(local, dry_run=True)

    print(f"[verify] Firestore documents: {firestore_total}")
    print(f"[verify] Firebase Auth users: {auth_total}")
    print(f"[verify] R2 objects: {r2_total}")

    if firestore_total or auth_total or r2_total:
        raise SystemExit("Production chưa trắng dữ liệu.")
    print("[verify] Production đã trắng dữ liệu.")


def parse_args():
    parser = argparse.ArgumentParser(description="Reset RedShark production data.")
    parser.add_argument("command", choices=("dry-run", "reset", "verify"))
    parser.add_argument("--project", default=PROJECT_ID)
    parser.add_argument("--confirm", default="")
    return parser.parse_args()


def main() -> None:
    args = parse_args()
    if args.command == "dry-run":
        dry_run(args)
    elif args.command == "reset":
        reset(args)
    elif args.command == "verify":
        verify(args)


if __name__ == "__main__":
    main()
