# FishingNotes — Whole-App UI/UX Modern Refresh (Design Spec)

Date: 2026-06-12
Branch: `compose_migration`
Status: Approved direction, pending spec review

## 1. Goal

Dramatically improve the UI/UX across the entire app via a **design-system-first** refresh:
establish shared design tokens and a consolidated reusable component library, then roll them
across every screen. The visual ambition is a **"modern refresh"** — keep the Material3 foundation
and the Blue/Green brand identity, but meaningfully elevate hierarchy, spacing rhythm,
states, motion, and consistency.

### Guiding design principle (north star)
**Reuse Material3 components as the base, but style them with personality so the app looks modern
and intentional — not stock Material.** Concretely: M3 primitives underneath (free accessibility,
touch targets, theming), custom-styled on top (branded active indicators, gradient accents, refined
shape/elevation, tasteful motion, considered empty/loading/error states). Avoid both extremes:
neither flat default Material nor hand-rolled framework internals.

## 2. Constraints

- **KMP/iOS-safe (hard constraint).** All shared UI stays in `commonMain`; no Android-only APIs
  leak into shared code. New tokens are pure `Dp`/`Color`/`TextStyle`/spec objects.
- **Keep it buildable.** Every phase compiles; no half-finished refactors left in the tree.
  Verification = build `:androidApp:installDebug` and screenshot on the running emulators at each checkpoint.
- **Brand identity preserved.** Blue/Green schemes + Nunito stay; palette is cleaned, not reinvented.
- **Behavior changes are allowed and in-scope** where entangled with the UI work: fix crash guards,
  silent error states, dead affordances, and string-concatenation localization. Larger flow/logic
  changes are out of scope unless explicitly raised.

## 3. Verification approach

The user runs the app for me. Two emulators are live (`emulator-5554`, `emulator-5556`).
At each screen-group checkpoint:
1. `./gradlew :androidApp:installDebug` (installed id `com.merkost.fishingnotes.debug`).
2. Launch + screenshot the affected screens (light + dark).
3. Review against the findings; iterate before moving to the next group.

Also: keep an iOS-safety check by ensuring no Android imports enter changed `commonMain` files
(the shared module already compiles for iOS).

## 4. Layer A — Foundation tokens (`commonMain`, zero behavior change)

New/updated files under `shared/src/commonMain/kotlin/com/mobileprism/fishing/ui/theme/`.

### 4.1 Spacing — `Spacing.kt` (new)
Pure `Dp` object on a 4dp grid:
`none=0, xxs=2, xs=4, sm=8, md=12, lg=16, xl=24, xxl=32, xxxl=48`
plus semantic aliases: `screenH=16, sectionGap=24, listItemGap=8, cardPadding=16, fabClearance=88`.
Migration: replace dominant literals (8/16/4/12/24/20 dp ≈ 520 of 801), snap off-grid
values (2/6/14/18/22/42) onto the grid. Use `Arrangement.spacedBy(...)` instead of interleaved
`Spacer`s where it tidies columns. (A `LocalSpacing` CompositionLocal is optional; a plain object is enough.)

### 4.2 Elevation — `Elevation.kt` (new)
M3 tonal ladder: `level0=0, level1=1, level2=3, level3=6, level4=8, level5=12` dp +
semantic: `card=level1, raisedCard=level2, dialog=level3, bottomSheet=level4, fab=level3`.
Prefer `tonalElevation` on M3 surfaces; reserve `shadowElevation` for true shadows.

### 4.3 Shapes — `Shape.kt` (update)
Add `extraSmall=2dp`. Canonical mapping: chips/fields → `small(8)`, cards → `medium(12)`,
sheets/large surfaces → `large(16)`, dialogs → `extraLarge(24/28)`. Migrate the 25 ad-hoc
`RoundedCornerShape(N.dp)` call sites to `MaterialTheme.shapes.*`. Move the non-symmetric
bottom-sheet shape out of `Constants.kt` into a small `ShapeTokens` helper.

### 4.4 Motion — `Motion.kt` (new)
Named `AnimationSpec`s + durations/easings: `Motion.short=150, medium=250, long=400` ms;
`enterContent` (fade + slide-up), `screenEnter/screenExit` nav presets, `navIndicator` spring.
Apply an expressive motion scheme on **both** Android and iOS for parity.

