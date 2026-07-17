# Releases

## Release history

| Version | Code | Date       | Track                  | Status                |
|---------|------|------------|------------------------|-----------------------|
| 0.1.0   | 3    | 2026-07-17 | Internal testing       | Built, ready to upload |
| 1.0.0   | 2    | —          | — (internal build only) | Never shipped         |
| 1.0.0   | 1    | —          | — (internal build only) | Never shipped         |

Artifact: `androidApp/build/outputs/bundle/release/androidApp-release.aab` (signed, R8-minified).

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
