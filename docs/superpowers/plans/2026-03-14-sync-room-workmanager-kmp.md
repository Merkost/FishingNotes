# Sync / Room / WorkManager KMP Migration

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Move SyncAware repositories, SyncStatusManager, and CachedWeatherRepository to commonMain by extracting a SyncScheduler interface and leveraging Room KMP's `withTransaction`.

**Architecture:** Extract `SyncScheduler` interface to commonMain (Android actual uses WorkManager). The 4 repository/manager files depend only on Room DAOs, ConnectionManager interface, and SyncScheduler interface — all KMP-compatible. `SyncWorker` stays in androidMain (extends `CoroutineWorker`). Split `RepositoryModule` so repository bindings live in commonMain while Android platform factories stay in androidMain.

**Tech Stack:** Room KMP 2.8.4, WorkManager 2.11.1 (Android-only), Koin 4.1.1, kotlinx.coroutines 1.10.2, kotlinx.serialization

---

## File Map

| Action | File | Notes |
|--------|------|-------|
| Create | `shared/src/commonMain/.../sync/SyncScheduler.kt` | Interface: `scheduleSync()`, `schedulePeriodicSync()` |
| Rename+refactor | `shared/src/androidMain/.../sync/SyncScheduler.kt` → `SyncSchedulerImpl.kt` | Implements interface |
| Move | `shared/src/androidMain/.../sync/SyncStatusManager.kt` → commonMain | `java.io.Closeable` → `AutoCloseable` |
| Move | `shared/src/androidMain/.../SyncAwareCatchesRepository.kt` → commonMain | `java.io.Closeable` → `AutoCloseable` |
| Move | `shared/src/androidMain/.../SyncAwareMarkersRepository.kt` → commonMain | `java.io.Closeable` → `AutoCloseable` |
| Move | `shared/src/androidMain/.../CachedWeatherRepository.kt` → commonMain | No changes needed |
| Stay | `shared/src/androidMain/.../sync/SyncWorker.kt` | Extends `CoroutineWorker` — Android-only |
| Create | `shared/src/commonMain/.../di/RepositoryModule.kt` | KMP repo bindings |
| Refactor | `shared/src/androidMain/.../di/RepositoryModule.kt` | Keep only platform factories |
| Update | `shared/src/androidMain/.../di/KoinModules.kt` | `Closeable` → `AutoCloseable` in `onClose` |
| Update | `androidApp/.../FishingApp.kt` | Import `SyncScheduler` from new location |

---

### Task 1: Extract SyncScheduler interface to commonMain

**Files:**
- Create: `shared/src/commonMain/kotlin/com/mobileprism/fishing/model/datasource/local/sync/SyncScheduler.kt`
- Rename: `shared/src/androidMain/.../sync/SyncScheduler.kt` → `SyncSchedulerImpl.kt`

- [ ] **Step 1: Create SyncScheduler interface in commonMain**

```kotlin
// shared/src/commonMain/kotlin/com/mobileprism/fishing/model/datasource/local/sync/SyncScheduler.kt
package com.mobileprism.fishing.model.datasource.local.sync

interface SyncScheduler {
    fun scheduleSync()
    fun schedulePeriodicSync()
}
```

- [ ] **Step 2: Rename androidMain SyncScheduler.kt → SyncSchedulerImpl.kt, implement interface**

Rename file to `SyncSchedulerImpl.kt`. Change class declaration:

```kotlin
// Before:
class SyncScheduler(private val context: Context) {

// After:
class SyncSchedulerImpl(private val context: Context) : SyncScheduler {
```

Everything else stays identical — `scheduleSync()` and `schedulePeriodicSync()` already match the interface.

- [ ] **Step 3: Update RepositoryModule.kt — SyncScheduler → SyncSchedulerImpl**

In `shared/src/androidMain/.../di/RepositoryModule.kt`:

```kotlin
// Before:
single { SyncScheduler(androidContext()) }

// After:
single<SyncScheduler> { SyncSchedulerImpl(androidContext()) }
```

Update import: `SyncScheduler` → `SyncSchedulerImpl` (add import for `SyncSchedulerImpl`; `SyncScheduler` now resolves from commonMain interface).

- [ ] **Step 4: Update FishingApp.kt import**

