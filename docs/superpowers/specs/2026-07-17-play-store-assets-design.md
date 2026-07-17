# Google Play Store Asset Redesign

## Goal

Create a cohesive, conversion-focused Google Play listing for Fishing Notes that feels useful, trustworthy, and emotionally connected to time on the water. The listing must show the real Android product, make each benefit understandable at a glance, and satisfy Google Play's image requirements.

## Selected direction: Dawn Water

Use a premium outdoor palette built from deep lake blue, teal, and a restrained sunrise-orange accent. Subtle dawn-water atmosphere, contour lines, and soft reflected light provide emotional character without competing with the product UI.

The product remains the focal point. Every screenshot uses real Fishing Notes UI rather than AI-invented interface elements. Marketing text is rendered deterministically so wording, typography, and spacing remain exact.

## Visual system

- Background: deep navy-to-teal lake atmosphere with restrained texture and generous negative space.
- Accent: sunrise orange used sparingly for emphasis and brand warmth.
- Typography: bold rounded headline styling aligned with the app's Nunito personality; short supporting copy only where it improves comprehension.
- Product framing: large rounded UI crop with a soft shadow and subtle edge highlight. Avoid a device-inside-device treatment.
- Composition: headline in the upper safe area; product UI dominates the lower two-thirds; meaningful UI remains legible at Play Store thumbnail size.
- Consistency: identical type scale, margins, corner radii, and shadow language across all screenshots.

## Screenshot sequence and copy

All screenshots are 1080 x 1920 portrait PNGs, 8-bit-per-channel RGB, with no alpha channel.

1. `01-spots.png`
   - Headline: `Never lose a great spot.`
   - Product view: map with saved fishing markers and a selected-place summary.
2. `02-catches.png`
   - Headline: `Remember every catch.`
   - Product view: catch detail showing species, weight, location, note, and tackle.
3. `03-details.png`
   - Headline: `Keep every detail.`
   - Product view: fishing log/history with useful catch metadata.
4. `04-weather.png`
   - Headline: `Know before you go.`
   - Product view: current conditions and forecast for a saved spot.
5. `05-story.png`
   - Headline: `Your fishing story, in one place.`
   - Product view: a complementary history/detail composition from real app UI.

Do not claim that offline sync is reliable or that cross-device syncing is complete until the release blockers identified in the readiness audit are fixed.

## Feature graphic

Create `feature-1024x500.png` at exactly 1024 x 500, 8-bit-per-channel RGB PNG, without alpha.

- Left: Fishing Notes icon/name and the tagline `Every spot. Every catch. One log.`
- Right: an energetic but readable layered composition of the map and catch UI.
- Keep important content away from the outer crop zones.
- The graphic must remain legible at small recommendation-card sizes.
- No Google Play badge, ranking language, price language, or call to action.

## Production approach

Use AI image generation only for the atmospheric, non-text background material. Composite the real checked-in app UI, exact copy, and brand mark locally. This prevents fabricated UI, misspelled marketing text, and misleading functionality while retaining a distinctive visual treatment.

Write the new set non-destructively to `docs/store-assets/v2/`. Preserve the current assets until the new set has passed visual and technical validation.

## Acceptance criteria

- Five portrait screenshots and one feature graphic are present in `docs/store-assets/v2/`.
- Exact dimensions, 8-bit RGB color, and absence of alpha are verified mechanically.
- Screenshot aspect ratio and format satisfy Google Play's published requirements.
- All visible marketing copy matches this specification exactly.
- UI content is derived from the real Fishing Notes app assets.
- No unsupported offline-sync, cross-device-sync, or reliability claims appear.
- The set is visually cohesive and each asset remains understandable at thumbnail size.
- Final assets are inspected individually and as a sequence before replacing existing files.
