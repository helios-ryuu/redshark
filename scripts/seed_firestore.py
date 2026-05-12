#!/usr/bin/env python3
"""
RedShark Firestore — Reset & Seed Script
Project : redshark-application
Collections: users, ideas, issues, comments, notifications,
             conversations, messages   (tags/skills: không seed riêng)

Cài đặt:
    pip install firebase-admin

Xác thực (chọn 1 trong 2):
  A) Đặt file service account vào:  scripts/serviceAccountKey.json
     (Firebase Console → Project Settings → Service Accounts → Generate new private key)
  B) Set biến môi trường:
     set GOOGLE_APPLICATION_CREDENTIALS=path\\to\\serviceAccountKey.json

Chạy:
    python scripts/seed_firestore.py
"""

import os, sys, uuid, datetime, random
import firebase_admin
from firebase_admin import credentials, firestore

# ─── Config ────────────────────────────────────────────────────────────────
PROJECT_ID  = "redshark-application"
BATCH_SIZE  = 400          # Firestore batch limit = 500; dùng 400 để an toàn

COLLECTIONS = [
    "users", "ideas", "issues", "comments",
    "notifications", "conversations", "messages",
]

# ─── Init ───────────────────────────────────────────────────────────────────
def init():
    key = os.path.join(os.path.dirname(__file__), "serviceAccountKey.json")
    if os.path.exists(key):
        cred = credentials.Certificate(key)
        print(f"[auth] Dùng service account: {key}")
    elif os.environ.get("GOOGLE_APPLICATION_CREDENTIALS"):
        cred = credentials.ApplicationDefault()
        print("[auth] Dùng GOOGLE_APPLICATION_CREDENTIALS")
    else:
        sys.exit(
            "\n[ERROR] Không tìm thấy credentials.\n"
            "  Cách 1: Đặt scripts/serviceAccountKey.json\n"
            "  Cách 2: set GOOGLE_APPLICATION_CREDENTIALS=path\\to\\key.json\n"
        )
    firebase_admin.initialize_app(cred, {"projectId": PROJECT_ID})
    return firestore.client()


# ─── Helpers ────────────────────────────────────────────────────────────────
def uid() -> str:
    return str(uuid.uuid4())

def ts(days_ago: float = 0, hours_ago: float = 0) -> datetime.datetime:
    """Trả về datetime UTC (Firestore tự convert thành Timestamp)."""
    delta = datetime.timedelta(days=days_ago, hours=hours_ago)
    return datetime.datetime.now(tz=datetime.timezone.utc) - delta

def delete_collection(db, name: str):
    col = db.collection(name)
    deleted = 0
    while True:
        docs = col.limit(BATCH_SIZE).stream()
        batch = db.batch()
        count = 0
        for doc in docs:
            batch.delete(doc.reference)
            count += 1
        if count == 0:
            break
        batch.commit()
        deleted += count
        print(f"  [{name}] xoá {deleted}...")
    print(f"  [{name}] ✓ xoá {deleted} document")


def write_batch(db, col_name: str, docs: list[dict]):
    """docs = list of {"id": str, "data": dict}"""
    col = db.collection(col_name)
    for i in range(0, len(docs), BATCH_SIZE):
        batch = db.batch()
        for item in docs[i:i + BATCH_SIZE]:
            ref = col.document(item["id"])
            batch.set(ref, item["data"])
        batch.commit()
    print(f"  [{col_name}] ✓ seed {len(docs)} document")


# ═══════════════════════════════════════════════════════════════════════════
#  SEED DATA
# ═══════════════════════════════════════════════════════════════════════════

# ── Users ────────────────────────────────────────────────────────────────
USER_SY   = "seed_user_sy"
USER_HAI  = "seed_user_hai"
USER_NAM  = "seed_user_nam"
USER_LAN  = "seed_user_lan"
USER_MINH = "seed_user_minh"
ALL_USERS = [USER_SY, USER_HAI, USER_NAM, USER_LAN, USER_MINH]

