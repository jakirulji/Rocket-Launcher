# Rocket Launcher — MistOS Settings Integration Plan

**Target branch:** `16-dev`  
**Status:** Implementation in progress — see [feature audit](MISTOS_FEATURE_AUDIT.md) for ported items and remaining gaps  
**Reference:** [Project-Mist-OS/packages_apps_Launcher3 (17.0)](https://github.com/Project-Mist-OS/packages_apps_Launcher3/tree/17.0)

## Goal

Bring the MistOS Launcher Settings experience into Rocket Launcher while preserving Rocket's identity, existing functionality, navigation, preference storage, and compatibility with ordinary Android devices.

The target is to reproduce the MistOS Home Settings UI as faithfully as practical (layout, visual hierarchy, cards, colors, typography, spacing, icons/shapes, search treatment, and motion), while implementing it in Rocket's existing Jetpack Compose architecture. This is a UI adaptation, not a second launcher settings system.

## Product and UX requirements

1. **One unified Settings experience.** Do not add separate "Rocket" and "Mist" tabs, parallel settings dashboards, or duplicate entry points.
2. **Mist-inspired dashboard fidelity.** Recreate the MistOS expressive Settings dashboard and relevant Home Settings screens as closely as Rocket's Compose architecture allows. Match the card-based category layout, large Settings heading, search field, expressive shapes, rounded surfaces, visual hierarchy, and animations.
3. **Keep Rocket's existing destinations and functionality.** Preserve existing categories and routes, including General, Home Screen, Digital Wellbeing, Smartspace, Dock, App Drawer (where supported), Search, Folders, Gestures, Recents, Quickstep (where supported), Backup & Restore, and About.
4. **Port missing Mist features into the correct existing category.** Audit MistOS Launcher3 settings against Rocket before implementation:
   - Home-screen features → Home Screen
   - App-drawer features → App Drawer
   - Icon features → Icons (add/retain a dedicated category if needed)
   - Recent-apps features → Recents
   - Dock features → Dock
   - Gesture features → Gestures
   - General launcher behavior → the appropriate existing General/Misc category
5. **Avoid duplicates.** If Rocket already has an equivalent setting, retain Rocket's implementation and improve its presentation where appropriate; do not create a second toggle or competing preference key.
6. **Keep genuinely extra features separate.** Mist features that do not naturally belong in an existing category should be grouped in a clearly named additional category (for example, "Additional Features") rather than being forced into unrelated sections. Add a category only when the audit shows it is needed.
7. **Preserve user configuration.** Do not rename/remove existing preference keys or silently reset stored settings. Any unavoidable migration must be explicit, backward-compatible, and tested.
8. **Platform compatibility.** For MistOS/ROM-only or privileged features, use public Android APIs where feasible. Otherwise show a clear unavailable/unsupported state or a safe fallback; never expose a nonfunctional control as if it works.
9. **Branding and attribution.** Keep Rocket Launcher branding. Preserve required upstream Lawnchair and MistOS copyright/license notices for any adapted source. Verify each source file's applicable license before copying; attribution alone is not a substitute for license compliance.
10. **Architecture.** Implement the dashboard and screens in Rocket's existing Compose/navigation/preferences architecture. Do not import a parallel XML/View settings framework solely to reproduce the visuals.

## Implementation phases

### Phase 1 — Source and feature audit
- [x] Review MistOS Launcher3 17.0 dashboard, category activities, preference XML/resources, and feature implementations.
- [x] Review Rocket's current Compose dashboard, destinations, preference models/keys, and platform capability checks.
- [x] Record the initial feature matrix in [MISTOS_FEATURE_AUDIT.md](MISTOS_FEATURE_AUDIT.md). The matrix remains open for deeper runtime verification.
- [x] Check file-level licensing for the source already adapted; continue checking before each additional port.

### Phase 2 — Unified Mist-style Settings UI
- [x] Rework the existing dashboard into the Mist-inspired asymmetric card layout in Compose.
- [x] Port the expressive radial shape algorithm and spring press motion.
- [x] Add a collapsing Settings title and a working category search dialog.
- [x] Expand Settings search from category-only results to an indexed set of individual controls and common search synonyms. Results open the existing owning settings destination; direct control deep-links remain a future enhancement.
- [ ] Adapt the remaining category screens while preserving Rocket preference behavior.
- [x] Preserve Rocket branding and existing routes.

### Phase 3 — Missing feature integration
- [x] Port short wallpaper parallax, single-page wallpaper centering, and wallpaper zooming into Home Screen settings and runtime code.
- [x] Port Focus Mode into App Drawer settings and the long-press popup lifecycle, with a blur-disabled scrim fallback.
- [x] Add the Mist-style Misc category with phone-only fixed landscape mode, background blur depth, and restart action.
- [ ] Port the remaining audited App Library, pinned apps, Recents, extended Smartspace, and unported Misc features where compatible.
- [ ] Reuse Rocket preference keys and implementations where behavior already matches.
- [ ] Keep genuinely uncategorized extras in a clearly labeled category.
- [ ] Add compatibility guards/fallbacks for ROM-specific capabilities.

### Phase 4 — Validation
- [x] Style check has passed for the initial dashboard implementation.
- [ ] Complete CI build validation for the current combined dashboard and feature ports.
- [ ] Test Settings navigation, search, category rendering, and all affected controls on-device.
- [ ] Verify preference persistence and ensure existing Rocket preferences are not reset.
- [ ] Test graceful fallback on non-MistOS Android devices.
- [ ] Run the final manual release workflow and provide an APK only after a successful build/artifact is confirmed.

## Scope guardrails

- Work on `16-dev` only unless explicitly authorized otherwise.
- Do not merge into `main` without explicit permission.
- Do not produce intermediate APK artifacts; provide the final test APK after the planned work and validation are complete.
- Do not remove existing Rocket features to make the dashboard resemble MistOS.
- This plan records intended work; it does not claim that the integration has been implemented or tested.