### 4.5 Emphasis/alpha — `Emphasis.kt` (new)
`disabled=0.38f, medium=0.60f, hint=0.74f, divider=0.12f, pressedOverlay=0.08f, scrim=0.32f`.
Replace ~25 scattered hardcoded alphas.

### 4.6 Color cleanup — `Color.kt` / `CustomColors.kt` (update)
- Make the raw palette constants `private` (they only feed the four `ColorScheme` builders).
- Migrate the ~52 legacy-val usages + 145 hardcoded `Color(0x…)` literals to M3 roles:
  `secondaryTextColor → onSurfaceVariant`, `primaryWhiteColor → onPrimary/surface`,
  `primaryFigmaColor/primaryBlueColor → primary`, `cardColor/surfaceGreenColor → surfaceContainer*`,
  hardcoded greens → `primary/tertiary`.
- Add explicit `error/onError/errorContainer/onErrorContainer` per scheme.
- Define `BrandGradients` (theme-driven primary gradient) replacing the 3 hand-rolled hex gradients.
- Delete dead `backgroundSecondaryColor`; keep `secondaryTextColor`/`secondaryIconColor` only if they
  intentionally differ from `onSurfaceVariant` (otherwise fold in).

### 4.7 Typography — `Type.kt` (update) + real font assets
- **Fetch the real Nunito Regular/Medium/SemiBold/Bold files** (Google Fonts, OFL) into
  `shared/src/commonMain/composeResources/font/`, map each `FontWeight` to its own resource
  (removes synthetic faux-bold).
- Add per-style `lineHeight` + `letterSpacing` tuned for Nunito; fill missing `displayLarge/Medium`,
  `headlineLarge/Medium` tiers for a coherent scale.

## 5. Layer B — Reusable component library

Tokens-driven, M3-based, styled-with-personality. New components live in
`ui/home/views/` (existing component home) and `ui/components/` (specialized inputs); legacy
duplicates are deleted as callers migrate. APIs below are the contract.

### 5.1 Core primitives
- **`AppButton(text, onClick, modifier, style = Filled, enabled = true, loading = false, leadingIcon = null)`**
  — `AppButtonStyle { Filled, Tonal, Outlined, Text }` delegating to the right M3 button; ≥48dp;
  built-in loading spinner; **no forced uppercase**. Replaces `DefaultButton/Filled/Outlined/SecondaryLight/LoadingIconButtonOutlined`
  and the login/onboarding one-off CTAs.
- **`AppIconButton(onClick, icon, contentDescription, modifier, ...)`** — the one icon-only button;
  enforces 48dp min hit target; **required** non-null `contentDescription`.
- **`AppText(text, style = Body, color = LocalContentColor, ...)`** — `AppTextStyle` enum mapped to
  M3 roles (Display/Heading/Title/Subtitle/Body/BodySmall/Caption/Support). Collapses the 14
  text composables; `TextWithLeadingIcon` replaces `SubtitleWithIcon`.
- **`AppCard(modifier, onClick = null, shape = shapes.medium, elevation = Elevation.card, contentPadding = Spacing.cardPadding, content)`**
  — single card; replaces all 5 `Cards.kt` variants and their dead params.
- **`SectionCard(icon, title, modifier, onClick = null, trailing = null, content)`** — card + icon-header
  + content; optional edit affordance when `onClick != null`; used by catch/place/weather detail sections.
- **`FormTextField(value, onValueChange, label, leadingIcon = null, readOnly = false, isError = false, supportingText = null, ...)`**
  — one wrapper over `OutlinedTextField`; replaces hand-rolled fields in ~9 files.
- **`PickerField(value, label, leadingIcon, placeholder, onClick)`** — read-only "tap to open a picker"
  field; removes the disabled-colors hack.

### 5.2 State + list system
- **`EmptyState(illustration, title, description = null, action = null, modifier)`** + presets
  (`NoPhotos`, `NoPlaces`, `NoCatches`). Replaces `NoContentView` + the per-screen `Spacer + CTA` wrappers.
- **`ErrorState(message, illustration, onRetry = null, modifier)`** — owns the retry button;
  `NoInternetView` becomes a preset. Removes the `padding(top=128.dp)` hacks (true viewport centering).
