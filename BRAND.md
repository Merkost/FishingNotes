# FishingNotes Brand Guide

## Brand Identity

FishingNotes is a fishing companion app that helps anglers track their catches, mark favorite spots, and plan trips with weather data. The brand is **friendly, nature-inspired, and functional** — it should feel like a reliable tool wrapped in a warm outdoor aesthetic.

**Personality**: Approachable, outdoorsy, reliable, clean
**Tone**: Encouraging without being pushy. Informative without being clinical.

---

## Color System

### Brand Colors

| Name | Hex | Usage |
|------|-----|-------|
| **Primary Green** | `#43A047` | Primary actions, active states, map/nature elements |
| **Primary Green Light** | `#76D275` | Highlights, success states, dark mode primary |
| **Primary Green Dark** | `#00701A` | Emphasis, contrast text on green |
| **Secondary Orange** | `#FF6D00` | CTAs, secondary actions, weather/energy elements |
| **Secondary Orange Light** | `#FF9E40` | Hover/pressed states, accents |
| **Secondary Orange Dark** | `#C43C00` | Emphasis on orange elements |
| **Primary Blue** | `#2196F3` | Alternative theme primary, water/sky elements |
| **Primary Blue Light** | `#6EC6FF` | Highlights in blue theme |
| **Primary Blue Dark** | `#0069C0` | Emphasis in blue theme |

### Onboarding Gradients

These gradients define the emotional tone of each feature area and should be reused when introducing or highlighting these features elsewhere in the app:

| Feature | Start Color | End Color | Direction |
|---------|-------------|-----------|-----------|
| **Map/Places** (nature, outdoors) | `#A5D6A7` | `#2E7D32` | Top → Bottom |
| **Catches** (water, depth) | `#90CAF9` | `#1565C0` | Top → Bottom |
| **Weather** (sun, warmth) | `#FFE082` | `#E65100` | Top → Bottom |

Use `Brush.verticalGradient(listOf(startColor, endColor))` in Compose.

### Text Colors

| Name | Value | Usage |
|------|-------|-------|
| Primary Text | `#DE000000` (87% black) | Headlines, body text |
| Secondary Text | `#8A000000` (54% black) | Subtitles, descriptions |
| Support Text | `#42000000` (26% black) | Hints, placeholders |
| On-gradient Text | `#FFFFFF` | Text on colored backgrounds |
| On-gradient Secondary | `#FFFFFF` at 85% opacity | Descriptions on gradients |

### Material 3 Theme Variants

The app ships with **two theme families** (Blue and Green), each with light and dark variants, plus **Dynamic Color** support on Android 12+.

**Default theme**: Blue Light

**Green theme** is the nature-focused option with warmer tones. Blue theme is cleaner and more neutral. Both use the same orange secondary.

### Custom Colors (beyond M3)

| Token | Light | Dark |
|-------|-------|------|
| `secondaryTextColor` | `#8A000000` | `#B0B0B8` |
| `secondaryIconColor` | `#6E6E76` | `#9E9EA6` |
| `backgroundSecondaryColor` | `#19FF9E40` (10% orange) | `#276EC6FF` (15% blue) |

Access via `LocalColors.current` in Compose.

---

## Typography

### Font Family: Nunito

Nunito is a rounded sans-serif that reinforces the friendly, approachable brand personality. It is loaded from bundled resources via `NunitoFontFamily()`.

### Type Scale

| Style | Weight | Size | Usage |
|-------|--------|------|-------|
| Display Small | Normal (400) | 36sp | Hero numbers, splash text |
| Headline Small | Normal (400) | 24sp | Screen titles |
| Title Large | SemiBold (600) | 20sp | Section headers, dialog titles |
| Title Medium | Medium (500) | 16sp | Card titles, onboarding titles |
| Title Small | Medium (500) | 14sp | Subtitles, prompt card title |
| Body Large | Normal (400) | 16sp | Primary body text, descriptions |
| Body Medium | Normal (400) | 14sp | Secondary text, list items |
| Body Small | Normal (400) | 12sp | Helper text, captions |
| Label Large | Medium (500) | 14sp | Buttons, bottom bar labels |
| Label Medium | Medium (500) | 12sp | Badges, small buttons |
| Label Small | Medium (500) | 11sp | Tags, metadata |

