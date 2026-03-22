# New Place: Dialog → Bottom Sheet Redesign

## Problem

The current `NewPlaceDialog` uses a `Dialog` composable that feels cramped. It packs a title field, description field, color picker (15 colors in a horizontal scroll), and buttons into a small card overlay. There isn't enough room for the fields and color picker to breathe.

## Decision

Replace the `Dialog` with a `ModalBottomSheet` that slides up from the bottom, giving more vertical space while keeping the map visible above.

## Design

### Layout (top to bottom)

1. **Drag handle** — standard bottom sheet affordance
2. **Header row** — "New place" title on the left, live marker icon preview (36dp) on the right that updates color in real-time
3. **Title field** — `OutlinedTextField`, empty by default. Geocoded place name shown as placeholder text. If saved empty, uses the geocoded name (falls back to "No name place" if geocoding failed)
4. **"+ Add description"** — collapsed by default as a clickable row with a plus icon. Tapping reveals an `OutlinedTextField` for description
5. **Color grid** — 8 columns × 2 rows grid of 34dp colored circles (all 15 `pickerColors` visible without scrolling). Selected color gets a white border ring. First color selected by default
6. **Cancel / Save buttons** — right-aligned row at the bottom

### Behavior

- `ModalBottomSheet` with `skipPartiallyExpanded = true`
- Dismissable via swipe down or Cancel button
- Save button triggers `viewModel.addNewMarker()` same as today
- Title field auto-focuses on open (request focus)
- Marker preview icon tint updates immediately when a color is tapped
- Character counter on title field (existing `MAX_PLACE_NAME_LENGTH` validation)

### What changes from current implementation

| Aspect | Before | After |
|--------|--------|-------|
| Container | `Dialog` + `MyCard` | `ModalBottomSheet` |
| Color picker | Horizontal `LazyRow` (scroll) | 8×2 grid (all visible) |
| Description | Always-visible text field | Collapsed "+ Add description", expandable on tap |
| Title default | Pre-filled with geocoded name | Empty, geocoded name as placeholder |
| Marker preview | 40dp box left of color row | 36dp icon in header row, updates live |

### Files to modify

- `shared/src/commonMain/.../ui/home/map/NewPlaceDialog.kt` — rewrite composable from `Dialog` to `ModalBottomSheet`, restructure layout
- `shared/src/commonMain/.../ui/home/map/MapScreen.kt` — update `newPlaceDialog` state handling to work with bottom sheet pattern (already uses `ModalBottomSheet` for settings, so the pattern exists)
- `shared/src/commonMain/.../ui/utils/ColorPicker.kt` — add a grid-based `ColorGrid` composable (keep existing `ColorPicker` for any other usages)

### Files unchanged

- `MapViewModel.kt` — no changes to place-saving logic
- `MapViews.kt` — `PlaceTileView`, `SetPlaceNameResultListener` stay as-is
- `pickerColors` list — same 15 colors

### Out of scope

- Adding new fields (photos, tags, etc.)
- Changing the place selection mode flow (crosshair + FAB)
- Modifying the `RawMapMarker` data model