USERS = [
    {"id": USER_SY,   "data": {
        "email": "ngo.tien.sy@university.edu.vn",
        "displayName": "Ngô Tiến Sỹ",
        "avatarUrl": None,
        "bio": "Lead dev. Thích clean architecture và coffee.",
        "skills": ["Android", "Backend", "DevOps"],
        "createdAt": ts(30), "updatedAt": ts(2),
    }},
    {"id": USER_HAI,  "data": {
        "email": "nguyen.tien.hai@university.edu.vn",
        "displayName": "Nguyễn Tiến Hải",
        "avatarUrl": None,
        "bio": "UI/UX + Android. Thích thiết kế trải nghiệm người dùng.",
        "skills": ["Android", "UI/UX", "Product"],
        "createdAt": ts(30), "updatedAt": ts(3),
    }},
    {"id": USER_NAM,  "data": {
        "email": "tran.huu.nam@university.edu.vn",
        "displayName": "Trần Hữu Nam",
        "avatarUrl": None,
        "bio": "Full-stack. Chuyên tích hợp API và notification hệ thống.",
        "skills": ["Android", "Web", "Backend"],
        "createdAt": ts(30), "updatedAt": ts(1),
    }},
    {"id": USER_LAN,  "data": {
        "email": "pham.thi.lan@university.edu.vn",
        "displayName": "Phạm Thị Lan",
        "avatarUrl": None,
        "bio": "Product designer. Tập trung vào nghiên cứu người dùng.",
        "skills": ["UI/UX", "Product", "Data"],
        "createdAt": ts(28), "updatedAt": ts(5),
    }},
    {"id": USER_MINH, "data": {
        "email": "le.van.minh@university.edu.vn",
        "displayName": "Lê Văn Minh",
        "avatarUrl": None,
        "bio": "DevOps enthusiast. Quan tâm đến hiệu năng và bảo mật.",
        "skills": ["Backend", "DevOps", "Security"],
        "createdAt": ts(25), "updatedAt": ts(4),
    }},
]

# ── Tags (UUID cố định dùng cho ideas.tagIds) ────────────────────────────
TAG_ANDROID = "aaaaaaaa-0001-0001-0001-000000000001"
TAG_WEB     = "aaaaaaaa-0001-0001-0001-000000000002"
TAG_BACKEND = "aaaaaaaa-0001-0001-0001-000000000003"
TAG_UIUX    = "aaaaaaaa-0001-0001-0001-000000000004"
TAG_ML      = "aaaaaaaa-0001-0001-0001-000000000005"
TAG_DATA    = "aaaaaaaa-0001-0001-0001-000000000006"

# ── Ideas ────────────────────────────────────────────────────────────────
# 12 ideas: 8 ACTIVE, 2 CLOSED, 2 CANCELLED
IDEA_1  = uid(); IDEA_2  = uid(); IDEA_3  = uid(); IDEA_4  = uid()
IDEA_5  = uid(); IDEA_6  = uid(); IDEA_7  = uid(); IDEA_8  = uid()
IDEA_9  = uid(); IDEA_10 = uid(); IDEA_11 = uid(); IDEA_12 = uid()

ACTIVE_IDEAS = [IDEA_1, IDEA_2, IDEA_3, IDEA_4, IDEA_5, IDEA_6, IDEA_7, IDEA_8]

