# Plan 13 — Legacy Wrapper Consolidation (migration tail)

**Goal:** Achieve full design-system adoption by migrating every remaining call site off the legacy text/button/card wrappers onto the shared `AppText`/`AppButton`/`AppIconButton`/`AppCard` primitives, then deleting the now-dead wrappers. This eliminates the parallel-component duplication the Plan 11 grep gates surfaced (incl. the all-caps `.uppercase()` button styling).

**Constraints (inherit from the whole migration):**
- KMP/iOS-safe: all edits stay in `commonMain`; no `android.*`/`java.*`/`com.google.android.gms` imports. `:shared:compileKotlinIosSimulatorArm64` must pass.
- **NEVER add code comments** (project CLAUDE.md). Remove existing stale comments in code you rewrite only if they are obviously dead; do not add new ones.
- Match surrounding style; keep imports tidy (remove now-unused legacy imports).

---

## Authoritative mapping table

All new components live in `com.mobileprism.fishing.ui.home.views` except `EmptyState*` (`com.mobileprism.fishing.ui.components.state`).

`AppText(text, modifier, style: AppTextStyle, color, textAlign, maxLines, overflow)` — `AppTextStyle { Display, Heading, Title, Subtitle, Body, BodySmall, Caption, Support }` mapping to `displaySmall/headlineSmall/titleLarge/titleMedium/bodyLarge/bodyMedium/bodySmall/labelMedium`. Default `color = LocalContentColor.current`, `textAlign = null` (Start), `overflow = Ellipsis`.

### Text wrappers → AppText
| Legacy | New | Notes |
|---|---|---|
| `BigText(text, textAlign?, textColor?)` | `AppText(text, style = AppTextStyle.Display, color = textColor, textAlign = textAlign)` | displaySmall. If caller omits textColor, omit color (defaults differ negligibly — `onSurface` ≈ default content). |
| `SubtitleText(text, textColor?, maxLines?, textAlign?)` | `AppText(text, style = AppTextStyle.Subtitle, color = onSurfaceVariant, maxLines, textAlign)` | titleMedium. Default color was `customColors.secondaryTextColor` → use `MaterialTheme.colorScheme.onSurfaceVariant`. |
| `PrimaryText(text, textAlign?, textColor?, maxLines?)` | `AppText(text, style = AppTextStyle.Title, color = textColor, textAlign, maxLines)` | titleLarge. No caller passes `fontWeight`, so it is dropped. If caller omits textColor, omit color. |
| `PrimaryTextSmall(text, textAlign?, maxLines?, textColor?)` | `AppText(text, style = AppTextStyle.BodySmall, color = textColor, textAlign, maxLines)` | bodyMedium. |
| `SecondaryText(text, maxLines?, textAlign?, textColor?)` | `AppText(text, style = AppTextStyle.Body, color = onSurfaceVariant, textAlign = TextAlign.Center, maxLines)` | bodyLarge. **Default textAlign is Center — preserve it: pass `TextAlign.Center` unless the caller passed a different align.** Default color → `onSurfaceVariant`. |
| `SecondaryTextSmall(text, maxLines?, textAlign?, textColor?)` | `AppText(text, style = AppTextStyle.BodySmall, color = onSurfaceVariant, textAlign = TextAlign.Center, maxLines)` | bodyMedium. **Same Center-default rule.** |
| `SupportText(text, style?, maxLines?)` | `AppText(text, style = AppTextStyle.BodySmall, color = onSurfaceVariant, maxLines = 1)` | bodyMedium; legacy forced `maxLines = 1` — keep `maxLines = 1`. |
| `SecondaryTextColored(text, style?, color?, maxLines?, textAlign?)` | `AppText(text, style = <see below>, color = <caller's color>, maxLines, textAlign)` | Per-site. **Only 2 callers:** `NotesViews` ItemDate chip → `style = AppTextStyle.BodySmall, color = MaterialTheme.colorScheme.inverseOnSurface`. `TextFields` helperText → `style = AppTextStyle.Caption, color = MaterialTheme.colorScheme.onSurfaceVariant`. |

When a caller passes an explicit `textColor`/`color`, forward it verbatim. When it passes an explicit `textAlign`, forward it (do not also force Center).

### Button wrappers → AppButton / AppIconButton
`AppButton(text, onClick, modifier, style: AppButtonStyle, enabled, loading, leadingIcon: Painter?)` — `AppButtonStyle { Filled, Tonal, Outlined, Text }`. Drops the legacy `.uppercase()` and hardcoded `tertiary` colors (uses theme).
`AppIconButton(onClick, icon: Painter, contentDescription: String /*required*/, modifier, enabled, tint)` — enforces ≥48dp.

