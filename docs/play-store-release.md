# Google Play Store — Release Checklist

Everything you need to fill in across Play Console for **Fishing Notes** (`com.merkost.fishingnotes`). Copy-paste-ready text in code blocks; pick options where there are choices.

> Current build: **versionName 1.0.0 / versionCode 1** (in `gradle/libs.versions.toml`). This is a fresh first release under the `merkost` developer account (previous `mobileprism` account was removed). Bump both for any future release upload.

---

## 1. App identity

| Field | Value |
|---|---|
| **App name** (max 30 chars) | `Fishing Notes` |
| **Default language** | English (United States) — `en-US` |
| **App or game** | App |
| **Free or paid** | Free |
| **Application ID** | `com.merkost.fishingnotes` (don't change after release — locked forever) |
| **Package name (debug)** | `com.merkost.fishingnotes.debug` (not published) |
| **Signing key** | `MerkostDev.jks`, alias `fishing` (use upload key signing; let Google manage app signing) |

---

## 2. Store listing — text

### Short description (max 80 chars)
```
Mark your spots, log every catch, and check the weather before you go.
```

### Full description (max 4000 chars)
```
Fishing Notes is the personal log every angler needs. Mark your favorite spots, capture every catch with the conditions that hooked it, and check live weather before you head out — all in one app.

Why anglers pick it
• Save the spots that matter. Drop a pin on any lake, river, or stretch of coast and never lose a productive location again.
• Log every catch. Species, weight, lure, weather at the moment — captured in seconds.
• Browse your full history. Every catch, every place, sorted and searchable.
• Know the conditions. Live weather and daily forecasts for every saved spot.
• Sync everywhere. Sign in once and your log is on every device you own.

Built for the way you actually fish
• Add a spot in two taps from the map.
• Add a catch in seconds with smart defaults.
• See fish-activity, wind, and temperature at a glance from the map.
• Filter your catches by species, place, or date.
• Works offline — your log syncs automatically when you're back in range.

What's included
• Map with custom-colored markers for every spot you save.
• Catch detail view with photo, weight, length, lure, weather, and notes.
• Multi-day weather forecast at every spot.
• Personal stats — your best species, your best places.
• Free, no subscriptions, no in-app purchases.

Privacy
• Your account is used only to sync your catches across your devices.
• We never post on your behalf and we never sell your data.
• Read the full policy at https://merkost.github.io/FishingNotes/privacy.html.

Got feedback or a feature request? Email us at merkostdev+fishing@gmail.com. Tight lines.
```

### What's new (release notes — max 500 chars per language)

First public release:

```
1.0.0
• Save your favorite fishing spots on the map.
• Log every catch with species, weight, lure, photo, and the weather you caught it in.
• Browse and filter your full catch history.
• Live weather and multi-day forecasts for every spot.
• Sign in once and sync your log across your devices.
```

---

## 3. Categorization

| Field | Value |
|---|---|
| **App category** | Sports |
| **Tags** (Play picks 5 from a fixed list) | `Outdoor recreation`, `Hobbies & interests`, `Tracking & journaling`, `Maps & navigation`, `Weather` |
| **Content rating questionnaire** | Complete IARC questionnaire — no violent, sexual, or gambling content; expected rating **Everyone (3+)** |
| **Target audience** | 18+ (recreational adult activity; remove this restriction only if you complete the Designed-for-Families flow) |

---

## 4. Graphics & screenshots

**All final assets are exported to `docs/store-assets/`** — ready to upload as-is.

| Slot | Dimensions | File in `docs/store-assets/` | Notes |
|---|---|---|---|
| **App icon** | 512 × 512 px | `icon-512.png` | Clean production icon (BETA banner removed). Also written back to `shared/src/main/ic_launcher-playstore.png` and the mipmap launcher set. |
| **Feature graphic** | 1024 × 500 px | `feature-1024x500.png` | Brand-blue (palette E) banner: logo lockup + headline + Map/Catches/Weather chips + phone mockup. |
| **Phone screenshots (5)** | 1028 × 2138 px portrait | `01-hero.png` … `05-weather.png` | `01-hero` is the branded title slide (palette E); `02`–`05` are app screens from the `MarketingScreenshotExporter` instrumented test. |
| **Tablet screenshots (optional)** | 7" / 10" | — | optional |

> **How screenshots are generated:** the `MarketingScreenshotExporter` instrumented test (`shared/src/androidInstrumentedTest/kotlin/.../marketing/`) renders each marketing composable at density 2.5 and writes a PNG to the device's external files dir. Regenerate with:
> ```
> ./gradlew :shared:assembleDebugAndroidTest
> adb install -r -t shared/build/outputs/apk/androidTest/debug/shared-debug-androidTest.apk
> adb shell am instrument -w -e class com.mobileprism.fishing.marketing.MarketingScreenshotExporter \
>   com.mobileprism.fishing.test/androidx.test.runner.AndroidJUnitRunner
> adb pull /sdcard/Android/data/com.mobileprism.fishing.test/files/marketing/. docs/store-assets/
> ```
> Then rebuild the feature graphic from `01-map.png` (see the ImageMagick recipe in git history / `feature-1024x500.png`).

### Screenshot order (first 2–3 are visible in the listing preview row)

1. **`01-hero.png`** — branded title slide (logo + "Every spot. Every catch. Every forecast." + phone)
2. **`02-map.png`** — map with marker pins + live place card ("Mark every spot.")
3. **`03-catch.png`** — catch detail page ("Log every catch.")
4. **`04-notes.png`** — populated notes list ("Your log, always with you.")
5. **`05-weather.png`** — weather screen ("Know the conditions.")

---

## 5. Privacy policy

**Required** because the app collects user data (email, photos, location).

- **Privacy policy URL:** `https://merkost.github.io/FishingNotes/privacy.html`
- **Terms of service URL:** `https://merkost.github.io/FishingNotes/terms.html`
- Source lives in `docs/` (`index.html`, `privacy.html`, `terms.html`). Enable GitHub Pages: repo **Settings → Pages → Source = Deploy from a branch**, branch = the branch holding `docs/` (e.g. `master` after merge), folder = **`/docs`**. The pages go live at the URLs above within a minute.
- Paste the privacy URL in: Play Console → Policy → App content → **Privacy policy**.
- The hosted policy covers:
  - What data is collected: account email + display name (Firebase Auth), GPS location (only when in app, only when user pins / opens weather), catch photos (uploaded to Firebase Storage), catch metadata (Firestore), advertising ID (AdMob).
  - Why: account sync, weather lookups, catch logging, ads.
  - Sharing: Google (Firebase, Maps, AdMob); OpenWeatherMap; RapidAPI.
  - Retention: as long as account exists; user can delete via in-app logout + Firebase Console deletion request OR build an in-app "delete account" option (recommended for compliance).
  - Contact email for privacy requests.

---

## 6. Data safety form (Play Console → App content → Data safety)

| Question | Answer |
|---|---|
| Does your app collect or share any of the required user data types? | **Yes** |
| Is all of the user data collected by your app encrypted in transit? | **Yes** (HTTPS — Firebase, Maps, OpenWeather, RapidAPI all use TLS) |
| Do you provide a way for users to request that their data be deleted? | **Yes** — users request account + data deletion by emailing **merkostdev+fishing@gmail.com** (documented in the [privacy policy](https://merkost.github.io/FishingNotes/privacy.html#5-deleting-your-data)). No in-app delete button for v1.0.0; the support-email method is an accepted deletion channel. In the Data safety form, choose "Yes" and provide that email as the deletion-request contact. |

### Data types — collected & shared

| Data type | Collected | Shared with 3rd party | Why | Optional? |
|---|---|---|---|---|
| **Email address** | Yes | No (only Firebase backend) | Account creation, sync | No (required for sign-in) |
| **Name** | Yes | No | Profile display | No (taken from Google account) |
| **Photos** | Yes | No (Firebase Storage) | Catch photos | Yes (optional per catch) |
| **Approximate location** | Yes | Yes (Google Maps, OpenWeatherMap) | Show map, fetch weather | Yes (skippable, but degrades features) |
| **Precise location** | Yes | Yes (Google Maps, OpenWeatherMap) | Drop-pin accuracy, weather coordinates | Yes |
| **App activity (in-app actions)** | Yes | Yes (Firebase Analytics) | Funnel + crash analytics | No |
| **Crash logs** | Yes | Yes (Firebase Crashlytics) | Stability | No |
| **Diagnostics / performance** | Yes | Yes (Firebase Performance) | Speed monitoring | No |
| **Advertising ID** | Yes | Yes (Google AdMob) | Personalized ads | Yes (system-level opt-out via Android settings) |

### Security practices to declare
- ✅ Data encrypted in transit
- ⚠️ "Encrypted at rest" — depends on Firebase/Firestore (Google does this; you can claim ✅)
- ✅ Users can request data deletion (only if you add in-app delete OR provide a contact)
- ✅ Independent security review — leave **No** (you don't have an audit)

---

## 7. App content questionnaires

Play Console → **Policy → App content** — work through every section:

- [ ] **Privacy policy** — paste URL.
- [ ] **App access** — "All functionality is available without restrictions" since users can sign in with any Google account. (Provide test credentials if you ever gate behind invitation/role.)
- [ ] **Ads** — **Yes, my app contains ads** (AdMob).
- [ ] **Content rating** — complete IARC questionnaire.
- [ ] **Target audience** — set 18+ unless you do the Families flow.
- [ ] **News app** — No.
- [ ] **COVID-19 contact tracing/status** — No.
- [ ] **Data safety** — section 6 above.
- [ ] **Government app** — No.
- [ ] **Financial features** — No.
- [ ] **Health connect** — No.
- [ ] **Actions on Google** — No.

---

## 8. Permissions justification (only requested for sensitive perms)

Play Console may ask for written justification of certain permissions. Pre-write these.

| Permission | Justification (paste) |
|---|---|
| `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION` | Location is used to (1) center the map on the user's current position when they pick a fishing spot, (2) calculate distance from the user to saved places, and (3) fetch weather for the user's current location. Location is requested only after explicit user permission via the system dialog and is never used in the background. |
| `READ_EXTERNAL_STORAGE` / `READ_MEDIA_IMAGES` | Allow users to attach photos from their gallery to a catch entry. The app reads only images the user explicitly picks. |
| `AD_ID` | Required by Google AdMob to serve ads in the free version. Users can reset or limit this ID via Android system settings. |

---

## 9. Pricing & distribution

| Field | Value |
|---|---|
| **Countries** | All countries available (default) — exclude only countries with sanctions, e.g., as listed in the Play console default block. |
| **Price** | Free |
| **In-app purchases** | None |
| **Contains ads** | Yes |
| **Designed for Families** | No |

---

## 10. App release — internal/closed/open testing tracks

Recommended path:
1. **Internal testing** — upload AAB, add up to 100 testers by email. Live within minutes.
2. **Closed testing** — same AAB, broader tester group via Google Group / opt-in URL. Required for new accounts as of 2025: **20+ testers for at least 14 days continuously** before production.
3. **Open testing (optional)** — public opt-in URL.
4. **Production** — global rollout.

### Build the upload artifact

```
./gradlew :androidApp:bundleRelease
```

Output: `androidApp/build/outputs/bundle/release/androidApp-release.aab`. Upload this `.aab` to Play Console.

> Make sure `local.properties` has `KEYSTORE_PATH=MerkostDev.jks` and `KEYSTORE_PASSWORD` set so the release build is signed correctly. Run `./gradlew :androidApp:validateSigningRelease` first to confirm.

---

## 11. Pre-submit checklist

- [x] versionName / versionCode set to **1.0.0 / 1** (fresh first release; bump versionCode for every later upload)
- [ ] Tested **debug** build end-to-end on a real device (sign in, save a place, log a catch, check weather, sign out, sign back in)
- [ ] Tested **release** build (`./gradlew :androidApp:installRelease`) — verified Google Sign-In works (correct SHA-1 in Firebase)
- [ ] Verified `google-services.json` in `androidApp/` contains `com.merkost.fishingnotes` (release) AND `com.merkost.fishingnotes.debug` entries with correct certificate hashes
- [ ] Privacy policy URL is live and reachable
- [ ] Screenshots exported at correct dimensions, named in upload order (`01-…`, `02-…` …)
- [ ] Feature graphic 1024×500 PNG ready
- [ ] Icon 512×512 PNG ready (no transparency, no rounded mask — Play applies its own)
- [ ] Data safety answers match what the app actually does
- [ ] Release notes written (under 500 chars)
- [ ] Crashlytics enabled and uploading dSYMs/mapping (mapping file is auto-uploaded by Crashlytics Gradle plugin on release builds)
- [ ] Tested back-button → app exits properly
- [ ] No references to `localhost`, dev API endpoints, or test data anywhere in release build
- [ ] No `Log.d` / `println` / debug-only `BuildKonfig.IS_DEBUG` flags accidentally true in release

---

## 12. Post-launch (first week)

- [ ] Monitor Crashlytics daily
- [ ] Monitor Play Console **vitals** (ANR rate < 0.47%, crash rate < 1.09% to avoid penalty)
- [ ] Watch first reviews — respond to all 1- and 2-star reviews within 24h
- [ ] Set up Play Console email alerts for crashes, vitals, and policy issues
- [ ] If using staged rollout: start at 5%, monitor, ramp to 25% → 50% → 100% over 5–7 days

---

## 13. Things still to do **before** you can submit (project-side)

| Task | Why |
|---|---|
| ~~Add "Delete my account" button in app~~ | **Deferred for v1.0.0.** Account deletion is handled via the support email `merkostdev+fishing@gmail.com`, documented in the privacy policy. An in-app delete button is recommended later but not blocking. |
| ~~Privacy policy URL~~ | **Done.** Hosted at `https://merkost.github.io/FishingNotes/privacy.html` (enable GitHub Pages on `/docs`). |
| ~~Support email~~ | **Done.** `merkostdev+fishing@gmail.com`. |
| ~~Bump versionCode~~ | **Done.** Set to versionName `1.0.0` / versionCode `1` (fresh first release). |
| **Run `:androidApp:bundleRelease`** | Generates the AAB to upload. |

---

## 14. Reference — useful links

- Play Console: <https://play.google.com/console>
- Data safety form guide: <https://support.google.com/googleplay/android-developer/answer/10787469>
- Content rating (IARC): <https://support.google.com/googleplay/android-developer/answer/188189>
- AdMob policies: <https://support.google.com/admob/answer/6128543>
- Firebase console (manage SHA-1, download `google-services.json`): <https://console.firebase.google.com>
