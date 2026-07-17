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

make_panel "$ROOT/docs/store-assets/02-map.png" '860x1280+67+374' 350 "$TMP/feature-map.png"
make_panel "$ROOT/docs/store-assets/03-catch.png" '860x1120+67+374' 285 "$TMP/feature-catch.png"
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
  -draw 'roundrectangle 60,183 190,193 5,5' \
  "$TMP/feature-icon.png" \
  -gravity northwest \
  -geometry +60+58 \
  -composite \
  -font "$FONT_BOLD" \
  -pointsize 48 \
  -fill white \
  -gravity northwest \
  -annotate +165+72 'Fishing Notes' \
  "$TMP/feature-tagline.png" \
  -gravity northwest \
  -geometry +60+215 \
  -composite \
  "$TMP/feature-map-rotated.png" \
  -gravity northwest \
  -geometry +660+15 \
  -composite \
  "$TMP/feature-catch-rotated.png" \
  -gravity northwest \
  -geometry +820+112 \
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