IDEAS = [
    # ── ACTIVE ──────────────────────────────────────────────────────────
    {"id": IDEA_1, "data": {
        "authorId": USER_SY,
        "title": "Hệ thống quản lý tài liệu học tập thông minh",
        "description": (
            "Xây dựng nền tảng cho phép sinh viên upload, tổ chức và tìm kiếm tài liệu học tập "
            "bằng AI. Hỗ trợ OCR, tóm tắt tự động và gợi ý tài liệu liên quan dựa trên môn học. "
            "Tích hợp với hệ thống LMS của trường để đồng bộ lịch học và bài tập."
        ),
        "status": "ACTIVE",
        "tagIds": [TAG_ANDROID, TAG_BACKEND, TAG_ML],
        "collaboratorIds": [USER_HAI, USER_NAM],
        "createdAt": ts(20), "updatedAt": ts(2), "deletedAt": None,
    }},
    {"id": IDEA_2, "data": {
        "authorId": USER_HAI,
        "title": "App theo dõi thói quen và sức khỏe sinh viên",
        "description": (
            "Ứng dụng mobile giúp sinh viên theo dõi thói quen hằng ngày: giấc ngủ, uống nước, "
            "tập thể dục và thời gian học. Có tính năng nhắc nhở thông minh và báo cáo tuần. "
            "Gamification với huy hiệu và bảng xếp hạng giữa bạn bè."
        ),
        "status": "ACTIVE",
        "tagIds": [TAG_ANDROID, TAG_UIUX],
        "collaboratorIds": [USER_LAN],
        "createdAt": ts(18), "updatedAt": ts(3), "deletedAt": None,
    }},
    {"id": IDEA_3, "data": {
        "authorId": USER_NAM,
        "title": "Nền tảng kết nối mentor và sinh viên IT",
        "description": (
            "Platform cho phép sinh viên IT tìm kiếm và kết nối với các mentor là senior developer, "
            "tech lead trong ngành. Hỗ trợ lên lịch 1-1 session, review code, định hướng career. "
            "Tích hợp video call và shared editor để pair programming."
        ),
        "status": "ACTIVE",
        "tagIds": [TAG_WEB, TAG_BACKEND],
        "collaboratorIds": [USER_MINH],
        "createdAt": ts(15), "updatedAt": ts(1), "deletedAt": None,
    }},
    {"id": IDEA_4, "data": {
        "authorId": USER_LAN,
        "title": "Công cụ design system tự động cho mobile app",
        "description": (
            "Xây dựng CLI tool và Figma plugin để tự động generate design tokens (màu sắc, "
            "typography, spacing) từ file thiết kế và export sang code Android/iOS. "
            "Đảm bảo consistency giữa design và implementation trong team."
        ),
        "status": "ACTIVE",
        "tagIds": [TAG_UIUX, TAG_ANDROID],
        "collaboratorIds": [USER_HAI],
        "createdAt": ts(12), "updatedAt": ts(2), "deletedAt": None,
    }},
    {"id": IDEA_5, "data": {
        "authorId": USER_MINH,
        "title": "Pipeline CI/CD tự động cho dự án Android",
        "description": (
            "Thiết lập hệ thống CI/CD hoàn chỉnh cho dự án Android sử dụng GitHub Actions. "
            "Tự động chạy unit test, lint check, build APK, deploy lên Firebase App Distribution. "
            "Tích hợp Slack notification cho mỗi build và pull request."
        ),
        "status": "ACTIVE",
        "tagIds": [TAG_ANDROID, TAG_BACKEND],
        "collaboratorIds": [USER_SY],
        "createdAt": ts(10), "updatedAt": ts(1), "deletedAt": None,
    }},
    {"id": IDEA_6, "data": {
        "authorId": USER_SY,
        "title": "Chatbot hỗ trợ học lập trình cho người mới bắt đầu",
        "description": (
            "AI chatbot trả lời câu hỏi lập trình theo ngữ cảnh, giải thích code, "
            "gợi ý tài liệu học và bài tập phù hợp với trình độ. "
            "Hỗ trợ Python, JavaScript, Kotlin. Fine-tune trên dataset lập trình tiếng Việt."
        ),
        "status": "ACTIVE",
        "tagIds": [TAG_ML, TAG_BACKEND, TAG_WEB],
        "collaboratorIds": [],
        "createdAt": ts(8), "updatedAt": ts(0.5), "deletedAt": None,
    }},
    {"id": IDEA_7, "data": {
        "authorId": USER_HAI,
        "title": "Dashboard phân tích code quality realtime",
        "description": (
            "Web dashboard hiển thị metrics code quality của dự án: coverage, complexity, "
            "duplications, tech debt. Tự động kéo data từ SonarQube và GitHub. "
            "Gửi weekly digest report cho toàn team qua email."
        ),
        "status": "ACTIVE",
        "tagIds": [TAG_WEB, TAG_DATA],
        "collaboratorIds": [USER_NAM, USER_MINH],
        "createdAt": ts(6), "updatedAt": ts(0.5), "deletedAt": None,
    }},
    {"id": IDEA_8, "data": {
        "authorId": USER_NAM,
        "title": "App chia sẻ ghi chú và mindmap cho nhóm học",
        "description": (
            "Ứng dụng collaborative note-taking với tính năng mindmap, "
            "real-time sync giữa các thành viên trong nhóm học. "
            "Hỗ trợ Markdown, LaTeX cho công thức toán học, và export sang PDF."
        ),
        "status": "ACTIVE",
        "tagIds": [TAG_ANDROID, TAG_UIUX],
        "collaboratorIds": [USER_LAN],
        "createdAt": ts(4), "updatedAt": ts(0.2), "deletedAt": None,
    }},
    # ── CLOSED ──────────────────────────────────────────────────────────
    {"id": IDEA_9, "data": {
        "authorId": USER_LAN,
        "title": "Hệ thống đặt phòng học nhóm online",
        "description": "Platform cho phép đặt phòng học nhóm tại thư viện và cơ sở trường.",
        "status": "CLOSED",
        "tagIds": [TAG_WEB, TAG_BACKEND],
        "collaboratorIds": [USER_SY],
        "createdAt": ts(25), "updatedAt": ts(10), "deletedAt": None,
    }},
    {"id": IDEA_10, "data": {
        "authorId": USER_MINH,
        "title": "Tool tự động generate báo cáo thực tập",
        "description": "Sinh báo cáo thực tập từ template Word, tích hợp với Google Docs API.",
        "status": "CLOSED",
        "tagIds": [TAG_BACKEND],
        "collaboratorIds": [],
        "createdAt": ts(22), "updatedAt": ts(8), "deletedAt": None,
    }},
    # ── CANCELLED ────────────────────────────────────────────────────────
    {"id": IDEA_11, "data": {
        "authorId": USER_SY,
        "title": "NFT marketplace cho đồ án sinh viên",
        "description": "Cho phép sinh viên mint đồ án thành NFT và bán/trao đổi.",
        "status": "CANCELLED",
        "tagIds": [TAG_WEB, TAG_BACKEND],
        "collaboratorIds": [],
        "createdAt": ts(28), "updatedAt": ts(20), "deletedAt": None,
    }},
    {"id": IDEA_12, "data": {
        "authorId": USER_HAI,
        "title": "Game mobile học tiếng Anh qua meme",
        "description": "Học vocabulary qua meme và video viral. Quá phức tạp để thực hiện trong thời hạn.",
        "status": "CANCELLED",
        "tagIds": [TAG_ANDROID, TAG_UIUX],
        "collaboratorIds": [],
        "createdAt": ts(27), "updatedAt": ts(18), "deletedAt": None,
    }},
]

