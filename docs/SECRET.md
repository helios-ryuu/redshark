# SECRET.md — Biến cấu hình đang sử dụng

> File này chỉ giữ các biến cấu hình còn dùng trong dự án hiện tại (Google Sign-In + Cloudflare R2 + ký release).

## 1. Bảng biến cấu hình

| Tên biến                          | Môi trường | Nơi lưu trữ        | Hướng dẫn lấy / sinh                                                                               |
|-----------------------------------|------------|--------------------|----------------------------------------------------------------------------------------------------|
| `GOOGLE_WEB_CLIENT_ID`            | Dev + Prod | `local.properties` | Google Cloud Console → APIs & Services → Credentials → OAuth 2.0 (Web)                             |
| `GOOGLE_ANDROID_CLIENT_ID`        | Dev + Prod | `local.properties` | Google Cloud Console → OAuth 2.0 (Android, package = `com.helios.redshark`)                        |
| `CLOUDFLARE_R2_ACCOUNT_ID`        | Dev + Prod | `local.properties` | Cloudflare Dashboard → R2 → Overview → Account ID                                                  |
| `CLOUDFLARE_R2_ACCESS_KEY_ID`     | Dev + Prod | `local.properties` | Cloudflare Dashboard → R2 → Manage R2 API Tokens → Create token (scope: bucket cụ thể, WRITE+READ) |
| `CLOUDFLARE_R2_SECRET_ACCESS_KEY` | Dev + Prod | `local.properties` | Hiển thị duy nhất 1 lần khi tạo token — lưu ngay                                                   |
| `CLOUDFLARE_R2_BUCKET`            | Dev + Prod | `local.properties` | Cloudflare Dashboard → R2 → Buckets                                                                |
| `CLOUDFLARE_R2_ENDPOINT`          | Dev + Prod | `local.properties` | `https://<account-id>.r2.cloudflarestorage.com`                                                    |
| `CLOUDFLARE_R2_PUBLIC_BASE_URL`   | Dev + Prod | `local.properties` | URL public/custom domain của bucket                                                                |
| `RELEASE_KEYSTORE_PATH`           | Prod       | `local.properties` | Đường dẫn file `.keystore`                                                                         |
| `RELEASE_KEYSTORE_PASSWORD`       | Prod       | `local.properties` | Password keystore                                                                                  |
| `RELEASE_KEY_ALIAS`               | Prod       | `local.properties` | Alias key                                                                                          |
| `RELEASE_KEY_PASSWORD`            | Prod       | `local.properties` | Password alias                                                                                     |

## 2. File `local.properties` mẫu

```properties
# Android SDK
sdk.dir=C\:\\Users\\<you>\\AppData\\Local\\Android\\Sdk

# Google Sign-In
GOOGLE_WEB_CLIENT_ID=xxx.apps.googleusercontent.com
GOOGLE_ANDROID_CLIENT_ID=xxx.apps.googleusercontent.com

# Cloudflare R2
CLOUDFLARE_R2_ACCOUNT_ID=xxx
CLOUDFLARE_R2_ACCESS_KEY_ID=xxx
CLOUDFLARE_R2_SECRET_ACCESS_KEY=xxx
CLOUDFLARE_R2_BUCKET=redshark-media-dev
CLOUDFLARE_R2_ENDPOINT=https://xxx.r2.cloudflarestorage.com
CLOUDFLARE_R2_PUBLIC_BASE_URL=https://media.redshark.dev

# Release signing (chỉ prod)
RELEASE_KEYSTORE_PATH=../keystore/release.keystore
RELEASE_KEYSTORE_PASSWORD=xxx
RELEASE_KEY_ALIAS=redshark
RELEASE_KEY_PASSWORD=xxx
```

## 3. Checklist

- [ ] `local.properties`, `*.keystore`, `google-services.json` có trong `.gitignore`
- [ ] Không log token/credential trong runtime log
- [ ] Không hardcode biến cấu hình trong Kotlin source
