# Google Play Store Assets Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Produce five conversion-focused Google Play screenshots and one feature graphic for Fishing Notes using real product UI and a cohesive Dawn Water visual system.

**Architecture:** Generate two text-free atmospheric background masters, then use one deterministic ImageMagick compositor to place real checked-in UI, exact Nunito typography, and the Fishing Notes mark. The compositor writes upload-ready RGB PNGs into a non-destructive `docs/store-assets/v2/` directory and a final validation pass checks format, dimensions, copy, and sequence-level visual cohesion.

**Tech Stack:** Built-in image generation, ImageMagick, Nunito font assets, shell validation, Google Play image requirements.

## Global Constraints

- Screenshots must be exactly 1080 x 1920 portrait PNGs.
- The feature graphic must be exactly 1024 x 500.
- Every final image must be 8-bit-per-channel RGB with no alpha channel.
- Product UI must come from checked-in Fishing Notes assets; do not generate or fabricate UI.
- Marketing copy must match the approved design verbatim.
- Do not advertise reliable offline sync, completed cross-device sync, or other audit-blocked behavior.
- Preserve existing `docs/store-assets/*.png` files until the v2 set passes validation.

---

### Task 1: Generate the Dawn Water background masters

**Files:**
- Create: `docs/store-assets/v2/sources/dawn-water-portrait.png`
- Create: `docs/store-assets/v2/sources/dawn-water-landscape.png`

**Interfaces:**
- Consumes: the approved Dawn Water palette and composition rules.
- Produces: text-free raster backgrounds used by the compositor in Task 2.

- [ ] **Step 1: Create the output source directory**

Run:

```bash
mkdir -p docs/store-assets/v2/sources
```

Expected: the directory exists and `git status --short` shows no generated files yet.

- [ ] **Step 2: Generate the portrait master**

Use the built-in image generator with this exact prompt:

```text
Use case: ads-marketing
Asset type: reusable portrait Google Play screenshot background
Primary request: Create an abstract premium dawn-on-the-water atmosphere for a fishing journal app. Deep lake navy at the top transitions through rich teal to a restrained sunrise-orange reflected glow near one lower edge. Add very subtle topographic contour lines and soft water-ripple texture. Keep the center calm and uncluttered for a large app UI panel.
Composition/framing: portrait, broad negative space, no horizon cutting through the upper headline area, strongest texture toward the outer edges
Lighting/mood: calm dawn light, capable, adventurous, trustworthy, premium rather than rustic
Color palette: #082C3D, #0B5E73, #0E88A8, restrained #FF7A1A accent
Constraints: background only; no text, no logos, no icons, no phone, no interface, no people, no fish, no boats, no watermark
Avoid: photorealistic landscape, busy waves, neon colors, heavy grain, lens flare, central focal object
```

Save the selected result to `docs/store-assets/v2/sources/dawn-water-portrait.png`.

- [ ] **Step 3: Generate the landscape master**

Use the built-in image generator with this exact prompt:

```text
Use case: ads-marketing
Asset type: Google Play feature-graphic background
Primary request: Create a wide abstract premium dawn-on-the-water atmosphere for a fishing journal app. Deep lake navy and teal dominate, with a restrained sunrise-orange reflection entering from the right. Add very subtle topographic contour lines and soft water-ripple texture. Preserve clean negative space across the left half for a brand name and tagline.
Composition/framing: wide landscape, calm left side, slightly more energy and reflected light on the right, no central object
Lighting/mood: calm dawn light, capable, adventurous, trustworthy, premium rather than rustic
Color palette: #082C3D, #0B5E73, #0E88A8, restrained #FF7A1A accent
Constraints: background only; no text, no logos, no icons, no phone, no interface, no people, no fish, no boats, no watermark
Avoid: photorealistic landscape, busy waves, neon colors, heavy grain, lens flare, central focal object
```

Save the selected result to `docs/store-assets/v2/sources/dawn-water-landscape.png`.

- [ ] **Step 4: Inspect both background masters**

Open both files with the image viewer. Confirm there is usable headline space, no generated text or UI, and the two images read as one visual family.

- [ ] **Step 5: Commit the approved background masters**

```bash
git add docs/store-assets/v2/sources
git commit -m "assets: add Dawn Water background masters"
```

### Task 2: Build the deterministic asset compositor