# ── Issues ───────────────────────────────────────────────────────────────
def make_issues() -> list[dict]:
    items = []

    def add(idea_id, author, assignee, title, desc, status, priority, days_ago, updated_ago):
        items.append({"id": uid(), "data": {
            "ideaId": idea_id,
            "authorId": author,
            "assigneeId": assignee,
            "title": title,
            "description": desc,
            "status": status,
            "priority": priority,
            "createdAt": ts(days_ago),
            "updatedAt": ts(updated_ago),
            "deletedAt": None,
        }})

    # IDEA_1 — Quản lý tài liệu thông minh (Sỹ)
    add(IDEA_1, USER_SY, USER_HAI, "Tích hợp Firebase Storage cho upload file",
        "Kết nối Firebase Storage, validate file type/size, generate download URL.", "CLOSED", "HIGH", 19, 10)
    add(IDEA_1, USER_HAI, USER_SY, "Thiết kế màn hình upload và quản lý tài liệu",
        "UI theo Material3: drag-drop area, progress bar, danh sách tài liệu dạng grid.", "IN_PROGRESS", "HIGH", 18, 1)
    add(IDEA_1, USER_NAM, USER_NAM, "Implement OCR API wrapper",
        "Gọi Google Vision API để extract text từ ảnh/PDF. Cache kết quả trên Firestore.", "OPEN", "MEDIUM", 17, 2)
    add(IDEA_1, USER_SY, None, "Tích hợp OpenAI GPT để tóm tắt tài liệu",
        "Gửi text trích xuất lên GPT-4o-mini, trả về 200 từ tóm tắt. Xử lý rate limit.", "OPEN", "MEDIUM", 16, 3)

    # IDEA_2 — App theo dõi thói quen (Hải)
    add(IDEA_2, USER_HAI, USER_LAN, "Thiết kế onboarding flow",
        "3 màn hình giới thiệu tính năng chính, cho phép set mục tiêu hằng ngày.", "CLOSED", "HIGH", 17, 8)
    add(IDEA_2, USER_LAN, USER_HAI, "Implement reminder notification thông minh",
        "Dùng WorkManager để lên lịch thông báo dựa trên habit pattern của user.", "IN_PROGRESS", "HIGH", 16, 1)
    add(IDEA_2, USER_HAI, None, "Tích hợp Health Connect API",
        "Đọc dữ liệu bước chân và giờ ngủ từ Health Connect, hiển thị trong app.", "OPEN", "MEDIUM", 15, 2)
    add(IDEA_2, USER_LAN, USER_LAN, "Thiết kế màn hình báo cáo tuần",
        "Chart hiển thị completion rate 7 ngày, so sánh với tuần trước.", "OPEN", "LOW", 14, 3)

    # IDEA_3 — Kết nối mentor (Nam)
    add(IDEA_3, USER_NAM, USER_MINH, "Thiết kế schema dữ liệu mentor-mentee",
        "Collections: mentors, sessions, reviews. Firestore rules để bảo vệ dữ liệu.", "CLOSED", "HIGH", 14, 7)
    add(IDEA_3, USER_MINH, USER_NAM, "Implement tính năng đặt lịch session",
        "Calendar picker, check availability của mentor, gửi email confirmation.", "IN_PROGRESS", "HIGH", 13, 1)
    add(IDEA_3, USER_NAM, None, "Tích hợp WebRTC cho video call",
        "Peer-to-peer video call trong app, fallback sang Jitsi Meet nếu cần.", "OPEN", "MEDIUM", 12, 2)
    add(IDEA_3, USER_MINH, USER_MINH, "Hệ thống đánh giá và review mentor",
        "Form 5 sao + comment sau mỗi session. Tổng hợp điểm trên profile mentor.", "OPEN", "LOW", 11, 3)

    # IDEA_4 — Design system tool (Lan)
    add(IDEA_4, USER_LAN, USER_HAI, "Parser Figma Variables API",
        "Đọc design tokens từ Figma Variables, map sang Material3 color scheme.", "IN_PROGRESS", "HIGH", 11, 1)
    add(IDEA_4, USER_HAI, USER_LAN, "Code generator cho Kotlin/Compose",
        "Template engine tạo ra Color.kt, Type.kt, Dimens.kt từ token đầu vào.", "OPEN", "HIGH", 10, 2)
    add(IDEA_4, USER_LAN, None, "CLI interface và npm package",
        "Đóng gói thành CLI `rsdsg generate`, publish lên npm, viết README.", "OPEN", "MEDIUM", 9, 3)

    # IDEA_5 — CI/CD pipeline (Minh)
    add(IDEA_5, USER_MINH, USER_SY, "Cấu hình GitHub Actions workflow",
        "Workflow chạy khi push lên develop/main: lint → test → build. Matrix cho API 26/33.", "CLOSED", "HIGH", 9, 4)
    add(IDEA_5, USER_SY, USER_MINH, "Tích hợp Firebase App Distribution",
        "Auto-deploy APK debug lên App Distribution sau mỗi merge vào develop.", "IN_PROGRESS", "MEDIUM", 8, 1)
    add(IDEA_5, USER_MINH, None, "Slack notification cho build status",
        "Gửi message vào channel #builds với link APK và link test report.", "OPEN", "LOW", 7, 2)

    # IDEA_6 — AI Chatbot (Sỹ)
    add(IDEA_6, USER_SY, None, "Fine-tune model trên dataset lập trình tiếng Việt",
        "Thu thập 10k Q&A Stack Overflow, dịch sang tiếng Việt, fine-tune GPT-3.5.", "OPEN", "HIGH", 7, 1)
    add(IDEA_6, USER_SY, USER_SY, "Thiết kế conversation flow",
        "State machine: greeting → topic detection → answer → follow-up question.", "IN_PROGRESS", "MEDIUM", 6, 0.5)
    add(IDEA_6, USER_SY, None, "Backend API và rate limiting",
        "FastAPI endpoint, Redis rate limiting 20 req/min/user, log conversation.", "OPEN", "MEDIUM", 5, 1)

    # IDEA_7 — Dashboard code quality (Hải)
    add(IDEA_7, USER_HAI, USER_NAM, "Kết nối SonarQube API",
        "Pull metrics coverage, duplications, code smells mỗi giờ qua cron job.", "IN_PROGRESS", "HIGH", 5, 0.5)
    add(IDEA_7, USER_NAM, USER_MINH, "Xây dựng chart components",
        "React + Recharts: line chart coverage, bar chart issues theo severity.", "OPEN", "MEDIUM", 4, 1)
    add(IDEA_7, USER_MINH, None, "Hệ thống gửi weekly digest email",
        "Nodemailer + handlebars template. Chạy vào 9h sáng thứ Hai hàng tuần.", "OPEN", "LOW", 3, 1)

    # IDEA_8 — App ghi chú nhóm (Nam)
    add(IDEA_8, USER_NAM, USER_LAN, "Real-time sync với Firestore",
        "Conflict resolution với Operational Transformation đơn giản. Debounce 500ms.", "IN_PROGRESS", "HIGH", 3, 0.5)
    add(IDEA_8, USER_LAN, USER_NAM, "Thiết kế editor mindmap",
        "Canvas-based mindmap với drag-drop node, zoom, export PNG.", "OPEN", "HIGH", 2, 0.5)
    add(IDEA_8, USER_NAM, None, "Parser LaTeX inline cho công thức",
        "Dùng KaTeX render công thức toán trong Markdown editor.", "OPEN", "MEDIUM", 1, 0.2)

    return items

