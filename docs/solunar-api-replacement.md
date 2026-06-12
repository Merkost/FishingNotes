# Solunar API Replacement Plan

Date checked: 2026-06-12

## Summary

`https://api.solunar.org` is no longer reliable enough for production. The recent crash came from a `ConnectTimeoutException` while the map place card was loading fish activity. The crash handling has already been hardened, but the app still depends on a fragile third-party Solunar endpoint for a user-visible feature.

The best replacement is to stop looking for another black-box "fish activity percent" API and instead:

1. Fetch astronomy data from a maintained provider.
2. Compute the fish activity score locally in shared KMP code.
3. Cache the per-place/per-day result so map interactions do not repeatedly spend API calls.

Recommended provider: **WeatherAPI.com Astronomy API**.

Fallback / secondary option: **IPGeolocation Astronomy API**.

Not a direct replacement: **Open-Meteo Marine API**. It is useful later for tide, wave, current, and sea-surface context, but it does not replace moonrise/moonset/phase-based Solunar calculations.

## Why Replace The Current Endpoint

Current implementation:

- `shared/src/commonMain/kotlin/com/mobileprism/fishing/model/datasource/SolunarRepositoryKtorImpl.kt`
- `DEFAULT_BASE_URL = "https://api.solunar.org"`
- Request shape: `/solunar/{latitude},{longitude},{yyyymmdd},{timezoneOffsetHours}`
- Domain interface: `SolunarRepository.getSolunar(latitude, longitude, date, timeZone): Result<Solunar>`
- Consumer: `GetFishActivityUseCase`, which reads `solunar.hourlyRating[hour]`
- UI surface: selected place card / marker info fish activity percent

Issues:

- The provider timed out on Android with `connect_timeout=10000 ms`.
- The endpoint is a single external dependency with no known SLA.
- The API returns an already-computed fish activity score, which makes the app dependent on provider availability and provider scoring rules.
- The current timezone argument is an integer hour offset. That loses half-hour and quarter-hour time zones, for example Australia/Adelaide or Asia/Kathmandu. A replacement should use an IANA timezone or provider-local times instead.

## Provider Comparison

| Provider | Direct Solunar score | Astronomy data | Free tier | Key required | Production fit | Notes |
|---|---:|---:|---:|---:|---|---|
| WeatherAPI.com Astronomy API | No | Yes | 100K calls/month | Yes | Best fit | Commercial use is listed in the pricing table, Astronomy API is available on Free, and response contains sunrise, sunset, moonrise, moonset, moon phase, and moon illumination. |
| IPGeolocation Astronomy API | No | Yes | 1K credits/day | Yes | Good fallback | Free plan includes standalone Astronomy API, but daily limit is much smaller. Response has richer moon position fields. |
| Open-Meteo Marine API | No | No moon phase/times | Free open-access for non-commercial use, 10K calls/day | No for free open-access | Supplement only | Good for waves, currents, sea surface temperature, and sea level. Free open-access is non-commercial and rate-limited, so an ad-supported Play Store app should use a paid plan if this becomes production-critical. |

## Recommended Option: WeatherAPI Astronomy

WeatherAPI.com is the cleanest replacement because it is simple, stable, and already provides the fields needed to calculate a Solunar-like rating locally.

Endpoint:

```text
GET https://api.weatherapi.com/v1/astronomy.json?key={WEATHERAPI_KEY}&q={latitude},{longitude}&dt={yyyy-MM-dd}
```

Useful response fields:

- `location.tz_id`
- `location.localtime`
- `astronomy.astro.sunrise`
- `astronomy.astro.sunset`
- `astronomy.astro.moonrise`
- `astronomy.astro.moonset`
- `astronomy.astro.moon_phase`
- `astronomy.astro.moon_illumination`
- `astronomy.astro.is_moon_up`
- `astronomy.astro.is_sun_up`

Why this is the recommended path:

- It provides the exact astronomy inputs the app needs.
- The free plan currently lists 100K calls/month.
- The pricing table currently marks Astronomy API as available on Free.
- The pricing table currently marks Commercial Use as available.
- The request model is simple enough for Ktor and KMP serialization.
- Local scoring means the app owns the behavior, tests, and future tuning.

Costs and limits:

- 100K calls/month means about 3.3K calls/day.
- One call per selected place per local day is enough if cached.
- A first public release should stay comfortably under this with per-place daily caching.
- If usage grows, cache aggressively before paying for more quota.

Attribution:

- WeatherAPI says free-plan users are appreciated to link back to the service.
- Add attribution in a low-friction place such as Settings > About or the privacy/data providers section.

## Fallback Option: IPGeolocation Astronomy

Endpoint:

```text
GET https://api.ipgeolocation.io/v3/astronomy?apiKey={IPGEOLOCATION_KEY}&lat={latitude}&long={longitude}&date={yyyy-MM-dd}
```

Useful response fields:

- `sunrise`
- `sunset`
- `solar_noon`
- `day_length`
- `sun_altitude`
- `sun_azimuth`
- `moon_phase`
- `moonrise`
- `moonset`
- `moon_altitude`
- `moon_distance`
- `moon_azimuth`
- `moon_illumination_percentage`
- `moon_angle`

Why it is a good fallback:

- It exposes richer moon position fields than WeatherAPI.
- The free plan currently includes standalone Astronomy API.
- The free plan currently provides 1K credits/day.

Why it is not the first recommendation:

- 1K credits/day is a tighter ceiling for map-heavy usage.
- The response model is richer than needed for the current UI.
- It is better as either a fallback provider or as the source for a more advanced future predictor.

## Supplement Only: Open-Meteo Marine

Endpoint example:

```text
GET https://marine-api.open-meteo.com/v1/marine?latitude={latitude}&longitude={longitude}&hourly=wave_height,wave_period,ocean_current_velocity,sea_surface_temperature,sea_level_height_msl&timezone=auto
```

Useful fields:

- `wave_height`
- `wave_direction`
- `wave_period`
- `wind_wave_height`
- `swell_wave_height`
- `ocean_current_velocity`
- `ocean_current_direction`
- `sea_surface_temperature`
- `sea_level_height_msl`

Why it is not a Solunar replacement:

- It does not provide moonrise, moonset, moon phase, or moon illumination.
- It cannot directly recreate the current fish activity percent.

Where it could help later:

- Coastal fishing context.
- Surf and safety conditions.
- Better fish activity scoring for saltwater spots.
- Future "best time to fish" recommendations that combine moon, weather, tide, wave, and user catch history.

Important licensing note:

- Open-Meteo free open-access API is currently for non-commercial use, has no uptime guarantee, and is rate-limited to 10K calls/day.
- The app contains ads, so treat Open-Meteo production use as commercial unless legal/product explicitly decides otherwise.
- A paid Open-Meteo plan includes a customer endpoint, API key, and commercial use license.

## Proposed Architecture

Keep the existing domain boundary at first:

```kotlin
interface SolunarRepository {
    suspend fun getSolunar(
        latitude: Double,
        longitude: Double,
        date: String,
        timeZone: Int,
    ): Result<Solunar>
}
```

Then replace the implementation behind it:

- Rename or replace `SolunarRepositoryKtorImpl`.
- Add `WeatherApiAstronomyRepositoryKtorImpl` or `WeatherApiSolunarRepositoryKtorImpl`.
- Fetch WeatherAPI astronomy data.
- Map provider response into the existing `Solunar` domain object.
- Calculate `HourlyRating` locally.
- Leave `GetFishActivityUseCase` mostly unchanged for the first pass.

This keeps the UI and view model behavior stable while removing the dead endpoint.

Recommended later cleanup:

- Replace the Solunar-shaped domain model with a clearer app-owned model:

```kotlin
data class FishActivityForecast(
    val date: LocalDate,
    val currentHourRating: Int,
    val dayRating: Int,
    val hourlyRatings: List<Int>,
    val majorPeriods: List<ActivityWindow>,
    val minorPeriods: List<ActivityWindow>,
    val moonPhase: String,
    val moonIlluminationPercent: Double,
    val source: FishActivitySource,
)
```

That cleanup can wait. The first production fix should minimize blast radius.

## BuildKonfig / Secrets

Add a dedicated key instead of reusing existing weather keys:

- `WEATHERAPI_KEY`

Files to update:

- `shared/build.gradle.kts`
- local `local.properties` or `secrets.properties`
- CI secrets, if CI builds release variants that require the key

Do not commit the real key.

Example BuildKonfig field:

```kotlin
buildConfigField(FieldSpec.Type.STRING, "WEATHERAPI_KEY", resolveProperty("WEATHERAPI_KEY"))
```

DI update:

```kotlin
single<SolunarRepository> {
    WeatherApiSolunarRepositoryKtorImpl(
        analyticsTracker = get(),
        weatherApiKey = BuildKonfig.WEATHERAPI_KEY,
        httpClient = get(),
    )
}
```

## Local Fish Activity Algorithm

The current UI only needs one integer percent for the selected hour, but the existing domain model stores 24 hourly scores plus major/minor periods. The local algorithm should compute the whole day so the app can reuse it later.

Inputs:

- Local date.
- Local timezone from provider response, preferably `location.tz_id`.
- Sunrise and sunset.
- Moonrise and moonset.
- Moon phase.
- Moon illumination percent.

