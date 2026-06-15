# FishingNotes — UI/UX Improvement Roadmap

> Source: multi-agent audit (Notes, Weather, Statistics, Design System, Accessibility) + synthesis, 2026-06-15. Shell/Account, Create/Detail, Motion, and Web-research audits were rate-limited in the first pass and are being completed in a follow-up run.

## 1. Executive Summary

The app has a solid, recently-modernized foundation (FishingTheme tokens, bespoke bottom bar, white elevated cards), but three structural gaps hold it back from best-in-class. First, **it presents data, not insight**: the catch list shows a photo-count icon instead of the photos it already loads (`NotesViews.kt:155,250`), Statistics computes rich fields (`averageWeight`, `mostCaughtSpecies`, `weightBySpecies`) never rendered (`StatisticsScreen.kt:96-196`), and Weather reads like a generic widget with zero angling interpretation despite having pressure-trend, wind, moon, sunrise data in-model. Second, **brand identity is barely expressed**: the blue/green gradient reaches ~4 surfaces while the three most-viewed heroes (Notes, Weather, Profile) are flat fills, and token discipline is half-finished (Notes uses 65 raw `.dp`; the date pill still paints the deprecated muddy-green `cardColor`). Third, **the app is largely invisible to TalkBack and missing journal table-stakes** — no search, no filter, no swipe actions, the bespoke bottom bar exposes no `Role.Tab`, the FAB is unlabeled, and all charts expose no data semantics. Highest leverage: turn the catch list into a photo journal, add a transparent "bite forecast" to weather, and surface the insights Statistics already calculates.

## 2. Quick Wins (high impact / S–M)

| Improvement | Why | Files | Effort |
|---|---|---|---|
| Catch card photo thumbnail | Flat text list → scannable photo journal; URLs (`downloadPhotoLinks`) + Coil3 already wired | `NotesViews.kt:155,250-256` (pattern: `PhotoViews.kt:99`) | M |
| Pressure-trend indicator on weather hero | The #1 angling signal; data in `Hourly.pressure`, absent today | `WeatherScreen.kt` hero, new `WeatherUtils.pressureTrend()` | M |
| Fix flat pressure chart Y-range | Variation invisible (Vico anchors near 0); `getBounds()` already computes min/max but is dead | `WeatherTrendChart.kt:54-68`, `WeatherUtils.kt:57-65` | S |
| Add Wind to hero metric grid | Critical for casting; `Hourly.windSpeed/windDeg` exist | `WeatherScreen.kt:293-318` | S |
| Statistics insight banner | `averageWeight`/`mostCaughtSpecies`/`weightBySpecies` computed but unshown | `StatisticsScreen.kt:96-196` | S |
| Brand gradient on weather hero | Most prominent surface is flat `Surface(primary)`; `BrandGradients.primaryDiagonal` exists | `WeatherScreen.kt:266-272` | S |
| Wire empty-state CTA buttons | `onAddCatch`/`onAddPlace` defined but passed `null` → dead empty screen | `UserCatchesScreen.kt:38,57`, `UserPlacesScreen.kt:39,55` | S |
| Label FAB + `Role.Tab` on bottom bar | Two key surfaces opaque to TalkBack | `FloatingActionButtons.kt`, `BrandFab.kt`, `Home.kt` | S |
| Fix muddy-green date pill | Off-brand deprecated token in a live screen | `NotesViews.kt:121-128`, `Color.kt:28-36` | S |
| Daily forecast high/low | Rows discard `Temperature.min/max` | `WeatherScreen.kt:241-251` | S |
| Named moon phase | `'50%'` → "Full moon"/"Waxing gibbous" | `WeatherViews.kt:298-329` | S |

## 3. Themed recommendations
(See git history / audit JSON for full detail.)

- **Lists & content:** photo thumbnails + corner `+N` badge; weight as a bold right-aligned tabular metric; search (per-tab query StateFlow); filter chips (species/has-photo/season); `SwipeToDismissBox` delete(undo)/edit in `PagedListScaffold`; human date buckets ("Today"/"This week") with per-section roll-ups ("3 catches · 5.4 kg"); remove dead `childModifier` plumbing; tokenize raw `.dp`.
- **Weather:** **Bite forecast** card (transparent weighted heuristic over pressure trend, wind, cloud, precip, moon, dawn/dusk — pure commonMain); best-times chips; pressure-trend headline; wind metric; named moon; high/low; surface solunar strip on main screen; interactive Vico markers + axis units; `heightIn(min=)` hero for font scaling; back-arrow = up (not navigate-to-map); 1-decimal wind.
- **Statistics:** render hidden insights (best month, avg weight, most-caught species); chart variety (line/area for trend, donut/h-bar for species); chart skeletons for loading parity.
- **Design system & typography:** gradient/accent variant on `AppCard` + `BrandStatTile`; dark-tuned gradient stops; migrate Notes/Weather off raw `.dp`; add `Spacing.xMd=20`, `IconSize` rungs; add semantic success/info/warning roles to `CustomColors`; add ExtraBold(800) display weight + a `Metric`/numeric text style + finish `AppTextStyle` (8/15 mapped); unify chips into one `AppChip`; route the 61 inline tween/spring through `Motion`; tokenize raw `shadowElevation`.
- **Motion & delight:** crossfade thumbnails; press-scale token on cards; count-up bite score; time-of-day adaptive hero gradient.
- **Accessibility:** bottom-bar `Role.Tab`/`stateDescription`/`selectableGroup`; FAB labels + `Role.Button`; `chartSemantics()` modifier; merge list-card descendants; `toggleable(role=Switch)` for settings rows; raise sub-48dp targets via `AppIconButton`; replace M2 `secondaryTextColor` on captions with `onSurfaceVariant`; a11y helper toolkit + CI grep for empty contentDescription.
- **First-run & retention:** wire empty-state CTAs; pre-fill weather into a new catch from the active forecast; empty-Catches prompt pointing at the FAB.

## 4. Phased plan

**P0 (now):** catch photo thumbnails + weight hierarchy; weather pressure-trend + wind + fixed chart Y-range + gradient hero + high/low + named moon; Statistics insight banner; wire empty-state CTAs; fix date pill; remove `childModifier`; a11y bottom-bar/FAB/chart semantics.

**P1 (next):** bite forecast + best-times + solunar strip; search + filter chips + insight→filtered deep-link; swipe actions; human date buckets; chart variety + interactivity; sort active state; tokenize Notes/Weather + `Metric` text style.

**P2 (later):** semantic color roles + dark gradients; unify `AppChip`; consolidate `Motion`; elevation/dark tonal strategy; multi-select; chart scrub markers; adaptive hero gradient; a11y toolkit + CI lint + font-scale 2× pass.

## 5. Signature moments
1. **Photo journal feed** — every catch leads with the angler's own photo + bold tabular weight + tinted fish-silhouette fallback.
2. **Bite Forecast** — transparent, factor-explained angling score on the weather screen.
3. **Pressure-trend headline** — rising/falling arrow + delta ("−3 mmHg / 3h, Falling") given top billing.
4. **Living gradient hero** — brand gradient adapting to conditions & time of day.
5. **Insight-to-action loop** — tap "Most caught: Pike" → Catches filtered to pike; per-section roll-ups surface patterns while browsing.