# ── Comments ─────────────────────────────────────────────────────────────
def make_comments() -> list[dict]:
    items = []

    def add(idea_id, author, content, days_ago):
        items.append({"id": uid(), "data": {
            "ideaId": idea_id,
            "authorId": author,
            "content": content,
            "createdAt": ts(days_ago),
        }})

    # IDEA_1
    add(IDEA_1, USER_HAI,  "Ý tưởng hay! Mình đã có kinh nghiệm với Google Vision API, có thể contribute phần OCR.", 19)
    add(IDEA_1, USER_NAM,  "Cần cân nhắc chi phí API nếu user upload nhiều. Nên có caching layer.", 18)
    add(IDEA_1, USER_SY,   "Đồng ý về caching. Sẽ dùng Firestore để lưu kết quả OCR theo hash của file.", 17)
    add(IDEA_1, USER_LAN,  "Phần UI upload cần UX tốt hơn, user thường không biết file nào đã xử lý xong.", 16)
    add(IDEA_1, USER_HAI,  "Mình sẽ làm progress indicator với animation rõ ràng hơn. Xem mockup ở Figma.", 15)

    # IDEA_2
    add(IDEA_2, USER_SY,   "Health Connect API khá mới, cần test kỹ trên các device khác nhau.", 17)
    add(IDEA_2, USER_NAM,  "Gamification rất hay! Nhưng cần cẩn thận về dark pattern, không nên tạo addiction.", 16)
    add(IDEA_2, USER_LAN,  "Đồng ý với Nam. Thiết kế theo hướng healthy engagement, có option tắt streak pressure.", 15)
    add(IDEA_2, USER_MINH, "WorkManager cho reminder rất đúng. Cần handle Doze mode và battery optimization.", 14)

    # IDEA_3
    add(IDEA_3, USER_SY,   "Market này đang thiếu hụt ở Việt Nam. LinkedIn Learning quá đắt cho sinh viên.", 14)
    add(IDEA_3, USER_HAI,  "WebRTC phức tạp hơn tưởng. Có thể dùng Daily.co API để tiết kiệm thời gian.", 13)
    add(IDEA_3, USER_NAM,  "Cảm ơn Hải! Đang nghiên cứu Daily.co vs Agora. Sẽ update sau khi so sánh xong.", 12)
    add(IDEA_3, USER_LAN,  "Nên có tính năng record session để mentee xem lại. Cần hỏi về privacy.", 11)

    # IDEA_4
    add(IDEA_4, USER_SY,   "Chính xác là thứ team cần! Bao giờ có bản demo đầu tiên?", 11)
    add(IDEA_4, USER_NAM,  "Figma Variables API mới ra, tài liệu còn sparse. Có thể sẽ có breaking changes.", 10)
    add(IDEA_4, USER_LAN,  "Đang làm rồi. Dự kiến tuần tới có prototype hoạt động được.", 9)

    # IDEA_5
    add(IDEA_5, USER_HAI,  "CI/CD là must-have. Bao nhiêu phút một build thường mất?", 9)
    add(IDEA_5, USER_MINH, "Hiện tại ~12 phút. Đang optimize bằng cách cache Gradle dependencies.", 8)
    add(IDEA_5, USER_SY,   "Có thể dùng Gradle build cache remote nữa, tiết kiệm thêm 3-4 phút.", 7)

    # IDEA_6
    add(IDEA_6, USER_HAI,  "Dữ liệu tiếng Việt về lập trình khá hiếm. Có thể crowdsource từ cộng đồng không?", 7)
    add(IDEA_6, USER_NAM,  "Có thể dùng synthetic data generation. GPT-4 tạo Q&A rồi verify thủ công.", 6)
    add(IDEA_6, USER_SY,   "Hybrid approach: synthetic + real Stack Overflow Vietnamese posts. Đang thử.", 5)

    # IDEA_7
    add(IDEA_7, USER_SY,   "Dashboard này sẽ rất hữu ích! Có thể add metric về PR review time không?", 5)
    add(IDEA_7, USER_HAI,  "Hay đó! Sẽ add vào backlog. GitHub API có endpoint cho PR analytics.", 4)
    add(IDEA_7, USER_MINH, "Cần authentication với SonarQube enterprise. Hỏi IT xem có token không.", 3)

    # IDEA_8
    add(IDEA_8, USER_SY,   "Notion và Obsidian đã làm tốt phần này. Điểm khác biệt của mình là gì?", 3)
    add(IDEA_8, USER_NAM,  "Real-time collaboration cho nhóm học là điểm chính. Notion team plan khá đắt.", 2)
    add(IDEA_8, USER_LAN,  "UX của mindmap trong mobile cần nghiên cứu kỹ. Touch target và gesture khó hơn web.", 1)

    return items

