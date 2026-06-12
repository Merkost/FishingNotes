# FishingNotes UI/UX Modern Refresh — Master Progress Tracker

Single source of truth for the whole-app UI/UX overhaul. Update the **Status** columns as work proceeds.

- **Design spec:** [`../specs/2026-06-12-ui-ux-modern-refresh-design.md`](../specs/2026-06-12-ui-ux-modern-refresh-design.md)
- **Branch:** `compose_migration`
- **Started:** 2026-06-12
- **Approach:** Design-system-first. Build tokens + components (S1), then roll across every screen (S2–S4), legacy components deleted only in the final sweep.

## Status legend
- ⬜ Not started
- 📝 Plan authored (ready to implement)
- 🟨 In progress
- ✅ Done & verified (compiles + screenshot reviewed)
- ⛔ Blocked

## Locked decisions
| Decision | Choice |
|---|---|
| Visual ambition | Modern refresh (keep M3 + Blue/Green brand, elevate) |
| Strategy | Design-system-first |
| Design principle | Reuse M3, **style with personality** (not stock Material) |
| Top bars | **Surface** app-wide (Weather hero card stays colored) |
| Bottom nav | M3 `NavigationBar` base + custom-styled active indicator/motion |
| Non-visual fixes | Fix entangled correctness/i18n bugs in-scope |
| Typography | Fetch **real Nunito** weight files + tune metrics |
| Constraint | **KMP/iOS-safe** (all shared UI in commonMain) |
| Verification | User runs app; build `:androidApp:installDebug` + screenshot per phase |

---

## Plans & sprints

| Plan | Sprint | Scope | Depends on | Plan doc | Impl status |
|---|---|---|---|---|---|
| 01 | S1 Design System | Foundation tokens & theme (Spacing/Elevation/Shapes/Motion/Emphasis, color cleanup, Nunito fonts) | — | `2026-06-12-01-foundation-tokens-theme.md` | ⬜ |
| 02 | S1 Design System | Core Components I — primitives & state system | 01 | `2026-06-12-02-core-components-primitives-state.md` | ⬜ |
| 03 | S1 Design System | Core Components II — chrome, brand & content | 01, 02 | `2026-06-12-03-core-components-chrome-brand-content.md` | ⬜ |
| 04 | S2 Primary | First-run & app shell (Login, Onboarding, Home + bottom nav) | 01–03 | `2026-06-12-04-firstrun-app-shell.md` | ⬜ |
| 05 | S2 Primary | Notes (tabs, Catches, Places, Statistics) | 01–03 | `2026-06-12-05-notes.md` | ⬜ |
| 06 | S2 Primary | Weather & Daily detail | 01–03 | `2026-06-12-06-weather.md` | ⬜ |
| 07 | S3 Create & Detail | New Catch flow | 01–03 | `2026-06-12-07-new-catch.md` | ⬜ |
| 08 | S3 Create & Detail | Detail screens (Place, Catch) | 01–03 | `2026-06-12-08-detail-place-catch.md` | ⬜ |
| 09 | S4 Account & Finish | Profile & Edit Profile | 01–03 | `2026-06-12-09-profile.md` | ⬜ |
| 10 | S4 Account & Finish | Settings & About | 01–03 | `2026-06-12-10-settings-about.md` | ⬜ |
| 12 | S2 Primary | Map screen (BrandFab, floating controls, skeletons, drag handle) | 01–03 | `2026-06-12-12-map-screen.md` | ⬜ |
| 11 | S4 Account & Finish | Final sweep (delete legacy, a11y/consistency, full build) | 04–10, 12 | `2026-06-12-11-final-sweep.md` | ⬜ |

> Plan 12 (Map) was added after the initial 11 so the Map tab — the app's start destination — gets a dedicated screen pass. **Total: 12 plans.**
> Plan-authoring status (this planning round) is tracked separately below until all 12 docs land.

### Plan-authoring checklist — ✅ COMPLETE (all 12 docs written, no code)
- [x] 01 Foundation tokens & theme (11 tasks)
- [x] 02 Core components I (11 tasks)
- [x] 03 Core components II (23 tasks)
- [x] 04 First-run & shell (8 tasks)
- [x] 05 Notes (9 tasks)
- [x] 06 Weather (11 tasks)
- [x] 07 New Catch (10 tasks)
- [x] 08 Detail (13 tasks)
- [x] 09 Profile (9 tasks)
- [x] 10 Settings & About (8 tasks)
- [x] 12 Map screen (9 tasks)
- [x] 11 Final sweep (11 tasks)