**Files:**
- Create: `docs/store-assets/v2/build-assets.sh`
- Create: `docs/store-assets/v2/01-spots.png`
- Create: `docs/store-assets/v2/02-catches.png`
- Create: `docs/store-assets/v2/03-details.png`
- Create: `docs/store-assets/v2/04-weather.png`
- Create: `docs/store-assets/v2/05-story.png`
- Create: `docs/store-assets/v2/feature-1024x500.png`

**Interfaces:**
- Consumes: the two Task 1 backgrounds; `docs/store-assets/02-map.png`, `03-catch.png`, `04-notes.png`, `05-weather.png`; `docs/store-assets/icon-512.png`; Nunito fonts under `shared/src/commonMain/composeResources/font/`.
- Produces: six exact-dimension, upload-ready images and a reproducible build script.

- [ ] **Step 1: Write `build-assets.sh`**

Create the file with this complete implementation:

```bash
#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../.." && pwd)"
OUT="$ROOT/docs/store-assets/v2"
SOURCES="$OUT/sources"
FONT_BOLD="$ROOT/shared/src/commonMain/composeResources/font/nunito_bold.ttf"
FONT_SEMIBOLD="$ROOT/shared/src/commonMain/composeResources/font/nunito_semibold.ttf"
PORTRAIT_BG="$SOURCES/dawn-water-portrait.png"
LANDSCAPE_BG="$SOURCES/dawn-water-landscape.png"
TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT

required=(
  "$FONT_BOLD"
  "$FONT_SEMIBOLD"
  "$PORTRAIT_BG"
  "$LANDSCAPE_BG"
  "$ROOT/docs/store-assets/02-map.png"
  "$ROOT/docs/store-assets/03-catch.png"
  "$ROOT/docs/store-assets/04-notes.png"
  "$ROOT/docs/store-assets/05-weather.png"
  "$ROOT/docs/store-assets/icon-512.png"
)

for file in "${required[@]}"; do
  if [[ ! -f "$file" ]]; then
    printf 'Missing required source: %s\n' "$file" >&2
    exit 1
  fi
done

mkdir -p "$OUT"

make_portrait_background() {
  local destination="$1"
  magick "$PORTRAIT_BG" \
    -auto-orient \
    -resize '1080x1920^' \
    -gravity center \
    -extent 1080x1920 \
    -colorspace sRGB \
    "$destination"
}

make_panel() {
  local input="$1"
  local crop="$2"
  local width="$3"
  local destination="$4"
  local stem
  local panel_width
  local panel_height
  stem="$(basename "$destination" .png)"

  magick "$input" \
    -crop "$crop" +repage \
    -resize "${width}x" \
    -alpha off \
    "$TMP/${stem}-raw.png"

  panel_width="$(identify -format '%w' "$TMP/${stem}-raw.png")"
  panel_height="$(identify -format '%h' "$TMP/${stem}-raw.png")"

  magick -size "${panel_width}x${panel_height}" xc:none \
    -fill white \
    -draw "roundrectangle 0,0 $((panel_width - 1)),$((panel_height - 1)) 54,54" \
    "$TMP/${stem}-mask.png"

  magick "$TMP/${stem}-raw.png" "$TMP/${stem}-mask.png" \
    -alpha off \
    -compose CopyOpacity \
    -composite \
    "$destination"
}

add_shadow() {
  local input="$1"
  local destination="$2"
  magick "$input" \
    \( +clone -background '#00151F' -shadow 48x18+0+24 \) \
    +swap \
    -background none \
    -layers merge \
    +repage \
    "$destination"
}

make_headline() {
  local text="$1"
  local point_size="$2"
  local destination="$3"
  magick \
    -background none \
    -fill white \
    -font "$FONT_BOLD" \
    -gravity center \
    -size 940x230 \
    -pointsize "$point_size" \
    caption:"$text" \
    "$destination"
}

make_screenshot() {
  local destination="$1"
  local source_image="$2"
  local headline="$3"
  local point_size="$4"
  local crop="${5:-860x1764+67+374}"
  local panel_width="${6:-920}"
  local stem
  stem="$(basename "$destination" .png)"

  make_portrait_background "$TMP/${stem}-background.png"
  make_panel "$source_image" "$crop" "$panel_width" "$TMP/${stem}-panel.png"
  add_shadow "$TMP/${stem}-panel.png" "$TMP/${stem}-panel-shadow.png"
  make_headline "$headline" "$point_size" "$TMP/${stem}-headline.png"

  magick "$TMP/${stem}-background.png" \
    -font "$FONT_SEMIBOLD" \
    -pointsize 26 \
    -kerning 6 \
    -fill '#8EDCE8' \
    -gravity north \
    -annotate +0+58 'FISHING NOTES' \
    "$TMP/${stem}-headline.png" \
    -gravity north \
    -geometry +0+105 \
    -composite \
    "$TMP/${stem}-panel-shadow.png" \
    -gravity north \
    -geometry +0+420 \
    -composite \
    -alpha off \
    -colorspace sRGB \
    -depth 8 \
    -define png:exclude-chunk=date,time \
    "PNG24:$destination"
}

make_screenshot \
  "$OUT/01-spots.png" \
  "$ROOT/docs/store-assets/02-map.png" \
  'Never lose a great spot.' \
  82 \
  '860x1400+67+700' \
  920

make_screenshot \
  "$OUT/02-catches.png" \
  "$ROOT/docs/store-assets/03-catch.png" \
  'Remember every catch.' \
  82

make_screenshot \
  "$OUT/03-details.png" \
  "$ROOT/docs/store-assets/04-notes.png" \
  'Keep every detail.' \
  86 \
  '860x1688+67+450' \
  920

make_screenshot \
  "$OUT/04-weather.png" \
  "$ROOT/docs/store-assets/05-weather.png" \
  'Know before you go.' \
  86

make_portrait_background "$TMP/story-background.png"
make_panel "$ROOT/docs/store-assets/04-notes.png" '860x1688+67+450' 760 "$TMP/story-log.png"
make_panel "$ROOT/docs/store-assets/03-catch.png" '860x1320+67+374' 440 "$TMP/story-catch.png"
add_shadow "$TMP/story-log.png" "$TMP/story-log-shadow.png"
add_shadow "$TMP/story-catch.png" "$TMP/story-catch-shadow.png"
make_headline $'Your fishing story,\nin one place.' 72 "$TMP/story-headline.png"

magick "$TMP/story-background.png" \
  -font "$FONT_SEMIBOLD" \
  -pointsize 26 \
  -kerning 6 \
  -fill '#8EDCE8' \
  -gravity north \
  -annotate +0+58 'FISHING NOTES' \
  "$TMP/story-headline.png" \
  -gravity north \
  -geometry +0+105 \
  -composite \
  "$TMP/story-log-shadow.png" \
  -gravity northwest \
  -geometry +52+445 \
  -composite \
  "$TMP/story-catch-shadow.png" \
  -gravity southeast \
  -geometry +18+45 \
  -composite \
  -alpha off \
  -colorspace sRGB \
  -depth 8 \
  -define png:exclude-chunk=date,time \
  "PNG24:$OUT/05-story.png"

magick "$LANDSCAPE_BG" \
  -auto-orient \
  -resize '1024x500^' \
  -gravity center \
  -extent 1024x500 \
  -colorspace sRGB \
  "$TMP/feature-background.png"

make_panel "$ROOT/docs/store-assets/02-map.png" '860x1280+67+374' 260 "$TMP/feature-map.png"
make_panel "$ROOT/docs/store-assets/03-catch.png" '860x1120+67+374' 210 "$TMP/feature-catch.png"
add_shadow "$TMP/feature-map.png" "$TMP/feature-map-shadow.png"
add_shadow "$TMP/feature-catch.png" "$TMP/feature-catch-shadow.png"

magick "$TMP/feature-map-shadow.png" -background none -rotate -3 "$TMP/feature-map-rotated.png"
magick "$TMP/feature-catch-shadow.png" -background none -rotate 4 "$TMP/feature-catch-rotated.png"
magick "$ROOT/docs/store-assets/icon-512.png" -resize 82x82 "$TMP/feature-icon.png"
magick \
  -background none \
  -fill white \
  -font "$FONT_BOLD" \
  -gravity northwest \
  -size 500x165 \
  -pointsize 50 \
  caption:'Every spot. Every catch. One log.' \
  "$TMP/feature-tagline.png"

magick "$TMP/feature-background.png" \
  -fill '#FF7A1A' \
  -draw 'roundrectangle 96,183 226,193 5,5' \
  "$TMP/feature-icon.png" \
  -gravity northwest \
  -geometry +96+58 \
  -composite \
  -font "$FONT_BOLD" \
  -pointsize 48 \
  -fill white \
  -gravity northwest \
  -annotate +201+72 'Fishing Notes' \
  "$TMP/feature-tagline.png" \
  -gravity northwest \
  -geometry +96+215 \
  -composite \
  "$TMP/feature-map-rotated.png" \
  -gravity northwest \
  -geometry +610+15 \
  -composite \
  "$TMP/feature-catch-rotated.png" \
  -gravity northwest \
  -geometry +690+125 \
  -composite \
  -alpha off \
  -colorspace sRGB \
  -depth 8 \
  -define png:exclude-chunk=date,time \
  "PNG24:$OUT/feature-1024x500.png"

printf '%s\n' \
  "$OUT/01-spots.png" \
  "$OUT/02-catches.png" \
  "$OUT/03-details.png" \
  "$OUT/04-weather.png" \
  "$OUT/05-story.png" \
  "$OUT/feature-1024x500.png"
```