# ── Notifications ─────────────────────────────────────────────────────────
def make_notifications() -> list[dict]:
    items = []

    def add(recipient, actor, ntype, target_type, target_id, message, is_read, days_ago):
        items.append({"id": uid(), "data": {
            "recipientId": recipient,
            "actorId": actor,
            "type": ntype,
            "targetType": target_type,
            "targetId": target_id,
            "message": message,
            "isRead": is_read,
            "createdAt": ts(days_ago),
        }})

    # ISSUE_CREATED
    add(USER_SY,  USER_HAI,  "ISSUE_CREATED", "IDEA", IDEA_1,
        "Hải đã tạo issue mới trên ý tưởng của bạn: 'Thiết kế màn hình upload'", True,  18)
    add(USER_SY,  USER_NAM,  "ISSUE_CREATED", "IDEA", IDEA_1,
        "Nam đã tạo issue mới: 'Implement OCR API wrapper'", True,  17)
    add(USER_HAI, USER_LAN,  "ISSUE_CREATED", "IDEA", IDEA_2,
        "Lan đã tạo issue mới trên ý tưởng của bạn: 'Thiết kế màn hình báo cáo'", True,  14)
    add(USER_NAM, USER_MINH, "ISSUE_CREATED", "IDEA", IDEA_3,
        "Minh đã tạo issue mới: 'Hệ thống đặt lịch session'", False, 13)
    add(USER_SY,  USER_MINH, "ISSUE_CREATED", "IDEA", IDEA_5,
        "Minh đã tạo issue: 'Cấu hình GitHub Actions workflow'", True,  9)
    add(USER_HAI, USER_NAM,  "ISSUE_CREATED", "IDEA", IDEA_7,
        "Nam đã tạo issue: 'Xây dựng chart components'", False, 4)
    add(USER_NAM, USER_LAN,  "ISSUE_CREATED", "IDEA", IDEA_8,
        "Lan đã tạo issue: 'Thiết kế editor mindmap'", False, 2)

    # COLLAB_REQUEST
    add(USER_SY,  USER_LAN,  "COLLAB_REQUEST", "IDEA", IDEA_1,
        "Lan muốn tham gia cộng tác ý tưởng của bạn: 'Hệ thống quản lý tài liệu'", True,  15)
    add(USER_HAI, USER_MINH, "COLLAB_REQUEST", "IDEA", IDEA_2,
        "Minh muốn tham gia cộng tác: 'App theo dõi thói quen'", False, 5)
    add(USER_LAN, USER_SY,   "COLLAB_REQUEST", "IDEA", IDEA_4,
        "Sỹ muốn tham gia ý tưởng của bạn: 'Công cụ design system'", False, 3)

    # COLLAB_ACCEPTED / REJECTED
    add(USER_NAM, USER_SY,   "COLLAB_ACCEPTED", "IDEA", IDEA_1,
        "Sỹ đã chấp nhận yêu cầu tham gia của bạn!", True,  14)
    add(USER_HAI, USER_SY,   "COLLAB_ACCEPTED", "IDEA", IDEA_1,
        "Sỹ đã chấp nhận yêu cầu tham gia của bạn!", True,  13)
    add(USER_LAN, USER_HAI,  "COLLAB_ACCEPTED", "IDEA", IDEA_2,
        "Hải đã chấp nhận yêu cầu tham gia của bạn!", True,  12)
    add(USER_MINH, USER_NAM, "COLLAB_REJECTED", "IDEA", IDEA_3,
        "Nam đã từ chối yêu cầu tham gia (ý tưởng đã đủ thành viên).", True,  10)

    # COMMENT
    add(USER_SY,  USER_HAI,  "COMMENT", "IDEA", IDEA_1,
        "Hải đã bình luận trên ý tưởng của bạn: 'Ý tưởng hay! Mình đã có kinh nghiệm...'", True,  19)
    add(USER_SY,  USER_NAM,  "COMMENT", "IDEA", IDEA_1,
        "Nam đã bình luận: 'Cần cân nhắc chi phí API nếu user upload nhiều...'", True,  18)
    add(USER_HAI, USER_SY,   "COMMENT", "IDEA", IDEA_2,
        "Sỹ đã bình luận trên ý tưởng của bạn: 'Health Connect API khá mới...'", True,  17)
    add(USER_HAI, USER_NAM,  "COMMENT", "IDEA", IDEA_7,
        "Nam đã bình luận: 'Dashboard này sẽ rất hữu ích!'", False, 5)
    add(USER_NAM, USER_SY,   "COMMENT", "IDEA", IDEA_8,
        "Sỹ đã bình luận: 'Notion và Obsidian đã làm tốt phần này...'", False, 3)

    return items