**~133 tasks total.** Placeholder scan clean; cross-plan component names reconciled (see reconciliations below).

---

## Screen coverage matrix
Every user-facing surface must be touched. Mark ✅ when its owning plan ships and screenshots are approved.

| Screen | Owning plan | Status |
|---|---|---|
| Login | 04 | ⬜ |
| Onboarding | 04 | ⬜ |
| Home scaffold / bottom nav | 04 | ⬜ |
| Map | 12 | ⬜ |
| Notes container & tabs | 05 | ⬜ |
| Catches list | 05 | ⬜ |
| Places list | 05 | ⬜ |
| Statistics | 05 | ⬜ |
| Weather | 06 | ⬜ |
| Weather Daily | 06 | ⬜ |
| New Catch | 07 | ⬜ |
| Place detail | 08 | ⬜ |
| Catch detail | 08 | ⬜ |
| Profile | 09 | ⬜ |
| Edit Profile | 09 | ⬜ |
| Settings | 10 | ⬜ |
| About | 10 | ⬜ |

> **Note:** Map screen components (floating controls, BrandFab, brand gradient, skeletons) are built in Plan 03; the Map screen's own adoption pass is **Plan 12**.

---

## Verification checkpoints
Run after each plan; do not mark a plan ✅ until all three pass.
- [ ] **Compiles:** `./gradlew :shared:compileDebugKotlinAndroid` (or the confirmed task) + `:androidApp:installDebug`
- [ ] **iOS-safe:** shared source set still compiles (`./gradlew :shared:compileKotlinIosSimulatorArm64` or nearest)
- [ ] **Screenshots:** affected screens captured light + dark on emulator and reviewed

---

## Cross-plan contract reconciliations
Resolve these when implementing Plans 01–03 (each consuming plan already has a "confirm signature, else fall back" note; these are the canonical answers):

1. **Component package** — all new shared components live in `com.mobileprism.fishing.ui.home.views` (with existing `Buttons`/`Cards`/`Text`/`AppBar`); tokens live in `com.mobileprism.fishing.ui.theme`. Plan 03 + screen plans must import components from `ui.home.views`, not `ui.theme`.
2. **`SettingsSelectionDialog`** — drop the `T : StringOperation` bound from spec §5.3. The weather unit enums (`TemperatureValues`/`PressureValues`/`WindSpeedValues`) do **not** implement `StringOperation` (they expose `.stringRes`). Signature becomes `SettingsSelectionDialog<T>(title, options, current, onSelect, optionLabel: @Composable (T) -> String)`. (`SortOptionsSheet<T : StringOperation>` keeps its bound — the sort enums do implement it; verify in Plan 05/10.)
3. **`EditBottomSheetScaffold`** — add an optional `leadingAction: (@Composable () -> Unit)? = null` slot (Plan 08 needs it for the Note delete / Photos gallery+camera actions).
4. **`FishAmountAndWeightView` is NOT dead** — it's called in `CatchScreenDialogs.kt`. Plan 07 migrates that caller to `StepperField` first, then grep-gates the deletion. Do not delete blindly.
5. **Color literal count** — actual hardcoded `Color(0x..)` outside `ui/theme` is ~150 across 6 files (spec said ~145). Screen-local hex (Map/Onboarding/FirstSpotPromptCard gradients, Settings swatches) is migrated by its owning screen/component plan, not Plan 01.

## Decisions / changes log
| Date | Note |
|---|---|
| 2026-06-12 | Audit completed (20 agents). Spec written. 11-plan / 4-sprint structure approved. |
| 2026-06-12 | Added Plan 12 (Map screen) → **12 plans total**. |
| 2026-06-12 | All 12 plan docs authored (~133 tasks). Self-review done: placeholder scan clean, `StatusView` dangling refs in Plan 06 reconciled to `ScreenStateContent`/`EmptyState`/`ErrorState`, cross-plan contracts logged above. |

## Open questions / follow-ups
- Source of real Nunito weight files (Google Fonts OFL) — confirm licensing/inclusion in repo (Plan 01).
- Whether to commit the spec/plans to git (currently uncommitted per repo "commit only when asked" rule).
- Execution mode for Plan 01: subagent-driven (fresh agent per task + review) vs inline batches.
