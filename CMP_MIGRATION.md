# CMP Migration Plan

## Current State

- **commonMain**: 49 files (domain entities, repository interfaces, use cases, utils, Koin module)
- **androidMain**: 175 files (UI, ViewModels, data layer, remaining domain, utils, DI)

Domain layer migration is ~80% complete.

---

## Phase 6: Datastore Interfaces → commonMain

**Goal**: Move preference/datastore interfaces to commonMain, unlocking remaining use cases.

### Move interfaces (keep `impl/` in androidMain)

| File | Notes |
|------|-------|
| `model/datastore/WeatherPreferences.kt` | Check for Android types in the interface |
| `model/datastore/NotesPreferences.kt` | Pure interface |
| `model/datastore/UserDatastore.kt` | Pure interface |
| `model/datastore/UserPreferences.kt` | May need splitting if it uses Android Context |

### Then move unlocked use cases

| File | Change needed |
|------|---------------|
| `use_cases/catches/GetNewCatchWeatherUseCase.kt` | Now depends on common `WeatherPreferences` |
| `use_cases/catches/SaveNewCatchUseCase.kt` | Needs `getCurrentUserId` abstracted (see Phase 7) |

### Verification
- `./gradlew :shared:assembleDebug`

---

## Phase 7: Abstract Auth & Move Remaining Use Cases

**Goal**: Remove Firebase Auth coupling from domain layer.

### Create auth abstraction in commonMain

```kotlin
// commonMain: domain/repository/AuthRepository.kt
interface AuthRepository {
    fun getCurrentUserId(): String
}
```

### Create implementation in androidMain

```kotlin
// androidMain: model/datasource/firebase/FirebaseAuthRepository.kt
class FirebaseAuthRepository : AuthRepository {
    override fun getCurrentUserId() = FirebaseAuth.getInstance().currentUser?.uid ?: "Anonymous"
}
```

### Register in DI and update use cases

| File | Change |
|------|--------|
| `use_cases/catches/SaveNewCatchUseCase.kt` | Inject `AuthRepository` instead of calling `getCurrentUserId()` |
| `use_cases/places/AddNewPlaceUseCase.kt` | Same |
| `use_cases/SavePhotosUseCase.kt` | Change `List<Uri>` → `List<String>`, move to commonMain |
| `use_cases/catches/UpdateUserCatchUseCase.kt` | Remove `toUri()` call, move to commonMain |

### Verification
- `./gradlew :androidApp:assembleDebug`

---

## Phase 8: Pure Model Utilities → commonMain

**Goal**: Move platform-independent model helpers.

| File | Notes |
|------|-------|
| `model/utils/SafeApiCall.kt` | Likely pure Kotlin (Result/try-catch wrapper) |
| `model/mappers/WeatherParsers.kt` | Check for Android imports |
| `model/datasource/utils/RepositoryUtils.kt` | Check for Android imports |

### Verification
- `./gradlew :shared:assembleDebug`

---

## Phase 9: Networking — Retrofit → Ktor

**Goal**: Replace Retrofit (JVM-only) with Ktor (multiplatform HTTP client).

### Gradle changes

```toml
# libs.versions.toml
ktor = "3.1.1"

[libraries]
ktor-client-core = { group = "io.ktor", name = "ktor-client-core", version.ref = "ktor" }
ktor-client-content-negotiation = { group = "io.ktor", name = "ktor-client-content-negotiation", version.ref = "ktor" }
ktor-serialization-kotlinx-json = { group = "io.ktor", name = "ktor-serialization-kotlinx-json", version.ref = "ktor" }
ktor-client-okhttp = { group = "io.ktor", name = "ktor-client-okhttp", version.ref = "ktor" }
```

```kotlin
// shared/build.gradle.kts
commonMain.dependencies {
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
}
androidMain.dependencies {
    implementation(libs.ktor.client.okhttp)
}
```

### Migrate API services

| Current (androidMain) | Target (commonMain) |
|------------------------|---------------------|
| `model/api/WeatherApiService.kt` (Retrofit) | Ktor `HttpClient` calls |
| `model/api/FreeWeatherApiService.kt` | Ktor `HttpClient` calls |
| `model/api/SolunarApiService.kt` | Ktor `HttpClient` calls |