# ── Conversations + Messages ──────────────────────────────────────────────
def make_conversations_and_messages():
    convs  = []
    msgs   = []

    def conv(uid_a, uid_b, preview, sender_id, has_unread, days_ago):
        cid = uid()
        convs.append({"id": cid, "data": {
            "type": "DIRECT",
            "participantIds": [uid_a, uid_b],
            "lastMessageAt": ts(days_ago, 0.1),
            "lastMessagePreview": preview[:80],
            "lastMessageSenderId": sender_id,
            "hasUnread": has_unread,
            "createdAt": ts(days_ago + 2),
        }})
        return cid

    def msg(conv_id, sender, content, days_ago):
        msgs.append({"id": uid(), "data": {
            "conversationId": conv_id,
            "senderId": sender,
            "content": content,
            "status": "SENT",
            "createdAt": ts(days_ago),
        }})

    # Conv 1: Sỹ ↔ Hải
    c1 = conv(USER_SY, USER_HAI, "Ok mình sẽ review PR của bạn tối nay.", USER_SY, False, 0.3)
    msg(c1, USER_HAI, "Sỹ ơi, bạn review PR #23 chưa? Mình đang chờ để merge.", 1.5)
    msg(c1, USER_SY,  "Chưa kịp, hôm nay bận fix bug production. Tối mình xem nhé.", 1.4)
    msg(c1, USER_HAI, "Ok không sao. Bạn nhớ check cái phần lazy loading mình thêm nhé.", 1.3)
    msg(c1, USER_SY,  "Ừ, mình để ý rồi. Bạn có viết unit test cho phần đó chưa?", 1.2)
    msg(c1, USER_HAI, "Chưa, đang viết. Sẽ push thêm tối nay.", 1.1)
    msg(c1, USER_SY,  "Ok mình sẽ review PR của bạn tối nay.", 0.3)

    # Conv 2: Nam ↔ Minh
    c2 = conv(USER_NAM, USER_MINH, "Bạn đã test WebRTC trên thiết bị thật chưa?", USER_MINH, True, 0.5)
    msg(c2, USER_NAM,  "Minh, mình đang stuck với WebRTC peer connection. Bạn có kinh nghiệm không?", 2.0)
    msg(c2, USER_MINH, "Có chút. Vấn đề thường ở STUN/TURN server config. Bạn đang dùng server nào?", 1.9)
    msg(c2, USER_NAM,  "Mình dùng Google STUN server free. Có vẻ không stable.", 1.8)
    msg(c2, USER_MINH, "Free STUN không ổn định đâu. Nên setup Coturn server riêng hoặc dùng Twilio.", 1.7)
    msg(c2, USER_NAM,  "Twilio tốn tiền không? Budget project mình eo hẹp.", 1.6)
    msg(c2, USER_MINH, "Twilio có free tier 5000 phút/tháng. Đủ dùng cho development.", 1.5)
    msg(c2, USER_NAM,  "Ok mình thử. Cảm ơn bạn nhiều!", 1.4)
    msg(c2, USER_MINH, "Không có gì. À bạn đã test trên thiết bị thật chưa? Emulator WebRTC hay bị lỗi.", 0.5)

    # Conv 3: Hải ↔ Lan
    c3 = conv(USER_HAI, USER_LAN, "File Figma mình share rồi, bạn xem nhé!", USER_LAN, True, 0.8)
    msg(c3, USER_HAI, "Lan ơi, bạn có thể share file Figma design system không? Mình cần tham khảo cho issue.", 2.5)
    msg(c3, USER_LAN, "Có chứ! Để mình set permission cho bạn. Email bạn dùng với Figma là gì?", 2.4)
    msg(c3, USER_HAI, "hai.nguyen@gmail.com nhé.", 2.3)
    msg(c3, USER_LAN, "Xong! Mình đã invite. Bạn check email nhé.", 2.2)
    msg(c3, USER_HAI, "Nhận được rồi, cảm ơn Lan! Design trông rất clean.", 2.0)
    msg(c3, USER_LAN, "Cảm ơn! Mình đang update component thêm nữa. Bạn có góp ý gì không?", 1.9)
    msg(c3, USER_HAI, "Màu primary hơi tối trên dark mode. Bạn thử lighten lên 10% xem.", 1.7)
    msg(c3, USER_LAN, "File Figma mình share rồi, bạn xem nhé!", 0.8)

    # Conv 4: Sỹ ↔ Minh
    c4 = conv(USER_SY, USER_MINH, "Build time giờ còn 8 phút rồi, tốt hơn nhiều!", USER_MINH, False, 1.0)
    msg(c4, USER_SY,   "Minh, pipeline CI/CD đã ổn chưa? Sáng nay mình push mà chưa thấy build.", 2.0)
    msg(c4, USER_MINH, "Có issue với secrets không? Bạn kiểm tra GitHub Actions secrets chưa?", 1.9)
    msg(c4, USER_SY,   "Ồ đúng rồi, mình chưa add GOOGLE_SERVICES_JSON vào secrets.", 1.8)
    msg(c4, USER_MINH, "Base64 encode file google-services.json rồi paste vào secret GOOGLE_SERVICES_JSON nhé.", 1.7)
    msg(c4, USER_SY,   "Done! Build đang chạy. Mất bao lâu?", 1.6)
    msg(c4, USER_MINH, "Khoảng 12-15 phút lần đầu. Mình đang optimize cache Gradle.", 1.5)
    msg(c4, USER_SY,   "Ok mình chờ. Cảm ơn bạn!", 1.4)
    msg(c4, USER_MINH, "Build time giờ còn 8 phút rồi, tốt hơn nhiều!", 1.0)

    return convs, msgs