Core assumptions:

- Minor periods happen around moonrise and moonset.
- Major periods happen around moon transit and moon underfoot.
- If the provider does not return moon transit, approximate it from moonrise/moonset.
- Dawn and dusk are useful secondary boosts.
- Full moon and new moon receive stronger phase boosts than quarter phases.

Window calculation:

```text
minor1 = moonrise +/- 60 minutes
minor2 = moonset +/- 60 minutes
major1 = midpoint(moonrise, moonset) +/- 90 minutes
major2 = major1 + 12 hours, wrapped into the same local day +/- 90 minutes
dawn = sunrise +/- 45 minutes
dusk = sunset +/- 45 minutes
```

If moonrise or moonset is missing:

- Use the available one for one minor window.
- Skip midpoint-based major windows if both are not available.
- Still compute a phase/dawn/dusk based daily rating instead of failing.

Hourly score:

```text
score = 15
score += bestMajorWindowWeight(hour) * 50
score += bestMinorWindowWeight(hour) * 35
score += bestSunWindowWeight(hour) * 10
score += moonPhaseBoost
score = score.coerceIn(0, 100)
```

Weighting should be triangular:

```text
weight = 1.0 at window center
weight = 0.0 at window edge
```

Example boost table:

| Moon phase | Boost |
|---|---:|
| New Moon | 15 |
| Full Moon | 15 |
| Waxing Gibbous | 10 |
| Waning Gibbous | 10 |
| First Quarter | 6 |
| Last Quarter | 6 |
| Waxing Crescent | 4 |
| Waning Crescent | 4 |
| Unknown | 0 |

Day rating:

```text
dayRating = average(top 4 hourly ratings)
```

That makes the daily value represent the best fishable windows instead of being flattened by low overnight/off-window hours.

## Mapping To Existing Solunar Model

Existing fields can be populated like this:

| Existing field | Replacement source |
|---|---|
| `dayRating` | Local calculator |
| `hourlyRating` | Local calculator |
| `major1Start`, `major1Stop`, `major2Start`, `major2Stop` | Local calculated major windows |
| `minor1Start`, `minor1Stop`, `minor2Start`, `minor2Stop` | Moonrise/moonset windows |
| `moonIllumination` | WeatherAPI `moon_illumination` |
| `moonPhase` | WeatherAPI `moon_phase` |
| `moonRise`, `moonSet` | WeatherAPI `moonrise`, `moonset` |
| `moonTransit` | Approximate midpoint between moonrise and moonset |
| `moonUnder` | `moonTransit + 12 hours` |
| `sunRise`, `sunSet` | WeatherAPI `sunrise`, `sunset` |
| `sunTransit` | Midpoint between sunrise and sunset |

Decimal fields:

- Convert local time to decimal hours.
- Example: `06:30` -> `6.5`.
- Missing value -> `0.0` only if the field must remain non-null. Prefer a future domain cleanup to use nullable fields.

String fields:

- Preserve a display-safe local `HH:mm` format.
- Do not leak provider-specific values like `No moonrise` into UI fields without normalizing.

## Caching

Add caching before or during the provider replacement. Without caching, every map card refresh can spend another API call.

Cache key:

```text
provider = weatherapi
date = yyyy-MM-dd
lat = latitude rounded to 3 decimals
lon = longitude rounded to 3 decimals
```

Rounding to 3 decimals is roughly 100 meters. That is enough for fish activity and prevents duplicate calls for tiny marker coordinate differences.

TTL:

- Cache until the next local midnight for that place/timezone.
- Keep stale cache for a short grace period, for example 3 days, and use it only if the network fails.

Storage options:

- Short term: in-memory cache in repository.
- Better: SQLDelight/Room table similar to weather cache if this feature is frequently used.

Recommended cache behavior:

1. Return fresh cache if available.
2. Fetch remote if cache is missing or expired.
3. Save successful response/calculated forecast.
4. If remote fails and stale cache exists, return stale cache.
5. If remote fails and no cache exists, return `Result.failure`.

The view model already needs to tolerate failure and show a dash instead of crashing.

## Error Handling

Provider failures must never crash the app.

Keep these rules:

- Repository returns `Result.failure` for network and parsing failures.
- `safeApiCall` should keep rethrowing `CancellationException`.
- `MapViewModel` should catch non-cancellation failures and stop loading.
- UI shows a dash or neutral state when fish activity is unavailable.
- Log analytics/performance events so outage rate is visible.

Suggested analytics events:

- `get_solunar`
- `get_solunar_success`
- `get_solunar_failure`
- `get_solunar_cache_hit`
- `get_solunar_stale_cache_hit`

