# FishingNotes — App Roadmap

> **Purpose**: This file is a prioritized roadmap for improvements, bug fixes, and new features. It serves as a living document to guide development.

---

## Phase 1: Stability & Quality (Critical)

These items address crashes, data loss risks, and broken functionality. Ship-blocking.

### Bug Fixes
| # | Issue | File(s) | Notes |
|---|-------|---------|-------|
| 1 | Non-null assertion crash on `graph.findNode(graph.startDestinationId)!!` | `ui/FishingNotesAppStateHolder.kt` | Replace with safe call + fallback |
| 2 | Non-null assertion crash on `it.exception!!` | `model/datasource/firebase/FirebaseCatchesRepositoryImpl.kt:164` | Exception can be null |
| 3 | Text overlaps distance label in marker info dialog | `ui/home/map/MarkerInfoDialog.kt:189` | Existing TODO — layout issue |
| 4 | Cannot navigate to place screen from marker dialog | `ui/home/map/MarkerInfoDialog.kt:348` | Existing TODO — broken nav |
| 5 | Signing config has missing keystore passwords | `build.gradle.kts:30-32` | Release builds will fail |
| 6 | Mixed Russian string in English `strings.xml` (line ~82) | `res/values/strings.xml` | Permission message not translated |
| 7 | Deprecated `getFileFromUri()` still in use | `model/datasource/utils/PhotoUtils.kt:20` | Replace with `fileFromContentUri()` |
| 8 | Snapshot listeners may leak on error paths | `FirebaseCatchesRepositoryImpl.kt` | Add proper cleanup in catch blocks |

### Error Handling Overhaul
| # | Task | File(s) |
|---|------|---------|
| 9 | Uncomment and fix error differentiation in `SafeApiCall.kt` | `model/datasource/utils/SafeApiCall.kt:14-27` |
| 10 | Promote `Log.d` to `Log.e` for snapshot listener errors | `FirebaseCatchesRepositoryImpl.kt:125,223` |
| 11 | Add error UI states (snackbar / retry) to Weather, Notes, Map screens | `ui/home/weather/`, `ui/home/notes/`, `ui/home/map/` |
| 12 | Handle network loss mid-save gracefully (queued retry or user message) | Catch/Marker save flows |

### Security
| # | Task | File(s) |
|---|------|---------|
| 13 | Move `RAPIDAPI_KEY` out of BuildConfig into `local.properties` / Secrets Gradle Plugin | `di/RepositoryModule.kt`, `build.gradle.kts` |
| 14 | Audit Firestore security rules (server-side) | Firebase Console |
| 15 | Add certificate pinning for weather/solunar API calls | OkHttp config |

---

## Phase 2: Testing Foundation (High Priority)

Current test coverage is effectively 0%. This phase builds a safety net before adding features.

| # | Task | Scope |
|---|------|-------|
| 16 | Unit tests for all Use Cases (15 use cases) | `domain/use_cases/` |
| 17 | Unit tests for core ViewModels: `NewCatchMasterViewModel`, `MapViewModel`, `UserViewModel` | `viewmodels/` |
| 18 | Unit tests for Firebase repository mappers | `model/mappers/` |
| 19 | Integration tests for `FirebaseCatchesRepositoryImpl` (with emulator) | `model/datasource/firebase/` |
| 20 | Compose UI tests for critical flows: new catch wizard, login, map marker creation | `ui/home/` |
| 21 | Set up CI pipeline (GitHub Actions) with test + lint gates | Project root |

---

## Phase 3: Core UX Improvements (High Priority)

Improvements that directly affect daily user experience.

### Input Validation
| # | Task | Details |
|---|------|---------|
| 22 | Validate fish weight range (0–500 kg) with error message | `NewCatchMasterViewModel` |
| 23 | Validate fish amount range (0–9999) | `NewCatchMasterViewModel` |
| 24 | Prevent future dates in catch date picker | New catch wizard |
| 25 | Trim whitespace and enforce max length on place names | Place creation dialog |
| 26 | Validate latitude/longitude ranges on marker creation | `MapViewModel` |

### Missing UI States
| # | Task | Screen(s) |
|---|------|-----------|
| 27 | Add empty state illustrations for catches list, places list | Notes tab |
| 28 | Add error state with retry button on weather fetch failure | Weather tab |
| 29 | Add loading skeleton / shimmer for catch and place lists | Notes tab |
| 30 | Add success animation/feedback after saving a catch | New catch wizard |
| 31 | Add confirmation dialogs before delete actions (catch, place, note) | Detail screens |
| 32 | Add pull-to-refresh on catches, places, and weather screens | Notes & Weather tabs |