### Text Style Guidelines

- Headlines use `FontWeight.Bold` on gradients for contrast
- Body text on gradients uses `Color.White.copy(alpha = 0.85f)` for readability
- Bottom bar labels are **uppercase** (`text.uppercase()`)
- Button text uses `FontWeight.Bold` with `titleMedium` style

---

## Shapes & Corners

| Token | Radius | Usage |
|-------|--------|-------|
| Small | 4.dp | Subtle rounding, input fields |
| Medium | 8.dp | Cards (`MyCard`), default containers |
| Large | 16.dp | Prominent cards (`MyCardNoPadding`), modal sheets, prompt cards |
| Extra Large | 24.dp | Pill shapes, emphasis containers |
| Pill | 28.dp | Primary buttons (onboarding), FABs |
| Circle | `CircleShape` | Avatars, icon backgrounds, page dots |

### Modal Bottom Sheet

Top corners: 16.dp rounded, bottom corners: 0.dp (flush with bottom edge).

---

## Elevation & Shadow

| Level | Elevation | Usage |
|-------|-----------|-------|
| Flat | 0.dp | Backgrounds, surfaces |
| Low | 2.dp | `DefaultCard`, subtle content cards |
| Medium | 4.dp | `MyCardNoPadding`, info cards |
| High | 6-8.dp | `MyCard`, bottom bar, floating cards, prompt card |

Shadows are subtle — the app relies more on color and spacing than heavy elevation.

---

## Spacing System

The app uses a **4dp base grid** with these common intervals:

| Value | Usage |
|-------|-------|
| 4.dp | Tight padding (card internal, between small elements) |
| 8.dp | Standard gap (between page dots, small spacing) |
| 12.dp | Medium gap (between icon and text, title and description) |
| 16.dp | Section padding (card content padding, horizontal margins) |
| 24.dp | Large padding (Lottie internal padding, extra-large shape radius) |
| 32.dp | Screen-level horizontal padding (onboarding pages) |
| 48.dp | Major section breaks (onboarding top spacer) |

---

## Animation Guidelines

### Principles

- **Subtle over flashy** — animations guide attention, never distract
- **Spring physics** for interactive elements (bottom bar: stiffness 800, damping 0.8)
- **Tween easing** for content transitions (300-500ms range)
- **Staggered reveals** for related content (100ms delays between title → description)

### Standard Durations

| Type | Duration | Spec |
|------|----------|------|
| Quick fade | 200ms | `tween(200)` — skip buttons, tooltips |
| Standard transition | 300ms | `tween(300)` — page dots, color changes, crossfades |
| Content entrance | 400ms | `tween(400)` — titles, descriptions sliding in |
| Screen transition | 500ms | `tween(500)` — onboarding→app crossfade, card enter |
| Exit animation | 300ms | `tween(300)` — dismissals, card exits |

### Standard Patterns

- **Entrance**: `fadeIn + slideInVertically` (slide from direction of origin)
- **Exit**: `fadeOut + slideOutVertically` (slide toward exit direction)
- **Crossfade**: For swapping content in-place (button text changes, screen transitions)
- **AnimatedVisibility**: For show/hide elements (skip button, prompt cards, FAB menus)
- **graphicsLayer**: For per-frame alpha + translation (staggered text reveals)

### Lottie Animations

Rendered via Compottie (`AnimatedResource` composable). Default: infinite loop, `ContentScale.Fit`.

Available assets in `composeResources/files/`:

| Asset | Usage |
|-------|-------|
| `marker` / `marker_night` | Map pin, location features |
| `walking_fish` | Fish-themed loading, onboarding |
| `fish_loading` / `loading_fish` | Loading states |
| `clouds` | Weather features |
| `sunrise_sunset_animation` | Weather time-of-day |
| `my_location` | Current location indicator |
| `no_loaction` | Location unavailable state |
| `empty_status` | Empty lists, no data |
| `warning` | Error/warning dialogs |
| `error` | Error states |
| `confetti` | Success celebrations |
| `five_stars` | Rating/review prompt |
| `bye_bye` | Logout/farewell |
| `stats` | Statistics screen |
| `loading_animation` | Generic loading |
| `light_dark_mode_button` | Theme toggle |

---

## Component Patterns

### Buttons

- **Primary**: Filled button, pill shape (28.dp radius), `FontWeight.Bold`
- **On gradients**: White background, gradient's dark color as text color
- **Text buttons**: Used for secondary actions (Skip, Cancel), no background

### Cards

- Content cards: `DefaultCard` — surface color, 2.dp elevation, large shape
- Feature cards: `MyCard` — 8.dp elevation, 8.dp corner radius
- Prompt/CTA cards: Custom gradient background, 16.dp radius, 8.dp elevation

### Dialogs

- Standard: `DefaultDialog` wrapping `AlertDialog` with flexible button layouts
- Loading: `ModalLoadingDialog` — non-dismissible, progress indicators
- Feature-specific: Include Lottie animations for engagement

### Bottom Navigation

- 4 tabs: Map, Notes, Weather, Profile
- Spring-animated indicator that expands under selected tab
- Selected: primary color, icon + uppercase text label
- Unselected: `onSurfaceVariant` color, icon only (collapses)
- Surface elevation: 8.dp

### Floating Action Buttons

- Expandable FAB menu with rotation animation (45deg)
- Individual items with `AnimatedVisibility` (fade + size)
- Default bottom padding: 16.dp

---

## Iconography

- **Icon set**: Material Icons (Outlined variant preferred)
  - Map: `Icons.Outlined.Map`
  - Notes: `Icons.Outlined.Menu`
  - Weather: `Icons.Outlined.WbSunny`
  - Profile: `Icons.Outlined.Person`
- **Custom icons**: loaded via `painterResource(Res.drawable.*)`
- **Icon size on gradient backgrounds**: 40.dp with circular semi-transparent background (`Color.White.copy(alpha = 0.2f)`)

---

## Dark Mode

- Fully supported via Material 3 dark color schemes
- Three modes: System (follows device), Light (forced), Dark (forced)
- Gradient backgrounds (onboarding, prompt cards) remain the same in dark mode — they are self-contained and don't reference theme colors
- Lottie animations have night variants where available (`marker_night`, `mapstyle_night`)
- Custom colors adapt via `LocalColors` CompositionLocal

---

## Writing Style

- **Concise and action-oriented**: "Mark Your Spots" not "You can save your locations"
- **Encouraging**: "Never forget a great spot again" — positive framing
- **Feature titles**: 2-4 words, imperative verb + noun ("Log Your Catches", "Check the Weather")
- **Descriptions**: 1 sentence, under 15 words, explains the benefit not the mechanic
- **Button labels**: 1-2 words ("Next", "Get Started", "Skip")
- **Error messages**: Specific and helpful, not generic ("Can't connect to the server!" not "Error")

---

## Key Design Files

| File | Purpose |
|------|---------|
| `ui/theme/Color.kt` | All color definitions and M3 color schemes |
| `ui/theme/Type.kt` | Nunito font family and typography scale |
| `ui/theme/Shape.kt` | Corner radius definitions |
| `ui/theme/Theme.kt` | Theme composable (expect/actual per platform) |
| `ui/theme/CustomColors.kt` | Extended color tokens beyond M3 |
| `ui/utils/AnimatedResource.kt` | Lottie animation loading |
| `ui/home/views/Cards.kt` | Card component variants |
| `ui/home/Home.kt` | Bottom bar with spring animations |
| `ui/onboarding/OnboardingScreen.kt` | Onboarding pager reference implementation |
| `ui/home/map/FirstSpotPromptCard.kt` | Prompt card reference implementation |
| `utils/Constants.kt` | Shared dimension constants |