Suggested failure dimensions:

- provider
- error category: timeout, no_network, http_4xx, http_5xx, parse, missing_key, unknown
- cache state: none, fresh, stale

## UI Behavior

Current card behavior should remain simple:

- Loading: shimmer or placeholder.
- Success: `{rating}%`.
- Failure with no cache: `--`.
- Failure with stale cache: show stale value and optionally a subtle stale indicator later.

Do not show a disruptive snackbar for fish activity failures. The user did not explicitly ask for this data; it is supporting context on the place card.

Future improvement:

- Tapping fish activity can open a small bottom sheet with best windows:
  - Best: `06:30-08:00`
  - Good: `18:10-19:30`
  - Moon: `Full Moon, 98%`
  - Source: `Calculated from astronomy data`

## Privacy And Play Store Updates

Replacing `api.solunar.org` with WeatherAPI or IPGeolocation means precise coordinates are still shared with a third-party data provider.

Update:

- `docs/privacy.html`
- `docs/play-store-release.md`
- Play Console Data Safety notes, if already submitted

Text direction:

```text
We share the coordinates of a selected fishing spot with weather and astronomy data providers to show weather and fish-activity estimates for that spot.
```

Provider list:

- Google Maps
- OpenWeatherMap
- WeatherAPI.com, if selected
- IPGeolocation, if used as fallback
- Open-Meteo, only if marine data is added

## Implementation Steps

1. Add `WEATHERAPI_KEY` to `BuildKonfig`.
2. Add WeatherAPI astronomy DTOs under `model/datasource`.
3. Add a fish activity calculator in shared common code.
4. Add a mapper from WeatherAPI astronomy response to `Solunar`.
5. Replace the DI binding for `SolunarRepository`.
6. Add in-memory or persistent daily cache.
7. Keep the current crash-safe view model behavior.
8. Update privacy/release docs with the selected provider.
9. Add tests for parser, calculator, repository failure, cache behavior, and use case.
10. Push to the PR and let CI rerun.

## Test Plan

Unit tests:

- Calculator returns 24 scores in `0..100`.
- Major window center scores higher than window edge.
- Minor window center scores higher than off-window hours.
- Full/New Moon scores higher than quarter phases when windows are equal.
- Missing moonrise/moonset does not throw.
- Half-hour timezone locations do not drift because provider-local time is used.
- Mapping normalizes provider strings such as missing moonrise.
- Repository returns `Result.failure` for non-2xx responses.
- Repository returns `Result.failure` for malformed JSON.
- Repository rethrows cancellation through `safeApiCall`.
- Cache returns fresh values without remote call.
- Cache returns stale value only when remote fails.

Integration checks:

```text
./gradlew :shared:testDebugUnitTest --no-daemon
./gradlew :shared:detekt --no-daemon
./gradlew :shared:compileDebugKotlinAndroid --no-daemon
./gradlew :shared:compileKotlinIosSimulatorArm64 --no-daemon
```

Manual checks:

- Open a saved place with network enabled. Fish activity appears.
- Open the same place again. It should use cache or avoid duplicate loading flicker.
- Disable network. Existing cached value remains available if fresh/stale policy allows it.
- Disable network with no cache. UI shows `--`, not a snackbar and not a crash.
- Try a location in a half-hour timezone. Times and current-hour score are based on provider-local date/time.

## Future Scoring Improvements

After the first replacement is stable, improve the fish activity model with:

- Weather pressure trend from existing weather provider.
- Wind speed and gust penalty.
- Temperature comfort range per species.
- Tide and current context for coastal spots.
- User catch history by species, spot, date, hour, weather, and moon phase.
- A transparent "confidence" value separate from the activity percent.

Do not mix all of this into the first provider replacement. First remove the broken dependency, keep UI behavior stable, and prove the calculator is deterministic.

## Decision

Use **WeatherAPI.com Astronomy API** as the primary replacement and compute fish activity locally.

Use **IPGeolocation Astronomy API** only if WeatherAPI becomes unavailable or if the richer moon-position data is needed.

Use **Open-Meteo Marine API** later for marine conditions, not as the Solunar replacement.

## Sources

- WeatherAPI docs: https://www.weatherapi.com/docs/
- WeatherAPI pricing: https://www.weatherapi.com/pricing.aspx
- IPGeolocation Astronomy API docs: https://ipgeolocation.io/astronomy-api.html
- IPGeolocation pricing: https://ipgeolocation.io/pricing.html
- Open-Meteo Marine API docs: https://open-meteo.com/en/docs/marine-weather-api
- Open-Meteo pricing: https://open-meteo.com/en/pricing