- **`LoadingState(modifier)`**, **`InlineLoader`**, **`ListAppendLoader`** + keep `ModalLoadingDialog`
  for blocking writes only.
- **`ScreenStateContent<T>(state, loading, error, empty, content)`** — Crossfade between
  Loading/Error/Empty/Content for `BaseViewState`-driven screens.
- **`PagedListScaffold<T>(items, isRefreshing, onRefresh, skeleton, emptyState, appendFooter, itemContent, groupingKey = null)`**
  — eliminates the duplicated Catches/Places paging machinery; handles refresh, append-error retry,
  end-of-list, FAB bottom inset, `animateItem()`, and skeleton→content crossfade.
- **`SkeletonBox(width, height, shape)` / `SkeletonLine`** + component-level skeletons
  (`CardSkeleton`, `ListItemSkeleton`, `StatTile.Skeleton`, `WeatherSkeleton`) built on the existing
  KMP-safe `Modifier.placeholder()`.

### 5.3 Navigation, bars, chrome
- **`AppBottomNavigation(items, currentRoute, onSelect)`** — M3 `NavigationBar`/`NavigationBarItem`
  base with a **custom-styled active indicator** (filled brand container + subtle motion) so it keeps
  the app's personality; 48dp targets, `role = Tab` semantics, no `.uppercase()`. (Resolves the bespoke
  240-line Jetsnack port.)
- **`AppTopBar(title, subtitle = null, navigationIcon = null, actions = {}, scrollBehavior = null)`** —
  **surface-colored by default app-wide** (per decision); fixes the unused `elevation` param; one
  convention across Map/Weather/Notes/Profile/Settings/About. Supports a `LargeTopAppBar` variant.
- **`AppTabRow` / `TabbedPager(tabs, pagerState, content)`** — surface-based tab strip with real
  selected/unselected color states, synchronized indicator, shared-axis/crossfade tab transition;
  used by Notes and Place detail.
- **`AppScaffold`** — owns bottom bar slot + snackbar host + sync-status banner overlay + inset policy
  (so the sync banner overlays instead of reflowing every screen).
- **`BottomActionBar(primaryText, onClick, loading = false)`** — Save/Confirm bar with
  `navigationBarsPadding` + tuned slide/fade; used by NewCatch/EditProfile/place flows.
- **`EditBottomSheetScaffold(title, onCancel, onSave, saveEnabled, content)`** — standard edit-sheet
  shell (title + content + Cancel/Save row); dedupes the 4 catch edit sheets.
- **`SortOptionsSheet<T : StringOperation>(title, options, current, onSelect)`** — collapses the
  identical Places/Catches sort sheets.
- **`SettingsSelectionDialog<T : StringOperation>(title, options, current, onSelect)`** — one generic
  single-choice dialog replacing the 3 unit dialogs + dark-mode dialog.

### 5.4 Brand + content components
- **`BrandGradientCard` / `BrandGradientBackground`** (theme-driven) — unify the 3 gradient surfaces.
- **`BrandFab(onClick, onLongClick = null, content)`** — one branded FAB; single `FabSize` source.
- **`FloatingControlSurface` + `FloatingIconButton(size, iconSize, contentDescription, onClick)`** —
  map "glass" control pill + icon button (48dp targets, required description).
- **`IconStatChip(icon, label)` / `CountBadge(count)` / `StatusLabel(variant)`** — stat pills, count
  badges (hide-on-zero, accessible tint), and semantic status labels (Auto/Done/Offline/Loading).
- **`InlineBannerCard(tone, icon, title, body, actionLabel = null, onClick = null)`** —
  `BannerTone { Info, Warning, Error }`; for location-permission, weather stale-data, sync/offline.
- **`AvatarWithBadge(image, onEdit = null, contentDescription)`** — ≥48dp badge; no dead no-op;
  shared by Profile/EditProfile.
- **`StatTile(icon, title, value)` + `StatRow`** — dashboard metric tile (with skeleton) for
  Profile/Statistics/Map/detail summaries.
- **`ChartCard(title, data, formatLabel)`** — `DefaultCard` + titled header + **brand-tinted Vico**
  chart with consistent axis styling, value labels, and a responsive width; replaces the 5 inline
  stats charts and the 2 hand-rolled Canvas weather charts (as `WeatherTrendChart`).