### Migrate repository implementations

| Current (androidMain) | Target (commonMain) |
|------------------------|---------------------|
| `WeatherRepositoryRetrofitImpl.kt` | Rewrite with Ktor |
| `FreeWeatherRepositoryImpl.kt` | Rewrite with Ktor |
| `SolunarRetrofitRepositoryImpl.kt` | Rewrite with Ktor |

### Verification
- `./gradlew :androidApp:assembleDebug`
- Test weather, solunar API calls on device

---

## Phase 10: Local DB — Room → Room KMP or SQLDelight

**Goal**: Make local persistence multiplatform.

### Option A: Room KMP (recommended if staying with Room)

Room 2.7+ supports KMP. Move DAOs, entities, and database definition to commonMain with `expect`/`actual` for the database builder.

### Option B: SQLDelight

Replace Room with SQLDelight for fully multiplatform SQL.

### Files affected

| Category | Files |
|----------|-------|
| Database | `FishingDatabase.kt` |
| DAOs | `CatchDao.kt`, `MarkerDao.kt`, `PendingOperationDao.kt`, `WeatherCacheDao.kt` |
| Entities | `CatchEntity.kt`, `MarkerEntity.kt`, `PendingOperationEntity.kt`, `SyncStatus.kt`, `WeatherCacheEntity.kt` |
| Mappers | `EntityMappers.kt`, `Converters.kt` |
| Repos | `CachedWeatherRepository.kt`, `SyncAwareCatchesRepository.kt`, `SyncAwareMarkersRepository.kt` |
| Sync | `SyncScheduler.kt`, `SyncStatusManager.kt`, `SyncWorker.kt` (WorkManager — stays Android) |

### Verification
- `./gradlew :androidApp:assembleDebug`
- Test offline mode, sync, cached weather

---

## Phase 11: Firebase → Multiplatform Firebase

**Goal**: Replace Google Firebase SDK with `dev.gitlive:firebase-kotlin-sdk` (multiplatform).

### Gradle changes

```toml
# libs.versions.toml
firebase-gitlive = "2.1.0"

[libraries]
firebase-gitlive-auth = { group = "dev.gitlive", name = "firebase-auth", version.ref = "firebase-gitlive" }
firebase-gitlive-firestore = { group = "dev.gitlive", name = "firebase-firestore", version.ref = "firebase-gitlive" }
firebase-gitlive-storage = { group = "dev.gitlive", name = "firebase-storage", version.ref = "firebase-gitlive" }
```

### Files affected

| File | Notes |
|------|-------|
| `FirebaseUserRepositoryImpl.kt` | Rewrite with gitlive Firebase |
| `FirebaseCatchesRepositoryImpl.kt` | Rewrite with gitlive Firebase |
| `FirebaseMarkersRepositoryImpl.kt` | Rewrite with gitlive Firebase |
| `FirebaseCloudPhotoStorage.kt` | Rewrite with gitlive Firebase Storage |
| `FirebaseOfflineRepositoryImpl.kt` | Rewrite with gitlive Firebase |
| `FirebaseAnalyticsTracker.kt` | Keep in androidMain (analytics is platform-specific) |
| `CatchesPagingSource.kt` | Keep in androidMain (Paging is Android-specific) |
| `MarkersPagingSource.kt` | Keep in androidMain |
| `FirebaseUtils.kt` | Review and split |
| `RepositoryCollections.kt` | Rewrite without Firestore imports |

### Verification
- Full app test: login, CRUD on markers/catches, photo upload, offline sync

---

## Phase 12: ViewModels → commonMain

**Goal**: Move ViewModel logic to shared code using KMP-compatible ViewModel.

### Approach

Use `androidx.lifecycle:lifecycle-viewmodel` KMP artifact (available since 2.8+) or use a `commonMain`-compatible base class.

### Files to migrate