### Photo Improvements
| # | Task | Details |
|---|------|---------|
| 33 | Implement pinch-to-zoom and rotation on photo viewer | `PhotoViews.kt:395` (existing TODO) |
| 34 | Show photo upload progress indicator per photo | New catch wizard, catch edit |
| 35 | Allow camera capture directly (not just gallery pick) | New catch flow |
| 36 | Thumbnail generation for faster list loading | Photo grid components |

---

## Phase 4: Offline & Performance (Medium Priority)

### Offline Support
| # | Task | Details |
|---|------|---------|
| 37 | Implement Room database for local catch/place cache | New module: `data/local/` |
| 38 | Queue offline edits and sync on reconnect (WorkManager) | `OfflineRepository` |
| 39 | Cache weather data locally with TTL (1 hour) | `WeatherRepository` |
| 40 | Cache map tiles for offline viewing | Google Maps config |
| 41 | Show sync status indicator (synced / pending / conflict) | Bottom bar or catch items |
| 42 | Implement conflict resolution for simultaneous edits | Sync layer |

### Performance
| # | Task | Details |
|---|------|---------|
| 43 | Paginate catches list (Firestore `.limit()` + Paging 3) | `FirebaseCatchesRepositoryImpl` |
| 44 | Paginate places list similarly | `FirebaseMarkersRepositoryImpl` |
| 45 | Audit `remember`/`mutableStateOf` for unnecessary recompositions | All Compose screens |
| 46 | Add `@Stable` / `@Immutable` annotations to data classes used in Compose | `domain/entity/` |
| 47 | Lazy-load photos in catch detail (load on scroll into view) | `PhotoViews.kt` |
| 48 | Profile and optimize Firebase snapshot listener count | All repositories |

---

## Phase 5: New Features — Catch Experience (Medium Priority)

| # | Feature | Description |
|---|---------|-------------|
| 49 | **Catch Statistics Dashboard** | Charts: catches over time, top species, weight distribution, best months, activity by weather condition. Use a chart library (Vico or MPAndroidChart). |
| 50 | **Fish Species Database** | Pre-populated list of common fish species with images. Auto-complete on catch creation. Allow custom species. |
| 51 | **Catch Comparison** | Side-by-side compare two catches (weight, conditions, location). Identify what works. |
| 52 | **Personal Records Tracking** | Auto-detect and highlight personal bests: heaviest fish, most fish in a day, longest streak. Badge/achievement system. |
| 53 | **Catch Calendar View** | Monthly calendar showing days with catches, color-coded by success. Tap a day to see catches. |
| 54 | **Fishing Trip / Session Grouping** | Group multiple catches into a "trip" with start/end time, total stats, and trip notes. |
| 55 | **Quick Catch Mode** | One-tap catch logging: auto-fill location, date, weather. Just enter species + weight. |
| 56 | **Catch Tags / Labels** | Custom tags (e.g., "tournament", "release", "keeper") for filtering and search. |
| 57 | **Voice Notes on Catches** | Record audio memo attached to a catch for quick hands-free notes. |

---

## Phase 6: New Features — Map & Location (Medium Priority)

| # | Feature | Description |
|---|---------|-------------|
| 58 | **Heatmap Layer** | Overlay heatmap on the map showing catch density. Identify hot spots at a glance. |
| 59 | **Cluster Markers** | Group nearby markers into clusters that expand on zoom. Improves performance with many places. |
| 60 | **Route/Trail Tracking** | Record GPS trail during a fishing trip. Show distance covered, time spent. |
| 61 | **Offline Map Areas** | Download specific map regions for offline fishing in remote areas. |
| 62 | **Place Categories** | Categorize places: lake, river, pond, sea, reservoir. Filter map by category. |
| 63 | **Place Rating System** | Rate places 1–5 stars. Sort by rating. Show average rating on map marker. |
| 64 | **Nearby Places Discovery** | Show other users' public places within radius. Discover new fishing spots. |
| 65 | **Navigation Integration** | Deep link to Google Maps / Waze for turn-by-turn navigation to a place. |
| 66 | **Place Photos** | Attach photos to places (not just catches). Show what the spot looks like. |

---

## Phase 7: New Features — Weather & Planning (Lower Priority)