| Legacy | New |
|---|---|
| `DefaultButton(icon?, text, enabled?, textColor?, onClick)` | `AppButton(text = text, onClick = onClick, style = AppButtonStyle.Text, enabled = enabled, leadingIcon = icon)` |
| `DefaultButtonSecondaryLight(icon?, text, enabled?, onClick)` | `AppButton(text, onClick, style = AppButtonStyle.Text, enabled, leadingIcon = icon)` |
| `DefaultButtonOutlined(icon?, text, enabled?, onClick)` | `AppButton(text, onClick, style = AppButtonStyle.Outlined, enabled, leadingIcon = icon)` |
| `DefaultButtonFilled(icon?, text, enabled?, onClick)` | `AppButton(text, onClick, style = AppButtonStyle.Filled, enabled, leadingIcon = icon)` |
| `DefaultIconButton(childModifier?, icon, contentDescription, tint?, onClick)` | `AppIconButton(onClick = onClick, icon = icon, contentDescription = contentDescription, modifier = childModifier, tint = tint)` — if a caller passes `contentDescription = null`, supply a real localized string instead. |

### Card wrapper → AppCard
`AppCard(modifier, onClick: (() -> Unit)?, shape, elevation, contentPadding, content: ColumnScope)` — whole-card clickable, fillMaxWidth, surface, `Elevation.card`.

| Legacy | New |
|---|---|
| `DefaultCardClickable(modifier, shape?, padding?, onClick, content)` | `AppCard(modifier = modifier, onClick = onClick, contentPadding = 0.dp) { content }` — **pass `contentPadding = 0.dp`**: the legacy content provides its own inner `.padding(12.dp)`, so AppCard must not add its 16dp default on top. Outer spacing the caller applied via `modifier.padding(...)` is preserved. |

### Inline empty primitive — `NoContentView` (KEEP, modernize)
`NoContentView` is an **inline** centered empty (fillMaxWidth), distinct from the full-screen `EmptyState` (fillMaxSize). It is NOT duplicative — keep it, but rebuild its body on design-system primitives: replace its raw `Text(... color = customColors.secondaryTextColor)` with `AppText(text, style = AppTextStyle.Body, color = onSurfaceVariant, textAlign = TextAlign.Center)` and its icon tint `customColors.secondaryIconColor` → `MaterialTheme.colorScheme.onSurfaceVariant`. Keep its signature `(modifier, text, icon: Painter)` and all call sites unchanged.

### Off-grid `.dp` (opportunistic, only in files you already touch)
While editing a file, replace off-grid spacing literals (`6/10/14/18/22.dp`) with the nearest `Spacing`/`IconSize` token (4→`Spacing.xs`, 8→`Spacing.sm`, 12→`Spacing.md`, 16→`Spacing.lg`, 24→`Spacing.xl`; icon sizes 16→`IconSize.sm`? confirm: `IconSize { sm=18, md=24, lg=32 }` — so 18→`IconSize.sm`, 24→`IconSize.md`). Do not chase off-grid values in files outside your assigned set.

---

## Execution
- **Phase A (parallel, per-file):** migrate every legacy call site in the assigned file(s) per the table. Do NOT run gradle (parallel builds conflict); make precise edits only. Fix imports.
- **Phase B (after barrier, controller):** modernize `NoContentView`; grep-gate-delete the now-dead wrappers (8 text, 5 button, 1 card); delete emptied files (`Text.kt`, `Buttons.kt`); extend the `LegacyComponentRemovalTest` regression fence; run `:shared:compileDebugKotlinAndroid` + `:shared:compileKotlinIosSimulatorArm64` + `:shared:testDebugUnitTest` + `:androidApp:installDebug`.

## File partition (Phase A)
1. `home/notes/NotesViews.kt` (14 — incl. DefaultCardClickable×4, DefaultIconButton, SecondaryTextColored, PrimaryText, SupportText)
2. `home/weather/WeatherViews.kt` (10) + `home/weather/WeatherScreen.kt` (3)
3. `home/views/Dialogs.kt` (6) + `home/views/PhotoViews.kt` (2) + `home/views/DefaultViews.kt` (any button caller)
4. `home/map/MarkerInfoDialog.kt` (5) + `home/map/NewPlaceBottomSheet.kt` (1) + `home/new_catch/NewCatchSections.kt` (2)
5. `home/views/TextFields.kt`, `home/views/Counters.kt`, `home/views/AppBar.kt`, `home/profile/Profile.kt`, `home/place/PlaceViews.kt`, `home/catch/Catch.kt`, `home/catch/CatchScreenDialogs.kt` (1 each)

## Success criteria
- Zero references to the 14 legacy wrapper symbols remain (grep-gate clean); their definitions deleted.
- Both targets compile; tests green; app installs.
- No `.uppercase()` introduced; no code comments added; no `customColors` left in the touched text/empty code paths.