| File | Blocker |
|------|---------|
| `MainViewModel.kt` | SyncStatusManager |
| `MapViewModel.kt` | Google Maps `LatLng`, `UserPreferences` |
| `LoginViewModel.kt` | `FirebaseAuth`, `KMPAuth` |
| `UserViewModel.kt` | None after Phase 11 |
| `EditProfileViewModel.kt` | None after Phase 6 |
| `WeatherViewModel.kt` | `LocationManager` |
| `UserCatchViewModel.kt` | None after earlier phases |
| `UserPlaceViewModel.kt` | None after earlier phases |
| `UserCatchesViewModel.kt` | Paging |
| `UserPlacesViewModel.kt` | Paging |
| `StatisticsViewModel.kt` | None |
| `NewCatchMasterViewModel.kt` | `Uri` photos |

### Verification
- `./gradlew :androidApp:assembleDebug`

---

## Phase 13: UI Layer — Compose Multiplatform

**Goal**: Migrate UI composables from Android Compose to JetBrains Compose Multiplatform.

This is the largest phase (~85 files). Proceed screen-by-screen.

### Prerequisites
- Phases 6-12 substantially complete
- Replace `R.string` with Compose Multiplatform resources (`composeResources/`)
- Replace Lottie with a KMP alternative or `expect`/`actual`

### Migration order (by dependency)

| Step | Screens | Blockers |
|------|---------|----------|
| 13a | Shared views (`Buttons`, `Cards`, `DefaultViews`, `AppBar`, `Counters`) | `R.string` → resources |
| 13b | Profile (`Profile`, `EditProfile`, `ProfileViews`) | Coil → Coil KMP |
| 13c | Settings (`SettingsScreen`, `AboutApp`) | Minimal |
| 13d | Notes/Lists (`Notes`, `UserCatchesScreen`, `UserPlacesScreen`, `StatisticsScreen`) | Paging Compose |
| 13e | Catch detail (`Catch`, `CatchScreenDialogs`) | Coil for photos |
| 13f | Place detail (`Place`, `PlaceViews`) | Google Maps embed |
| 13g | New Catch flow (`NewCatchMaster`, pages, weather icons) | Photo picker, camera |
| 13h | Map screen (`MapScreen`, `MapViews`, `NewPlaceDialog`, `MarkerInfoDialog`) | Google Maps → KMP maps |
| 13i | Login (`LoginScreen`) | KMPAuth (already multiplatform) |
| 13j | App shell (`FishingNotesApp`, `Home`, navigation) | Navigation Compose KMP |

### Key replacements

| Android | KMP Alternative |
|---------|----------------|
| `R.string.*` | `composeResources/values/strings.xml` + `stringResource()` |
| `R.drawable.*` | `composeResources/drawable/` |
| Lottie Compose | `compottie` or `expect`/`actual` |
| Coil | `io.coil-kt.coil3:coil-compose` (KMP since Coil 3) |
| Google Maps Compose | `expect`/`actual` with platform-specific map |
| Accompanist Permissions | Custom `expect`/`actual` permission handling |
| Accompanist Pager | Built-in `HorizontalPager` (Compose 1.5+) |
| AdMob views | `expect`/`actual`, keep ads Android-only |
| `android.net.Uri` photo picker | `expect`/`actual` photo picker |

### Verification
- `./gradlew :androidApp:assembleDebug`
- Full visual regression test on device

---

## Phase Summary

| Phase | Scope | Est. files | Difficulty |
|-------|-------|-----------|------------|
| ~~0-5~~ | ~~Domain layer~~ | ~~49~~ | ~~Done~~ |
| 6 | Datastore interfaces | 4-6 | Low |
| 7 | Auth abstraction + remaining use cases | 6-8 | Low |
| 8 | Model utilities | 2-3 | Low |
| 9 | Retrofit → Ktor | 6-8 | Medium |
| 10 | Room → Room KMP / SQLDelight | 12-15 | Medium-High |
| 11 | Firebase → Multiplatform | 8-10 | High |
| 12 | ViewModels | 12 | Medium |
| 13 | UI Composables | 85+ | High |

**Recommended order**: 6 → 7 → 8 → 9 → 10 → 11 → 12 → 13