In `androidApp/src/main/java/com/mobileprism/fishing/app/FishingApp.kt`:

The import `com.mobileprism.fishing.model.datasource.local.sync.SyncScheduler` stays the same — it now points to the commonMain interface. The `get<SyncScheduler>()` call still works because Koin binds `SyncSchedulerImpl` as `SyncScheduler`.

No changes needed if the package path is identical.

- [ ] **Step 5: Verify build**

Run: `./gradlew :shared:compileDebugKotlinAndroid`
Expected: BUILD SUCCESSFUL

---

### Task 2: Move SyncStatusManager to commonMain

**Files:**
- Move: `shared/src/androidMain/.../sync/SyncStatusManager.kt` → `shared/src/commonMain/.../sync/SyncStatusManager.kt`

- [ ] **Step 1: Move file and replace Closeable**

Move the file to commonMain. Change:

```kotlin
// Before:
import java.io.Closeable

class SyncStatusManager(...) : SyncStatusProvider, Closeable {

// After:
class SyncStatusManager(...) : SyncStatusProvider, AutoCloseable {
```

Remove the `import java.io.Closeable` line. `AutoCloseable` is in `kotlin` stdlib — no import needed.

All other code stays identical. Dependencies (`PendingOperationDao`, `ConnectionManager`, `SyncState`, coroutines) are all commonMain-compatible.

- [ ] **Step 2: Update Koin onClose cast in RepositoryModule.kt**

In `shared/src/androidMain/.../di/RepositoryModule.kt`:

```kotlin
// Before:
single {
    SyncStatusManager(pendingOpsDao = get(), connectionManager = get())
} onClose { (it as? Closeable)?.close() }

// After:
single {
    SyncStatusManager(pendingOpsDao = get(), connectionManager = get())
} onClose { (it as? AutoCloseable)?.close() }
```

- [ ] **Step 3: Verify build**

Run: `./gradlew :shared:compileDebugKotlinAndroid`
Expected: BUILD SUCCESSFUL

---

### Task 3: Move CachedWeatherRepository to commonMain

**Files:**
- Move: `shared/src/androidMain/.../local/CachedWeatherRepository.kt` → `shared/src/commonMain/.../local/CachedWeatherRepository.kt`

- [ ] **Step 1: Move file — no code changes**

This file has zero Android-specific imports. All dependencies are already in commonMain:
- `WeatherRepository` (commonMain interface)
- `WeatherCacheDao` (commonMain DAO)
- `ConnectionManager` (commonMain interface)
- `kotlinx.serialization` (commonMain)
- `Cedar` logger (commonMain)

Move the file as-is.

- [ ] **Step 2: Verify build**

Run: `./gradlew :shared:compileDebugKotlinAndroid`
Expected: BUILD SUCCESSFUL

---

### Task 4: Move SyncAwareCatchesRepository to commonMain

**Files:**
- Move: `shared/src/androidMain/.../local/SyncAwareCatchesRepository.kt` → `shared/src/commonMain/.../local/SyncAwareCatchesRepository.kt`

- [ ] **Step 1: Move file, replace Closeable**

Move to commonMain. Change:

```kotlin
// Before:
import java.io.Closeable

class SyncAwareCatchesRepository(...) : CatchesRepository, Closeable {

// After:
class SyncAwareCatchesRepository(...) : CatchesRepository, AutoCloseable {
```

Remove `import java.io.Closeable`.

All other imports are KMP-compatible:
- `androidx.room.withTransaction` — Room KMP
- `SyncScheduler` — now the commonMain interface
- `CatchDao`, `PendingOperationDao` — commonMain DAOs
- `ConnectionManager` — commonMain interface
- `Cedar`, `kotlinx.serialization`, `kotlinx.coroutines` — all KMP

- [ ] **Step 2: Update Koin onClose cast in RepositoryModule.kt**

```kotlin
// Before:
single<CatchesRepository> {
    SyncAwareCatchesRepository(...)
} onClose { (it as? Closeable)?.close() }

// After:
single<CatchesRepository> {
    SyncAwareCatchesRepository(...)
} onClose { (it as? AutoCloseable)?.close() }
```

- [ ] **Step 3: Verify build**

Run: `./gradlew :shared:compileDebugKotlinAndroid`
Expected: BUILD SUCCESSFUL

