# Releases

## Release history

| Version | Code | Date       | Track                  | Status                |
|---------|------|------------|------------------------|-----------------------|
| 0.2.0   | 5    | 2026-07-18 | Internal testing       | Built (guest auth + Google linking) |
| 0.1.0   | 4    | 2026-07-18 | Internal testing       | Superseded by 0.2.0   |
| 0.1.0   | 3    | 2026-07-17 | Internal testing       | Superseded by code 4  |
| 1.0.0   | 2    | —          | — (internal build only) | Never shipped         |
| 1.0.0   | 1    | —          | — (internal build only) | Never shipped         |

Artifact: `androidApp/build/outputs/bundle/release/androidApp-release.aab` (signed, R8-minified).

### 0.2.0 code 5 — Guest (anonymous) auth + Google linking

- **Guest entry:** after onboarding the app signs in anonymously and goes straight to the map — no forced sign-in gate. Offline first-launch shows a retry screen (30s timeout).
- **Optional upgrade:** "Sign in with Google" from Profile / Settings links the account in place (same uid → catches carry over for free). Linking a Google account that already has data runs a dedupe-by-id merge; a merge interrupted mid-copy resumes from local data on retry.
- **Guest UI:** guest chip + "saved on this device" on Profile, backed-up state when linked, guest EditProfile hides the empty email, Settings offers "Clear all data" for guests and the existing delete flow for linked users. Logout dialog now explains the guest fallback.
- **Correctness fixes:** uid-reactive Firestore snapshot flows (no stale data after account switch), localized "Anonymous" label, per-provider analytics `method`.
- Includes the code 4 Google Sign-In fix. Merged from branch `anonymous-login` (base `develop`).
- **Before public rollout:** manual device smoke of the guest→link-a-used-account→merge path (no automated seam), and tighten Firebase **Storage** rules (currently `allow read, write: if request.auth != null` — anonymous auth widens this to every install; Firestore rules are already per-uid).

### code 4 — Google Sign-In fix

- **Root cause:** Google Sign-In failed in prod because the only SHA-1 registered in the Firebase project for `com.merkost.fishingnotes` matched no certificate that actually signs the delivered app. Play App Signing re-signs the AAB with Google's app-signing key (`a7f7ca76…`), which was not registered, so Google returned `DEVELOPER_ERROR` and KMPAuth swallowed it to a silent `null`. Dev worked because the debug cert was registered.
- **Fix (config, no build needed for existing installs):** registered both the Play app-signing key (`a7f7ca76…`) and the upload key (`c8ff5cdd…`) in Firebase; refreshed `androidApp/google-services.json`.
- **App change shipped in code 4:** sign-in failures are now surfaced to the user instead of silently resetting — `LoginViewModel.onGoogleSignInFailed()` shows an error banner + logs analytics, and the account-deletion reauth path shows a snackbar (`UserViewModel.onReauthSignInFailed()`).

## Track plan for 0.1.0

1. **Internal testing** — upload the AAB, verify install, sign-in, map, catch logging, offline sync, account deletion, and the EEA consent form (debug builds force EEA geography).
2. **Closed testing (optional)** — small tester group for 3–7 days; watch Crashlytics and pre-launch report.
3. **Production, staged rollout** — 10% → 50% → 100%, pausing on any crash-rate regression.

Pre-upload checklist:
- [ ] Privacy policy URL in store listing: https://merkost.github.io/FishingNotes/privacy.html
- [ ] Data safety → account deletion URL: https://merkost.github.io/FishingNotes/privacy.html#delete
- [ ] Data safety form declares: location, photos, email/name (Firebase), advertising ID (AdMob)
- [ ] AdMob: GDPR consent message published in AdMob console (Privacy & messaging) — required for EEA ads
- [ ] Version bumped in `gradle/libs.versions.toml` (`appVersionCode`, `appVersionName`)

## Cutting a release

1. Bump `appVersionCode` (must increase) and `appVersionName` in `gradle/libs.versions.toml`.
2. `./gradlew :androidApp:bundleRelease` — signing resolves from `secrets.properties`/`local.properties`.
3. Verify: `jarsigner -verify androidApp/build/outputs/bundle/release/androidApp-release.aab` → `jar verified.`
4. Commit the bump, push `master`, add the release entry and notes here.

## Release notes — 0.1.0

Paste the block below into Play Console → Release → Release notes (locale tags included; each under 500 chars).

```
<en-US>
Welcome to Fishing Notes — your personal fishing log!
• Save your fishing spots on the map
• Log catches with photos, weight, bait and notes
• Weather and bite (solunar) forecast for every spot
• Works offline — your log syncs when you're back online
• Sign in with Google to keep your data on all your devices
</en-US>
<ru-RU>
Встречайте Fishing Notes — ваш личный дневник рыбалки!
• Сохраняйте рыбные места на карте
• Записывайте уловы: фото, вес, приманка и заметки
• Прогноз погоды и клёва для каждого места
• Работает офлайн — данные синхронизируются при появлении сети
• Войдите через Google, чтобы дневник был доступен на всех устройствах
</ru-RU>
```