| # | Feature | Description |
|---|---------|-------------|
| 67 | **Best Time to Fish Predictor** | Combine solunar, weather, and personal catch history to suggest optimal fishing windows. |
| 68 | **Weather Alerts** | Push notifications for ideal fishing conditions at saved places. |
| 69 | **Tide Data** | Tide charts and predictions for coastal fishing spots. |
| 70 | **Water Temperature** | Integrate water temp data where available (affects fish behavior). |
| 71 | **Multi-Day Trip Planner** | Plan a trip: choose dates, see weather forecast, suggest best spots based on history. |
| 72 | **Barometric Pressure Trends** | Show pressure rising/falling trends (strong fishing indicator). |
| 73 | **Wind Map Overlay** | Visual wind direction/speed overlay on the map. |

---

## Phase 8: New Features — Social & Sharing (Lower Priority)

| # | Feature | Description |
|---|---------|-------------|
| 74 | **Public Feed / Timeline** | Scrollable feed of public catches from all users. Like and comment. |
| 75 | **Follow Other Anglers** | Follow users and see their public catches in your feed. |
| 76 | **Fishing Challenges / Tournaments** | Weekly/monthly challenges: "Catch the heaviest bass", "Most species in a week". Leaderboard. |
| 77 | **Catch of the Day** | Daily featured catch voted by community. |
| 78 | **Share to Social Media** | Generate shareable catch cards (image with stats overlay) for Instagram/Facebook. |
| 79 | **Group / Club Support** | Create fishing clubs. Shared places, group stats, event planning. |
| 80 | **Spot Reviews** | Written reviews on public places. Help others find good spots. |
| 81 | **Direct Messaging** | Chat between users who share places or are in the same club. |

---

## Phase 9: Monetization & Growth (Lower Priority)

| # | Feature | Description |
|---|---------|-------------|
| 82 | **Premium Subscription** | Remove ads, unlock advanced stats, unlimited photos, priority weather data. |
| 83 | **Pro Weather** | Hourly forecasts, extended 14-day forecast, historical weather at spots. |
| 84 | **Export Data** | Export catches to CSV/PDF for personal records or tournament submissions. |
| 85 | **Widgets** | Home screen widgets: today's fish activity, weather at favorite spot, recent catches. |
| 86 | **Wear OS Companion** | Quick catch logging from smartwatch. See fish activity on wrist. |
| 87 | **Apple Watch / iOS Port** | Expand to iOS with Kotlin Multiplatform (KMP). |
| 88 | **Localization Expansion** | Add Spanish, Portuguese, German, Finnish, Norwegian (popular fishing markets). |

---

## Phase 10: Code Quality & Tech Debt (Ongoing)

| # | Task | Details |
|---|------|---------|
| 89 | Remove all dead/commented-out code | `SafeApiCall.kt`, `PhotoViews.kt`, `CatchScreenDialogs.kt`, `FirebaseCatchesRepositoryImpl.kt` |
| 90 | Implement filters on Notes screen | `Notes.kt:135` (existing TODO) |
| 91 | Complete Google Sign-In button composable | `GoogleButton.kt:22` (existing TODO) |
| 92 | Add `contentDescription` to all interactive elements | All Compose screens |
| 93 | Add semantic grouping modifiers for screen readers | All Compose screens |
| 94 | Standardize spacing/padding to theme constants | All Compose screens |
| 95 | Replace hardcoded API base URLs with BuildConfig fields | `WeatherRepositoryRetrofitImpl.kt`, `FreeWeatherRepositoryImpl.kt`, `SolunarRetrofitRepositoryImpl.kt` |
| 96 | Set up Detekt / ktlint for static analysis | Project root |
| 97 | Add Baseline Profiles for startup performance | `app/` module |
| 98 | Migrate from Coil 2.x to Coil 3.x (Compose-first) | Image loading layer |
| 99 | Add ProGuard rules audit for all 3rd-party libs | `proguard-rules.pro` |
| 100 | Implement structured logging (Timber) replacing `Log.d` calls | All files using `android.util.Log` |

---

## Priority Summary

| Priority | Phases | Focus |
|----------|--------|-------|
| **Critical** | 1 | Fix crashes, broken features, security issues |
| **High** | 2–3 | Testing foundation, core UX polish |
| **Medium** | 4–6 | Offline, performance, major new features |
| **Lower** | 7–9 | Weather tools, social features, monetization |
| **Ongoing** | 10 | Tech debt cleanup alongside all other work |
