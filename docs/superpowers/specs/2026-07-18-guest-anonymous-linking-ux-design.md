# Guest (Anonymous) Auth + Google Linking + Profile UX — Design

**Date:** 2026-07-18
**Status:** Approved (design). Ready for implementation plan.
**Related:** `docs/superpowers/specs/2026-04-19-onboarding-login-flow-design.md`; RELEASES.md (Google Sign-In fix, code 4).

## 1. Goal

Let people use FishingNotes immediately, without being forced to sign in. After onboarding the app silently creates a Firebase **anonymous** account and drops the user straight into the app. Signing in with Google becomes an **optional upgrade** that backs up and syncs their log, and can be done at any time later. Data created as a guest carries over when they upgrade.

## 2. Locked product decisions

1. **Entry = silent anonymous.** After onboarding, auto-call `signInAnonymously()` and land on Home/Map. There is no login gate.
2. **Merge on collision.** Linking a brand-new Google account keeps the same Firebase `uid`, so catches/markers carry over automatically. Linking a Google account that was **already used** with the app (has its own cloud data) triggers a **full merge**: copy the anonymous account's catches/markers into the existing account, de-duplicated, then switch to it.

## 3. Architecture context (verified)

- **Gate today:** `MainViewModel.routing` computes a `RoutingDecision` (`Splash`/`Onboarding`/`Login`/`Home`) from Firebase auth state; `FishingNotesApp` renders it via `AnimatedContent` **before** the `NavHost`. The whole nav graph lives only under `RoutingDecision.Home`. `RoutingDecision.Login` shows `LoginScreen()` (the hard gate to remove).
- **Auth lib:** gitlive `dev.gitlive:firebase-auth:2.4.0`. Exposes `FirebaseAuth.signInAnonymously()`, `FirebaseUser.isAnonymous`, `FirebaseUser.linkWithCredential(credential)`, `AuthResult`. No anonymous/link usage exists yet.
- **User model:** `User(uid, email, displayName, photoUrl, login, registerDate, birthDate)`. `toUser()` maps null displayName → `"Anonymous"`, null email → `""`. **No `isAnonymous` field yet.**
- **Data is uid-scoped:** Firestore `users/{uid}/markers/{id}/catches/{id}`; local Room rows stamped `userId` from `authRepository.getCurrentUserId()`. Both `UserCatch` and `UserMapMarker` have a stable `id: String` (dedupe key). Sync (`SyncWorker`) keys only on uid — works for anonymous. Firestore rules already allow anonymous (`request.auth != null`).
- **Landmine:** `FirebaseAuthRepository.getCurrentUserId()` returns the literal string `"Anonymous"` when `currentUser == null`, and every local write uses it. Anonymous sign-in **must complete before any write path / Home is reachable**, or data is stamped `userId="Anonymous"`.
- **Photos:** stored at a **flat** `markerImages/{photoId}` Storage path (not per-uid); `UserCatch.downloadPhotoLinks` are portable download URLs. Merge copies the Firestore docs only — **no photo re-upload needed**. (Verify in Firebase console that Storage read rules on `markerImages/**` are not per-uid; the flat path strongly implies they aren't.)

## 4. Guest experience & how guest state is shown

Guest status is surfaced in exactly **two calm, permanent places** — nothing else. No global banner, no tab badge, no home-screen prompt, no after-N-catches interstitial, no warning-tone banner.

1. **Profile — `UserNameSection` guest variant:**
   - Headline: `displayName` if set, else `Res.string.anonymous`.
   - Beside the name: `IconStatChip(icon = Icons.Default.CloudOff, label = guest_chip_label)` — permanent, low-key marker (already in `Chips.kt`).
   - Second line: `AppText(guest_status_subtitle, Body, onSurfaceVariant)` = "Your catches are saved on this device." Factual, not alarmist. The guest `registerDate` (a meaningless "today") is **not** shown.
2. **Settings → Account group:** a single upgrade row (§7).

**Linked variant (legible by contrast):** name + `register_date_value` + `Row(Icon(CloudDone) + AppText(profile_backed_up))` = "Backed up & syncing".

## 5. Entry / routing change

- Remove `RoutingDecision.Login`. In `MainViewModel.routing`, the current `else -> Login` branch (onboarding done, `user == null`) instead calls `repository.signInAnonymously()` and stays on `Splash` until `authStateChanged` emits the anonymous user → resolves to `Home`.
- `FishingNotesApp` drops the `RoutingDecision.Login -> LoginScreen()` arm; only `Splash`, `Onboarding`, `Home` remain.
- The repurposed `LoginScreen()` becomes a pushed, dismissible destination inside the Home nav graph: `MainDestinations.LinkAccount` — never a gate.
- Onboarding already exposes `OnboardingScreen(onFinished)`; `onFinished` is the trigger point (routing change handles the actual anonymous sign-in).

## 6. Link entry points

All route to `MainDestinations.LinkAccount`. Pressure escalates only after the user opts in: Text (onboarding) → Tonal (Profile) → Filled (Link screen's own Google button).

1. **Profile (primary):** `AppButton(style = Tonal, leadingIcon = ic_google_logo, text = guest_link_cta)` between `UserNameSection` and `StatRow`. Guest-only.
2. **Settings (always available):** `SettingsNavLink(title = settings_sign_in_title, subtitle = settings_sign_in_subtitle, icon = Icons.Default.CloudUpload)` → `LinkAccount`. Guest-only.
3. **Onboarding tail:** the final slide keeps its primary finish button (`onboarding_get_started`) and adds a subtle `AppButton(style = Text, text = sign_with_google)` beneath it. Never blocks.
4. **EditProfile (contextual):** an Info banner in place of the empty email field (§9), not a standalone surface.

## 7. Linking flow

New `LinkAccountViewModel` (or extend `LoginViewModel`) exposing:
`LinkState = Idle | Linking | MergeConfirm(googleEmail) | Merging(progress: Float?) | MergeSuccess(added: Int, alreadyPresent: Int) | Success | Error(msgRes) | Cancelled`.

**Happy path (new Google account):**
1. Profile/Settings CTA → `navigate(LinkAccount)`.
2. Tap Filled Google button in `GoogleButtonUiContainer`.
3. Non-null `GoogleUser` → `vm.linkWithGoogle(idToken)` → `currentUser.linkWithCredential(GoogleAuthProvider.credential(idToken, null))`.
4. `Linking` → `ModalLoadingDialog(text = linking_in_progress)`.
5. Success: uid preserved, zero migration; `toUser()` now returns `isAnonymous = false` with real email/name/photo. Pop back to Profile (linked variant) + `SnackbarManager.showMessage(link_success)`.

**Returning-account merge branch (collision):**
- `linkWithCredential` throws the gitlive collision exception (confirm exact type during impl; expected `FirebaseAuthUserCollisionException` / credential-already-in-use) → `MergeConfirm`.
- `DefaultDialog(merge_confirm_title, merge_confirm_message, positive = merge_confirm_positive, negative = cancel)`. Message reassures: "…combine everything into one account — nothing gets deleted."
- On Combine: snapshot anonymous catches+markers, `signInWithCredential` into the existing account, copy the snapshot in **de-duplicated by `id`** (idempotent, resumable). `Merging(progress)` → `ModalLoadingDialog(text = merge_in_progress (+ merge_progress_detail), progress = copied/total)` (indeterminate if count unavailable).
- Complete → `MergeSuccess(added, alreadyPresent)` → `DefaultDialog(merge_done_title "Welcome back!", merge_done_message | merge_done_message_deduped, positive = merge_view_log)`. Use deduped variant when `alreadyPresent > 0`; countless fallback if counts unavailable. Clean up the orphaned anonymous uid's server data after a successful copy.
- Cancel on confirm → back to Link screen unchanged (still a guest, nothing touched).

**Error / cancel branch (the KMPAuth null problem):**
- Immediate `null` `GoogleUser` = cancel → `onGoogleSignInCancelled()` → `Idle`, **no** error banner. A deliberate back-out is not scolded.
- Only a **thrown** exception (`linkWithGoogle` / `signInWithCredential` / merge copy) → `Error` → existing in-place `InlineBannerCard(tone = Error, icon = Warning, title = sign_in_generic_error, actionLabel = retry)` on the Link screen. Non-blocking; Retry or leave via "Maybe later".
- Failure **during** merge (after switch) → title `merge_error` ("…Your catches are safe — please try again."). Idempotent copy makes Retry safe.

## 8. Screen-by-screen (ASCII + real components)

### Profile — guest
```
┌────────────────────────────────┐
│ Profile                    ⚙   │  ProfileAppBar(isAnonymous=true): logout HIDDEN
├────────────────────────────────┤
│            ( 🎣 )   ✎          │  AvatarWithBadge (ic_fisher fallback)
│         Angler  [☁̸ Guest]      │  headline + IconStatChip(CloudOff, guest_chip_label)
│   Your catches are saved on     │  AppText(guest_status_subtitle)
│         this device             │
│  ┌──────────────────────────┐  │
│  │ G  Sign in to back up    │  │  AppButton(Tonal, ic_google_logo, guest_link_cta) → LinkAccount
│  └──────────────────────────┘  │
│  ┌───────────┐ ┌───────────┐   │  StatRow { StatTile }
│  │ 🎣  12    │ │ 📍  4     │   │
│  └───────────┘ └───────────┘   │
│  ★ Favorite place    …          │
│  🏆 Best catch       …          │
└────────────────────────────────┘
```

### Profile — linked
```
┌────────────────────────────────┐
│ Profile              ⏻     ⚙   │  ProfileAppBar(isAnonymous=false): logout RETURNS
├────────────────────────────────┤
│            (photo)   ✎          │
│           Kostya M.             │  displayName
│      Registered Jul 2026        │  AppText(register_date_value)
│      ☁ Backed up & syncing      │  Row(Icon(CloudDone) + AppText(profile_backed_up))
│  ┌───────────┐ ┌───────────┐   │  (no CTA)
│  │ 🎣  12    │ │ 📍  4     │   │
│  └───────────┘ └───────────┘   │
└────────────────────────────────┘
```

### Link / upgrade screen (repurposed LoginScreen at MainDestinations.LinkAccount)
```
┌────────────────────────────────┐
│ ←                              │  AppTopBar(navigationIcon = ArrowBack, onNavigationClick = onBack)
│          ┌────────┐            │  Image(ic_launcher) + slideUpFadeIn
│          │  app   │            │
│          └────────┘            │
│     Back up your catches       │  Text(link_account_title, headlineSmall)
│  Sign in with Google to sync   │  Text(link_account_subtitle, bodyLarge)
│  across devices, never lose    │
│  a catch.                      │
│  ☁  Never lose a catch or spot │  3× Row(Icon 24dp + AppText Body)
│  📱  Sync across your devices  │
│  👤  Get a real profile        │
│  ┌──────────────────────────┐  │
│  │ G  Sign In with Google   │  │  GoogleButtonUiContainer { AppButton(Filled, ic_google_logo) }
│  └──────────────────────────┘  │
│  We only use your email to     │  Text(login_trust_copy)  [reuse — add RU]
│  sync your catches.            │
│          Maybe later           │  AppButton(Text, link_maybe_later) → onBack
└────────────────────────────────┘
```
Background: `BrandGradients.surfaceVertical(FishingTheme.colorScheme)`. Error state: existing `AnimatedVisibility` + `InlineBannerCard(tone = Error)` block below the button, shown only on thrown errors.

### Linking in progress (happy path)
`ModalLoadingDialog(visible, text = linking_in_progress)` — non-dismissable.

### Merge confirm (collision only)
```
┌──────────────────────────────┐
│ This account already has data│
│ This Google account has its  │
│ own catches and places.      │
│ We'll combine everything     │
│ into one account — nothing   │
│ gets deleted.                │
│        Cancel   [Combine]    │
└──────────────────────────────┘
```
`DefaultDialog(merge_confirm_title, merge_confirm_message, positive = merge_confirm_positive, negative = cancel)`.

### Merge in progress
```
        ( ◐ spinner )
    Combining your catches…
    ▓▓▓▓▓▓▓▓▓▓░░░░░░  60%
    Keep the app open.
```
`ModalLoadingDialog(text = merge_in_progress (+ merge_progress_detail), progress = 0f..1f)`.

### Merge result
```
┌──────────────────────────────┐
│ Welcome back!                │
│ 12 catches and 4 places are  │
│ now in your account.         │  or deduped: "12 added, 3 were already in your account."
│                [ View my log]│
└──────────────────────────────┘
```
`DefaultDialog(merge_done_title, merge_done_message | merge_done_message_deduped, positive = merge_view_log, onNegativeClick = null)`.

### Link error (thrown error only; cancel shows nothing)
```
┌────────────────────────────────┐
│ ←                              │
│     Back up your catches       │
│  ┌──────────────────────────┐  │
│  │ G  Sign In with Google   │  │
│  └──────────────────────────┘  │
│  ┌──────────────────────────┐  │
│  │ ⚠ Couldn't sign you in.  │  │  InlineBannerCard(Error, Warning, sign_in_generic_error)
│  │   Check your connection… │  │  actionLabel = retry (merge failure → title merge_error)
│  │                [Retry]   │  │
│  └──────────────────────────┘  │
│          Maybe later           │
└────────────────────────────────┘
```

### EditProfile — guest
```
┌────────────────────────────────┐
│ ← Edit profile                 │  AppTopBar
│           ( 🎣 )  ✎            │  AvatarWithBadge
│  👤 Name                        │  FormTextField (editable, persists locally)
│  @  Username                    │  FormTextField
│  📅 Date of birth               │  PickerField
│      (no e-mail field)          │  Email FormTextField only when !isAnonymous
│  ┌──────────────────────────┐  │
│  │ ℹ Sign in with Google to │  │  InlineBannerCard(Info, editprofile_guest_note,
│  │   back up your profile   │  │  actionLabel = link_action) → LinkAccount
│  │              [Sign in]   │  │
│  └──────────────────────────┘  │
└────────────────────────────────┘
```

### Settings — Account (guest)
```
│ Account                        │  SettingsGroup(settings_account)
│ ┌────────────────────────────┐ │
│ │ ☁ Sign in with Google    › │ │  SettingsNavLink(settings_sign_in_title/_subtitle,
│ │   Back up and sync your    │ │  icon = CloudUpload) → LinkAccount
│ │   catches                  │ │
│ ├────────────────────────────┤ │  SettingsDivider
│ │ 🗑 Clear all data         › │ │  SettingsNavLink(guest_clear_data, icon = DeleteForever)
│ └────────────────────────────┘ │
```
Linked users: today's flow verbatim (`SettingsNavLink(delete_account, delete_account_subtitle, DeleteForever)` → confirm → reauth via `GoogleButtonUiContainer` → `ModalLoadingDialog(delete_account_deleting)`).

## 9. Profile & EditProfile changes

**Profile.kt**
- `ProfileAppBar(navController, isAnonymous)`: render the logout `IconButton` only when `!isAnonymous` (signing out anonymous strands device-only data with no way back). Settings action always present.
- `UserNameSection(user, isAnonymous)`: guest → name + `IconStatChip(CloudOff, guest_chip_label)` + `AppText(guest_status_subtitle)`; linked → name + `AppText(register_date_value)` + `Row(Icon(CloudDone) + AppText(profile_backed_up))`.
- Insert the Tonal CTA between `UserNameSection` and `StatRow`, guest-only.

**EditProfile.kt**
- Render the Email `FormTextField` only when `!isAnonymous` (today it's always an empty read-only field for guests — confusing).
- For guests, add `InlineBannerCard(tone = Info, icon = CloudUpload, title = editprofile_guest_note, actionLabel = link_action)` at the bottom of the scroll column.
- Name / Username / Birthday stay editable and persist to the uid-keyed doc for both states.

## 10. Account deletion / sign-out

Keyed off `isAnonymous`:
- **Sign-out:** logout `IconButton` + `LogoutDialog` shown only when `!isAnonymous`. Linked behavior unchanged.
- **Deletion:**
  - Linked: today's flow verbatim.
  - Guest: Account group replaces "Delete account" with `SettingsNavLink(guest_clear_data, DeleteForever)`. Confirm via `DefaultDialog(guest_clear_data_title, guest_clear_data_message, positive = delete, negative = cancel)`. Reauth is **skipped** (no credential). On confirm: wipe local data + delete the anonymous Firebase user, then silently `signInAnonymously()` again and return to Map.

## 11. Copy table

**Reused keys** (already EN+RU unless noted): `anonymous`, `sign_with_google`, `signing_in`, `sign_in_generic_error`, `retry`, `cancel`, `delete`, `login_trust_copy` (**add RU — currently EN only**), `register_date_value`, `settings_account`, `onboarding_get_started`, `logout*`, `delete_account*`, `delete_account_reauth*`, `delete_account_deleting`.

**New keys (EN / RU):**

| key | EN | RU |
|---|---|---|
| `guest_chip_label` | Guest | Гость |
| `guest_status_subtitle` | Your catches are saved on this device | Ваши уловы сохранены на этом устройстве |
| `profile_backed_up` | Backed up & syncing | Сохранено и синхронизируется |
| `guest_link_cta` | Sign in to back up | Войти и сохранить |
| `link_action` | Sign in | Войти |
| `settings_sign_in_title` | Sign in with Google | Войти через Google |
| `settings_sign_in_subtitle` | Back up and sync your catches | Сохраняйте и синхронизируйте уловы |
| `link_account_title` | Back up your catches | Сохраните свои уловы |
| `link_account_subtitle` | Sign in with Google to sync across devices and never lose a catch. | Войдите через Google, чтобы синхронизировать данные и не потерять ни одного улова. |
| `link_benefit_backup` | Never lose a catch or spot | Не потеряете ни улова, ни точки |
| `link_benefit_sync` | Sync across your devices | Синхронизация на всех устройствах |
| `link_benefit_profile` | Get a real profile | Настоящий профиль |
| `link_maybe_later` | Maybe later | Позже |
| `linking_in_progress` | Linking your account… | Связываем аккаунт… |
| `link_success` | Signed in. Your catches are backed up. | Готово. Ваши уловы сохранены. |
| `merge_confirm_title` | This account already has data | В этом аккаунте уже есть данные |
| `merge_confirm_message` | This Google account has its own catches and places. We'll combine everything into one account — nothing gets deleted. | В этом аккаунте Google уже есть уловы и места. Мы объединим всё в один аккаунт — ничего не удалится. |
| `merge_confirm_positive` | Combine | Объединить |
| `merge_in_progress` | Combining your catches… | Объединяем уловы… |
| `merge_progress_detail` | Keep the app open. | Не закрывайте приложение. |
| `merge_done_title` | Welcome back! | С возвращением! |
| `merge_done_message` | %1$d catches and %2$d places are now in your account. | %1$d уловов и %2$d мест теперь в вашем аккаунте. |
| `merge_done_message_deduped` | %1$d added, %2$d were already in your account. | %1$d добавлено, %2$d уже были в аккаунте. |
| `merge_view_log` | View my log | Открыть дневник |
| `merge_error` | Couldn't finish combining. Your catches are safe — please try again. | Не удалось завершить объединение. Ваши уловы в безопасности — попробуйте ещё раз. |
| `editprofile_guest_note` | Sign in with Google to back up your profile | Войдите через Google, чтобы сохранить профиль |
| `guest_clear_data` | Clear all data | Очистить данные |
| `guest_clear_data_title` | Clear all data? | Очистить все данные? |
| `guest_clear_data_message` | You're using the app as a guest, so there's no backup. This permanently erases every catch and spot on this device. | Вы используете приложение как гость, резервной копии нет. Это навсегда удалит все уловы и точки на этом устройстве. |

## 12. Component reuse

**No new composables.** Every state maps to existing components:
- Entry points: `AppButton(Tonal)`, `SettingsNavLink`.
- Guest marker: `IconStatChip(CloudOff, …)`. Linked marker: `Icon(CloudDone)` + `AppText`.
- Link screen: existing `LoginScreen` body + `AppTopBar(ArrowBack)` + 3× inline `Row(Icon + AppText)` benefit rows + `AppButton(Text)` ("Maybe later").
- Progress: `ModalLoadingDialog(text, progress)` (progress param already supported).
- Merge confirm/result: `DefaultDialog`. Errors: `InlineBannerCard(Error)`. Guest EditProfile nudge: `InlineBannerCard(Info)`.
- `ConflictResolutionDialog` is **not** reused — it's a per-item field picker, wrong altitude for a bulk merge summary.

**Modifications only (signature gains a boolean):** `ProfileAppBar(isAnonymous)`, `UserNameSection(user, isAnonymous)`, `EditProfile` conditional email/banner, `AccountSettingsGroup` branch.

## 13. Plumbing (non-UI)

- **Domain:** add `User.isAnonymous: Boolean` (mapped in `toUser()`).
- **Repository (`UserRepository` + `FirebaseUserRepositoryImpl`):**
  - `isAnonymous: Flow<Boolean>` ← `Firebase.auth.authStateChanged.map { it?.isAnonymous ?: true }`.
  - `signInAnonymously(): Result<Unit>`.
  - `linkWithGoogle(idToken): Result<LinkOutcome>` where `LinkOutcome = Linked | Merged(added, alreadyPresent)`; catch the collision exception → merge (snapshot → `signInWithCredential` → idempotent dedupe-by-id copy → delete orphan anon data → counts).
  - `clearGuestData(): Result<Unit>` (wipe local + delete anon user + re-`signInAnonymously()`).
  - Fix `FirebaseAuthRepository.getCurrentUserId()` to never return literal `"Anonymous"` once anonymous auth is guaranteed before Home.
- **ViewModels:** `LinkAccountViewModel` with `LinkState`; surface `isAnonymous` into `UserViewModel` and `EditProfileViewModel` by collecting `repository.isAnonymous`.
- **Routing/nav:** remove `RoutingDecision.Login`; `MainViewModel` triggers `signInAnonymously()` on `user == null`; `FishingNotesApp` drops the Login arm; add `MainDestinations.LinkAccount` + its composable in `AppNavGraph`.

## 14. Edge cases & risks

- **[Highest] `isAnonymous` must be reactive from the repository, not just a model field.** Profile/EditProfile/Settings read the *datastore* user, which won't reliably carry `isAnonymous`. Back it with `Firebase.auth.authStateChanged`, collect in the viewmodels. Skipping this makes the guest/linked fork read stale state after linking.
- **Merge reliability is load-bearing on the copy** — must be dedupe-keyed by `id`, idempotent, resumable. Both entities have `id`. If reliability can't be guaranteed, downgrade the merge result copy to the countless fallback.
- **Photos:** no re-upload needed (flat `markerImages/` path, portable URLs). Verify Storage read rules on `markerImages/**` aren't per-uid.
- **Null GoogleUser = cancel or error, indistinguishable.** Treat immediate null as cancel (no banner); banner only on thrown exceptions. A genuine silent failure returning null (not throwing) will look like a cancel; the user can retry.
- **Orphan anonymous user cleanup** after a successful merge.
- **Collision exception type:** confirm the exact gitlive 2.4.0 name during impl (`FirebaseAuthUserCollisionException` vs credential-already-in-use variant) so the merge branch catches it precisely.
- **RU gap** for `login_trust_copy` (EN only) — add RU.
- **Discoverability trade-off (accepted):** calm chip + Tonal button + Settings row means some users may never back up before losing a device. Explicit price of the "frictionless, no-nag" mandate, mitigated by the honest subtitle and permanent chip.

## 15. Build order

1. Domain/repo: `User.isAnonymous`; `UserRepository.isAnonymous: Flow<Boolean>`; `signInAnonymously()`; `linkWithGoogle(idToken)` with collision→merge; `clearGuestData()`; fix `getCurrentUserId()`.
2. Routing: remove `RoutingDecision.Login`; `MainViewModel` user==null → `signInAnonymously()` + stay `Splash`; drop Login arm in `FishingNotesApp`.
3. Navigation: add `MainDestinations.LinkAccount`; add composable in `AppNavGraph`; repurpose `LoginScreen()` → `LinkAccountScreen(onBack, onLinked)` with `AppTopBar(ArrowBack)` + benefit rows + "Maybe later".
4. ViewModel: `LinkAccountViewModel` with `LinkState`; wire `linkWithGoogle`, `confirmMerge`, retry, reuse `onGoogleSignInCancelled` for null.
5. Surface `isAnonymous` into `UserViewModel` and `EditProfileViewModel`.
6. UI — Profile: `ProfileAppBar(isAnonymous)` hides logout for guests; `UserNameSection` variants; insert Tonal CTA guest-only.
7. UI — EditProfile: email field only when `!isAnonymous`; guest Info banner.
8. UI — Settings `AccountSettingsGroup`: guest (sign-in row + Clear all data) vs linked (existing delete flow).
9. Strings: all new EN+RU keys; add missing RU for `login_trust_copy`.
10. Onboarding: add subtle Text sign-in button on the final slide.

## 16. Verification checklist

- Happy-path link keeps uid; catches/markers/photos intact.
- Collision merge: dedupe by id, idempotent (re-run adds nothing), correct added/alreadyPresent counts.
- Cancel (null result) shows no banner; thrown error shows banner + Retry.
- Guest "Clear all data" wipes and re-mints a fresh anonymous user, lands on Map.
- No write path is reachable before anonymous sign-in completes (no `userId="Anonymous"` rows).
- Profile / Link / EditProfile / Settings correct in light and dark.