# ═══════════════════════════════════════════════════════════════════════════
#  MAIN
# ═══════════════════════════════════════════════════════════════════════════
def main():
    print("=" * 60)
    print("  RedShark Firestore Reset & Seed")
    print(f"  Project: {PROJECT_ID}")
    print("=" * 60)

    db = init()

    # ── 1. Xoá toàn bộ dữ liệu cũ ─────────────────────────────────────
    print("\n[1/3] Xoá dữ liệu cũ...")
    for col in COLLECTIONS:
        delete_collection(db, col)

    # ── 2. Seed ────────────────────────────────────────────────────────
    print("\n[2/3] Seed dữ liệu mới...")
    convs, msgs = make_conversations_and_messages()

    write_batch(db, "users",         USERS)
    write_batch(db, "ideas",         IDEAS)
    write_batch(db, "issues",        make_issues())
    write_batch(db, "comments",      make_comments())
    write_batch(db, "notifications", make_notifications())
    write_batch(db, "conversations", convs)
    write_batch(db, "messages",      msgs)

    # ── 3. Summary ─────────────────────────────────────────────────────
    print("\n[3/3] Hoàn tất!")
    print(f"  users:         {len(USERS)}")
    print(f"  ideas:         {len(IDEAS)}  (8 ACTIVE, 2 CLOSED, 2 CANCELLED)")
    print(f"  issues:        ~30  (OPEN / IN_PROGRESS / CLOSED)")
    print(f"  comments:      ~25")
    print(f"  notifications: ~19  (mix read/unread)")
    print(f"  conversations: {len(convs)}")
    print(f"  messages:      {len(msgs)}")
    print()
    print("Lưu ý: UID seed dùng prefix 'seed_user_*'.")
    print("Khi đăng nhập bằng Google thật, app sẽ tạo user document riêng")
    print("và hiển thị data seed như nội dung của 'người dùng khác'.")
    print("=" * 60)


if __name__ == "__main__":
    main()