- [ ] **Step 2: Make the compositor executable and run it**

```bash
chmod +x docs/store-assets/v2/build-assets.sh
docs/store-assets/v2/build-assets.sh
```

Expected: exit 0 and all six final paths are printed once.

- [ ] **Step 3: Verify deterministic rebuild**

```bash
before=$(shasum -a 256 docs/store-assets/v2/*.png)
docs/store-assets/v2/build-assets.sh >/dev/null
after=$(shasum -a 256 docs/store-assets/v2/*.png)
test "$before" = "$after"
```

Expected: exit 0.

- [ ] **Step 4: Commit compositor and generated assets**

```bash
git add docs/store-assets/v2/build-assets.sh docs/store-assets/v2/*.png
git commit -m "assets: create Play store campaign"
```

### Task 3: Validate Google Play compliance and visual quality

**Files:**
- Inspect: `docs/store-assets/v2/*.png`
- Create: `docs/store-assets/v2/contact-sheet.png`

**Interfaces:**
- Consumes: all six Task 2 deliverables.
- Produces: mechanical compliance evidence and one sequence-level review image.

- [ ] **Step 1: Validate screenshot dimensions and formats**

Run:

```bash
for file in docs/store-assets/v2/0*.png; do
  test "$(identify -format '%wx%h' "$file")" = "1080x1920"
  test "$(identify -format '%[channels]' "$file")" = "srgb  3.0"
  test "$(identify -format '%[bit-depth]' "$file")" = "8"
done
```