- **Weather set:** `WeatherMetric(label, icon, value, iconTint)` + `WeatherStatGrid`,
  `WeatherDailyForecastRow` (M3 `ListItem`, ≥48dp), `LocationPickerChip` (M3 chip w/ dropdown a11y).
- **`AppHeroHeader` + `VersionLabel` + `LabeledIconButton`** — About screen hero/actions.

### 5.5 Shared formatters/util (`commonMain`)
- `MeasurementFormatter` (weight/amount, locale-correct, no trailing `.0`), localized month/date
  labels (kotlinx-datetime), `UnitFormatter` (value+unit), `errorToMessage` mapper (friendly auth/network
  strings). Replace `+`-concatenated/`replace("%s")` strings with **parameterized string resources**.
- `SlideUpFadeIn` entrance modifier + `AppNavTransitions` presets.

## 6. Layer C — Cross-cutting fixes (applied during rollout)

- **Touch targets:** every interactive control ≥48dp (baked into `AppIconButton`/`FloatingIconButton`/
  bottom nav/avatar badge/chips).
- **Accessibility:** replace `contentDescription = Icon.name` developer strings with localized
  resources; mark decorative icons `null`; merge stat/label semantics.
- **Casing:** remove all forced `.uppercase()` (bottom nav, buttons); rely on typography for emphasis.
- **Bugs/i18n (in-scope):** guard Weather `forecast.hourly.first()`/daily `.first()` against empty;
  handle paging `append` error + end-of-list; wire or remove the EditProfile photo-edit no-op; wrap
  `setSortOrder(...)` in `LaunchedEffect`; localize Statistics month labels; fix About version
  concatenation/spacing; remove dead code (`LottieStars`, `FishAmountAndWeightView` if unused, commented
  Place buttons, Notes `Filter` branch).

## 7. Phased rollout (build + screenshot checkpoint after each)

- **Phase A — Foundations:** Spacing, Elevation, Shapes, Motion, Emphasis, Color cleanup, Type +
  Nunito weights. No visual regressions; mechanical token migration where touched.
- **Phase B — Core component library:** build the components in §5 (with the legacy components still
  present), so screens can adopt incrementally.
- **Phase C1 — First-run & shell:** Login, Onboarding, Home scaffold + `AppBottomNavigation`,
  `AppScaffold`, snackbar/sync banner.
- **Phase C2 — Notes:** container/tabs (`AppTabRow`), Catches + Places via `PagedListScaffold`,
  Statistics via `StatTile`/`ChartCard`.
- **Phase C3 — Weather:** Weather + Daily (metrics grid, `WeatherTrendChart`, section cards, list rows,
  location chip, stale-data banner, crash guards).
- **Phase C4 — New Catch:** `PlaceField`, `StatusFilterChip`, `SectionHeader`, single CTA via
  `BottomActionBar`, motion, stepper consolidation.
- **Phase C5 — Detail:** Place detail + Catch detail (`SectionCard`, `EditBottomSheetScaffold`,
  `TabbedPager`, `DetailHeader`, empty/loading states).
- **Phase C6 — Profile:** Profile + Edit Profile (`AvatarWithBadge`, `StatTile`, `FormTextField`,
  real states, photo-edit fix).
- **Phase C7 — Settings & About:** `SettingsSelectionDialog`, `SelectableColorSwatch`,
  `InlineBannerCard`, `AppTopBar` unification, About hero rebuild.
- **Phase D — Sweep:** delete remaining dead/legacy components, final consistency + a11y pass,
  full build (Android + ensure iOS source set unaffected).

## 8. Out of scope

- New features or navigation-flow redesigns (filtering, new screens).
- Backend/data model changes.
- A full iOS visual pass (we keep iOS *building*; visual verification is Android this round).
- Reinventing the color palette or switching design language away from M3.

## 9. Success criteria

- Shared token objects exist and the dominant `.dp`/color/alpha literals are migrated.
- The ~26 duplicate composables are consolidated to the §5 set; Catches/Places no longer duplicate.
- Every screen uses consistent spacing rhythm, surface top bars, unified empty/loading/error states,
  ≥48dp targets, localized content descriptions, and tasteful motion.
- App builds and installs; screenshots reviewed and approved per phase; iOS source set still compiles.
