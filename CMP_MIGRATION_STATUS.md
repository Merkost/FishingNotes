# CMP Migration Status

## Overview

The Compose Multiplatform migration is ~85% complete. 41 Kotlin files remain in `androidMain`, of which 35 have hard Android dependencies and must stay until iOS targets are added.

## Ready to Move (2 files)

| File | Reason it can move |
|------|-------------------|
| `ui/utils/AnimatedResource.kt` | Pure Compose/Lottie, uses only CMP resources |
| `ui/home/map/PointerAnimation.kt` | Pure Compose/Lottie, uses only CMP resources |

## Could Move with DataStore KMP Refactor (4 files)

| File | What's needed |
|------|--------------|
| `model/datastore/impl/UserDatastoreImpl.kt` | KMP DataStore setup (`createDataStore()` expect/actual for file path) |
| `model/datastore/impl/WeatherPreferencesImpl.kt` | Same |
| `model/datastore/impl/NotesPreferencesImpl.kt` | Same |
| `di/UseCasesModule.kt` | Pure Kotlin DI bindings, could merge into commonMain |

## Must Stay in androidMain (35 files)

### Google Maps (5 files)
- `ui/home/map/MapScreen.kt` — Google Maps SDK
- `ui/home/map/MapViews.kt` — Google Maps composables
- `ui/home/map/MapUtils.kt` — Map intents, location permissions
- `viewmodels/MapViewModel.kt` — Google Maps state
- `ui/home/map/GpsCheck.kt` — Activity-based GPS resolution

### Advertising / AdMob (4 files)
- `ui/home/advertising/AdIds.kt` — AdMob banner/interstitial IDs (expect/actual)
- `ui/home/advertising/AdvertisingViews.kt` — AndroidView + AdMob banner (expect/actual)
- `ui/home/advertising/InterstitialAd.kt` — AdMob interstitial (expect/actual)
- `ui/home/advertising/AdvertisingUtils.kt` — Ad load callbacks

### Sync / Room / WorkManager (6 files)
- `model/datasource/local/SyncAwareCatchesRepository.kt` — Room transactions
- `model/datasource/local/SyncAwareMarkersRepository.kt` — Room transactions
- `model/datasource/local/sync/SyncWorker.kt` — CoroutineWorker
- `model/datasource/local/sync/SyncScheduler.kt` — WorkManager
- `model/datasource/local/sync/SyncStatusManager.kt` — DAO + ConnectionManager
- `model/datasource/local/CachedWeatherRepository.kt` — Room DAO

### DI Modules (2 files)
- `di/KoinModules.kt` — androidContext, Geocoder, AppUpdateManager, Firebase
- `di/RepositoryModule.kt` — androidContext, Room.databaseBuilder

### Navigation (2 files)
- `ui/AppNavGraph.kt` — Route definitions referencing androidMain screens (expect/actual)
- `ui/home/HomeGraph.kt` — Home tab navigation graph

### Location (3 files)
- `utils/location/LocationManagerImpl.kt` — FusedLocationProviderClient
- `utils/location/LocationUtils.kt` — Empty placeholder
- `ui/home/weather/ObserveCurrentLocation.kt` — LocationManager (expect/actual)

### Firebase Native (2 files)
- `model/datasource/firebase/FirebaseAnalyticsTracker.kt` — Firebase Analytics Android SDK
- `model/datasource/firebase/FirebaseCloudPhotoStorage.kt` — Firebase Storage + Compressor

### UI Platform Utilities (8 files)
- `ui/theme/Theme.kt` — Dynamic colors, enableEdgeToEdge (expect/actual)
- `ui/utils/PlatformUtils.kt` — Dynamic color, app version, Play Store, billing (expect/actual)
- `ui/utils/PlatformBackHandler.kt` — Android back handler (expect/actual)
- `ui/utils/MediaPicker.kt` — Activity Results API (expect/actual)
- `ui/utils/Modifier.kt` — Error toast helper
- `ui/home/place/PlaceUtilFunctions.kt` — Map navigation intents (expect/actual)
- `utils/AndroidUtilFunctions.kt` — Toast helpers
- `utils/network/NetworkMonitor.kt` — ConnectivityManager (expect/actual)

### Other (3 files)
- `utils/network/ConnectionManagerImpl.kt` — Android ConnectivityManager
- `utils/network/ConnectionUtils.kt` — Network capability checks
- `model/datasource/utils/PhotoUtils.kt` — Uri/FileProvider/Crashlytics
- `domain/use_cases/GetPlaceNameUseCase.kt` — Android Geocoder

## Active Expect/Actual Pairs (11)

| Expect (commonMain) | Actual (androidMain) | Can collapse? |
|---------------------|---------------------|---------------|
| `AnimatedResource()` | `ui/utils/AnimatedResource.kt` | YES |
| `PointerAnimation()` | `ui/home/map/PointerAnimation.kt` | YES |
| `rememberGPSChecker()` | `ui/home/map/GpsCheck.kt` | No — Activity |
| `rememberInterstitialAdLauncher()` | `ui/home/advertising/InterstitialAd.kt` | No — AdMob |
| `BannerAdvertView()` | `ui/home/advertising/AdvertisingViews.kt` | No — AdMob |
| `object AdIds` | `ui/home/advertising/AdIds.kt` | No — AdMob IDs |
| `ObserveCurrentLocation()` | `ui/home/weather/ObserveCurrentLocation.kt` | No — GPS |
| `openMapNavigation()` / `shareMarkerLocation()` | `ui/home/place/PlaceUtilFunctions.kt` | No — Intents |
| `MediaPickerLauncher` | `ui/utils/MediaPicker.kt` | No — Activity Results |
| `PlatformBackHandler()` | `ui/utils/PlatformBackHandler.kt` | No — Back button |
| `isDynamicColorSupported()` / `rememberAppVersion()` / `rememberOpenAppStore()` / `rememberBillingLauncher()` | `ui/utils/PlatformUtils.kt` | No — Platform APIs |
| `FishingNotesTheme()` / `resolveDynamicColorScheme()` | `ui/theme/Theme.kt` | No — Dynamic colors |
| `rememberConnectionState()` | `utils/network/NetworkMonitor.kt` | No — ConnectivityManager |
| `AppNavGraph()` | `ui/AppNavGraph.kt` | No — Routes reference androidMain screens |

## Completed Phases

| Phase | Description |
|-------|-------------|
| 1–12 | R.string/drawable, SnackbarManager, Placeholder, ConstraintLayout, moko-permissions |
| 13 | UI infrastructure + expect/actual patterns |
| 14 | Mass move ~40 UI files to commonMain |
| 15 | Fix pre-existing compilation errors |
| 16 | NewCatch flow, Catch screen, PhotoViews |
| 17 | SettingsScreen, Place screen, PlaceViews |
| 18 | App shell + StatisticsScreen |
| 19 | Notes, LoginScreen, LoginViewModel, Theme refactor |
| 20 | Dependency cleanup + R.string pruning |
| 21 | SnackbarManager fix + res/raw elimination |
| 22 | MainViewModel bug + drawable dedup + layout cleanup |
| 23 | Deprecation cleanup + dead resource removal |
| 24 | Code quality fixes |
| 25 | Paging + Firebase gitlive + Cedar logger migration |