Expected: exit 0 with no output.

- [ ] **Step 2: Validate feature-graphic dimensions and format**

Run:

```bash
file=docs/store-assets/v2/feature-1024x500.png
test "$(identify -format '%wx%h' "$file")" = "1024x500"
test "$(identify -format '%[channels]' "$file")" = "srgb  3.0"
test "$(identify -format '%[bit-depth]' "$file")" = "8"
```

Expected: exit 0 with no output.

- [ ] **Step 3: Build the contact sheet**

Run:

```bash
font=shared/src/commonMain/composeResources/font/nunito_regular.ttf
magick montage docs/store-assets/v2/0*.png \
  -font "$font" \
  -thumbnail 216x384 \
  -tile 5x1 \
  -geometry +18+18 \
  -background '#07141C' \
  /tmp/fishingnotes-screenshot-row.png
magick docs/store-assets/v2/feature-1024x500.png -resize 768x375 /tmp/fishingnotes-feature.png
magick -size 1278x850 xc:'#07141C' \
  /tmp/fishingnotes-screenshot-row.png -gravity north -geometry +0+20 -composite \
  /tmp/fishingnotes-feature.png -gravity south -geometry +0+25 -composite \
  -alpha off -colorspace sRGB -depth 8 \
  'PNG24:docs/store-assets/v2/contact-sheet.png'
```

Expected: `docs/store-assets/v2/contact-sheet.png` is 1278 x 850, 8-bit RGB, and shows the five screenshots in order above the feature graphic.

- [ ] **Step 4: Inspect every final asset and the contact sheet**

Confirm:

- exact headline spelling;
- no clipped or unreadable important UI;
- no generated UI or fabricated functionality;
- consistent margins, scale, radius, and shadows;
- each asset communicates one distinct benefit at thumbnail size;
- no offline-sync or cross-device-sync claims;
- feature graphic remains legible when viewed at approximately 512 x 250.

- [ ] **Step 5: Run repository hygiene checks**

```bash
git diff --check
git status --short
```

Expected: only the intended contact sheet or final asset adjustments are uncommitted.

- [ ] **Step 6: Commit validation artifact or final visual corrections**

```bash
git add docs/store-assets/v2
git commit -m "assets: finalize Play store listing visuals"
```

Expected: commit succeeds and `git status --short` is empty.
