# RedShark 1.0.1 Release Notes

Release date: 2026-07-04

## Summary

RedShark 1.0.1 is a maintenance release for source readability and handoff quality. It keeps the 1.0.0 behavior baseline, adds explanatory comments across Kotlin and Android XML files, applies a low-risk simplification to the idea reaction upvote helper, and updates release documentation for the new tag.

## Improved

- Added file-level and declaration-level comments across non-generated Kotlin source files so domain, data, core, UI, and tests are easier to review.
- Added Android XML comments for manifest, resource, launcher icon, backup, and data extraction configuration files.
- Simplified the upvote count helper in `IdeaRepositoryImpl` by replacing mutable counter steps with a single `when`-based delta expression.
- Bumped Android app version to `versionName = "1.0.1"` and `versionCode = 3`.
- Updated README and handoff documents to mark the local release branch/tag as `hotfix/v1.0.1` and `v1.0.1`.

## Verification

Passed on the hotfix branch:

```powershell
.\gradlew.bat compileDebugKotlin
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
```