---

### Task 5: Move SyncAwareMarkersRepository to commonMain

**Files:**
- Move: `shared/src/androidMain/.../local/SyncAwareMarkersRepository.kt` → `shared/src/commonMain/.../local/SyncAwareMarkersRepository.kt`

- [ ] **Step 1: Move file, replace Closeable**

Same pattern as Task 4:

```kotlin
// Before:
import java.io.Closeable

class SyncAwareMarkersRepository(...) : MarkersRepositoryPaged, Closeable {

// After:
class SyncAwareMarkersRepository(...) : MarkersRepositoryPaged, AutoCloseable {
```

Remove `import java.io.Closeable`.

- [ ] **Step 2: Update Koin onClose cast in RepositoryModule.kt**

```kotlin
// Before:
single<MarkersRepositoryPaged> {
    SyncAwareMarkersRepository(...)
} onClose { (it as? Closeable)?.close() }

// After:
single<MarkersRepositoryPaged> {
    SyncAwareMarkersRepository(...)
} onClose { (it as? AutoCloseable)?.close() }
```

- [ ] **Step 3: Verify build**

Run: `./gradlew :shared:compileDebugKotlinAndroid`
Expected: BUILD SUCCESSFUL

---

### Task 6: Split RepositoryModule — move KMP bindings to commonMain

**Files:**
- Create: `shared/src/commonMain/kotlin/com/mobileprism/fishing/di/RepositoryModule.kt`
- Refactor: `shared/src/androidMain/kotlin/com/mobileprism/fishing/di/RepositoryModule.kt`

The current `RepositoryModule.kt` in androidMain mixes platform-specific and platform-agnostic bindings. Split it so that all repository bindings that use only commonMain types move to a new commonMain module, and androidMain keeps only the platform factories.

- [ ] **Step 1: Create commonMain RepositoryModule**

Create `shared/src/commonMain/kotlin/com/mobileprism/fishing/di/RepositoryModule.kt`:

```kotlin
package com.mobileprism.fishing.di

import com.mobileprism.fishing.domain.repository.AuthRepository
import com.mobileprism.fishing.domain.repository.UserRepository
import com.mobileprism.fishing.domain.repository.app.FreeWeatherRepository
import com.mobileprism.fishing.domain.repository.app.MarkersRepository
import com.mobileprism.fishing.domain.repository.app.MarkersRepositoryPaged
import com.mobileprism.fishing.domain.repository.app.OfflineRepository
import com.mobileprism.fishing.domain.repository.app.SolunarRepository
import com.mobileprism.fishing.domain.repository.app.WeatherRepository
import com.mobileprism.fishing.domain.repository.app.catches.CatchesRepository
import com.mobileprism.fishing.domain.repository.app.catches.CatchesRepositoryRead
import com.mobileprism.fishing.domain.repository.app.catches.CatchesRepositoryUpdate
import com.mobileprism.fishing.model.datasource.FreeWeatherRepositoryKtorImpl
import com.mobileprism.fishing.model.datasource.SolunarRepositoryKtorImpl
import com.mobileprism.fishing.model.datasource.firebase.FirebaseAuthRepository
import com.mobileprism.fishing.model.datasource.firebase.FirebaseCatchesPagedRepository
import com.mobileprism.fishing.model.datasource.firebase.FirebaseCatchesRepositoryImpl
import com.mobileprism.fishing.model.datasource.firebase.FirebaseMarkersPagedRepository
import com.mobileprism.fishing.model.datasource.firebase.FirebaseMarkersRepositoryImpl
import com.mobileprism.fishing.model.datasource.firebase.FirebaseOfflineRepositoryImpl
import com.mobileprism.fishing.model.datasource.firebase.FirebaseUserRepositoryImpl
import com.mobileprism.fishing.model.datasource.local.CachedWeatherRepository
import com.mobileprism.fishing.model.datasource.local.FishingDatabase
import com.mobileprism.fishing.model.datasource.local.SyncAwareCatchesRepository
import com.mobileprism.fishing.model.datasource.local.SyncAwareMarkersRepository
import com.mobileprism.fishing.model.datasource.local.sync.SyncStatusManager
import com.mobileprism.fishing.model.datasource.utils.RepositoryCollections
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.dsl.onClose

val commonRepositoryModule = networkModule + module {
    single<AuthRepository> { FirebaseAuthRepository() }
    single { RepositoryCollections(authRepository = get()) }

    single<UserRepository> {
        FirebaseUserRepositoryImpl(
            userDatastore = get(),
            dbCollections = get(),
            analyticsTracker = get()
        )
    }

    // DAOs
    single { get<FishingDatabase>().catchDao() }
    single { get<FishingDatabase>().markerDao() }
    single { get<FishingDatabase>().pendingOperationDao() }
    single { get<FishingDatabase>().weatherCacheDao() }

    // Sync infrastructure
    single {
        SyncStatusManager(pendingOpsDao = get(), connectionManager = get())
    } onClose { (it as? AutoCloseable)?.close() }

    // Core Firebase implementations
    single { FirebaseCatchesRepositoryImpl(
        dbCollections = get(),
        analyticsTracker = get(),
        connectionManager = get(),
        authRepository = get()
    ) } onClose { (it as? AutoCloseable)?.close() }
    single { FirebaseMarkersRepositoryImpl(
        dbCollections = get(),
        analyticsTracker = get()
    ) } onClose { (it as? AutoCloseable)?.close() }

    // Paged wrappers
    single<CatchesRepository>(named("firebase")) {
        FirebaseCatchesPagedRepository(
            coreRepo = get(),
            authRepository = get(),
            dbCollections = get()
        )
    }
    single<MarkersRepositoryPaged>(named("firebase")) {
        FirebaseMarkersPagedRepository(
            coreRepo = get(),
            authRepository = get(),
            dbCollections = get()
        )
    }

    // SyncAware wrappers (default bindings)
    single<CatchesRepository> {
        SyncAwareCatchesRepository(
            firebaseRepo = get(named("firebase")),
            catchDao = get(),
            pendingOpsDao = get(),
            connectionManager = get(),
            syncScheduler = get(),
            db = get()
        )
    } onClose { (it as? AutoCloseable)?.close() }
    single<MarkersRepositoryPaged> {
        SyncAwareMarkersRepository(
            firebaseRepo = get(named("firebase")),
            markerDao = get(),
            pendingOpsDao = get(),
            connectionManager = get(),
            syncScheduler = get(),
            db = get()
        )
    } onClose { (it as? AutoCloseable)?.close() }
    single<MarkersRepository> { get<MarkersRepositoryPaged>() }
    single<CatchesRepositoryRead> { get<CatchesRepository>() }
    single<CatchesRepositoryUpdate> { get<CatchesRepository>() }

    single<WeatherRepository> {
        CachedWeatherRepository(
            remoteRepo = get(named("remote")),
            weatherCacheDao = get(),
            connectionManager = get()
        )
    }

    single<SolunarRepository> {
        SolunarRepositoryKtorImpl(
            analyticsTracker = get(),
            httpClient = get()
        )
    }
    single<FreeWeatherRepository> {
        FreeWeatherRepositoryKtorImpl(
            analyticsTracker = get(),
            rapidApiKey = com.mobileprism.fishing.BuildKonfig.RAPIDAPI_KEY,
            httpClient = get()
        )
    }
    single<OfflineRepository> { FirebaseOfflineRepositoryImpl(dbCollections = get()) }
}
```

**Note:** `FirebaseCatchesRepositoryImpl` and `FirebaseMarkersRepositoryImpl` currently implement `java.io.Closeable`. If they're in commonMain, they should also be changed to `AutoCloseable`. Check and update if needed.

- [ ] **Step 2: Refactor androidMain RepositoryModule to platform-only factories**

Replace `shared/src/androidMain/kotlin/com/mobileprism/fishing/di/RepositoryModule.kt` with:

```kotlin
package com.mobileprism.fishing.di

import androidx.core.os.LocaleListCompat
import androidx.room.Room
import com.mobileprism.fishing.BuildKonfig
import com.mobileprism.fishing.domain.repository.PhotoStorage
import com.mobileprism.fishing.domain.repository.app.WeatherRepository
import com.mobileprism.fishing.model.datasource.WeatherRepositoryKtorImpl
import com.mobileprism.fishing.model.datasource.firebase.FirebaseCloudPhotoStorage
import com.mobileprism.fishing.model.datasource.local.FishingDatabase
import com.mobileprism.fishing.model.datasource.local.sync.SyncScheduler
import com.mobileprism.fishing.model.datasource.local.sync.SyncSchedulerImpl
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

val platformRepositoryModule = module {
    // Room Database (requires Android Context)
    single {
        Room.databaseBuilder(
            androidContext(),
            FishingDatabase::class.java,
            "fishing_database"
        ).build()
    }

    // SyncScheduler (requires WorkManager / Android Context)
    single<SyncScheduler> { SyncSchedulerImpl(androidContext()) }

    // WeatherRepository remote impl (requires LocaleListCompat)
    single<WeatherRepository>(named("remote")) {
        WeatherRepositoryKtorImpl(
            analyticsTracker = get(),
            openWeatherKey = BuildKonfig.OPENWEATHER_KEY,
            languageTag = LocaleListCompat.getAdjustedDefault().toLanguageTags().take(2),
            httpClient = get()
        )
    }

    // PhotoStorage (requires Android Context)
    single<PhotoStorage> {
        FirebaseCloudPhotoStorage(
            analyticsTracker = get(),
            context = androidContext()
        )
    }
}

val repositoryModule = commonRepositoryModule + platformRepositoryModule
```

This keeps `repositoryModule` as the public API name so `FishingApp.kt` doesn't change.

- [ ] **Step 3: Update FirebaseCatchesRepositoryImpl and FirebaseMarkersRepositoryImpl Closeable**

These files are already in commonMain. Check if they use `java.io.Closeable`:
- If yes: change to `AutoCloseable`, remove `java.io.Closeable` import
- If no: no changes needed

- [ ] **Step 4: Remove stale import of java.io.Closeable from androidMain RepositoryModule**

The old androidMain `RepositoryModule.kt` imported `java.io.Closeable`. The new version doesn't need it. Verify the rewritten file has no `java.io.Closeable` import.

- [ ] **Step 5: Verify build**

Run: `./gradlew :shared:compileDebugKotlinAndroid`
Expected: BUILD SUCCESSFUL

---

### Task 7: Clean up — remove empty androidMain directories

- [ ] **Step 1: Remove empty directories if any**

After moving files, check if `shared/src/androidMain/.../model/datasource/local/` still has files. `SyncWorker.kt` and `SyncSchedulerImpl.kt` remain in `sync/` subdirectory. The `local/` directory itself may be empty if all 3 repository files moved out. Remove empty dirs.

- [ ] **Step 2: Final full build + test verification**

Run: `./gradlew :shared:compileDebugKotlinAndroid`
Expected: BUILD SUCCESSFUL

Run: `./gradlew :shared:testDebugUnitTest`
Expected: BUILD SUCCESSFUL

---

## Verification Checklist

1. `./gradlew :shared:compileDebugKotlinAndroid` — no errors
2. `./gradlew :shared:testDebugUnitTest` — all tests pass
3. Run on device:
   - Creating a catch offline queues it and syncs when online
   - Settings persist (DataStore — already verified in Phase 26)
   - Weather data caches correctly and returns stale data when offline
   - Sync status indicator shows Synced/Pending/Error correctly
   - Periodic sync runs on app startup (via `FishingApp.onCreate`)

## Files Summary

| Action | Source (androidMain) | Destination (commonMain) |
|--------|---------------------|--------------------------|
| Create interface | — | `.../sync/SyncScheduler.kt` |
| Rename | `.../sync/SyncScheduler.kt` | `.../sync/SyncSchedulerImpl.kt` (stays androidMain) |
| Move | `.../sync/SyncStatusManager.kt` | `.../sync/SyncStatusManager.kt` |
| Move | `.../local/CachedWeatherRepository.kt` | `.../local/CachedWeatherRepository.kt` |
| Move | `.../local/SyncAwareCatchesRepository.kt` | `.../local/SyncAwareCatchesRepository.kt` |
| Move | `.../local/SyncAwareMarkersRepository.kt` | `.../local/SyncAwareMarkersRepository.kt` |
| Stay | `.../sync/SyncWorker.kt` | — (Android-only, `CoroutineWorker`) |
| Create | — | `.../di/RepositoryModule.kt` (commonMain) |
| Refactor | `.../di/RepositoryModule.kt` | platform-only factories |
