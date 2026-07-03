# RedShark 1.0.0 Release Notes

Release date: 2026-07-04

## Summary

RedShark 1.0.0 is the first stable handoff release for the native Android app. This release hardens the main Firebase-backed workflows, fixes registration and contribution graph regressions, refreshes shared UI tokens, and documents the final verification baseline.

## Fixed

- Registration no longer treats Firestore username-check failures as "username already taken".
- Username conflicts now carry the correct `username` field so the register form maps errors accurately.
- Contribution graph queries no longer depend on the `authorId + createdAt` composite query shape; the graph filters the 12-week window client-side after a simpler author query.
- Notification `markAsRead` now updates `isRead = true` instead of deleting the notification document.
- Profile bio can be cleared by saving a blank bio, and profile update failures no longer return fake local success.
- My Ideas realtime data no longer keeps stale cached ideas after a snapshot removes or soft-deletes an item.

## Improved

- Email signup writes `createdAt` for the Firestore user profile.
- Shared empty/error/loading states use a constrained content width for better mobile layout stability.
- Home drawer, top app bar, bottom navigation, and shared shape tokens were refreshed for a more consistent Material 3 UI.
- Unit coverage was expanded for username availability failures, username conflict field mapping, and clearing profile bio.

## Verification

Passed on the release branch:

```powershell
.\gradlew.bat compileDebugKotlin
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
```