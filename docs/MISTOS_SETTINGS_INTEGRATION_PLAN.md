# Rocket Launcher — MistOS Settings Integration Plan

**Target branch:** `16-dev`  
**Status:** Planned — implementation not yet started  
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
- Review MistOS Launcher3 17.0 dashboard, category activities, preference XML/resources, and feature implementations.
- Review Rocket's current Compose dashboard, destinations, preference models/keys, and platform capability checks.
- Build an internal feature matrix: already present in Rocket, missing and portable, requires adaptation/public API, ROM-only/unavailable, or not applicable.
- Check file-level licensing and dependencies before adapting code/assets.

### Phase 2 — Unified Mist-style Settings UI
- Rework Rocket's existing Settings dashboard to match MistOS's expressive card-based design.
- Adapt the relevant category screens, beginning with Home Settings, without changing their underlying preference behavior.
- Preserve every existing Rocket route and entry point; ensure search and navigation continue to work.
- Keep Rocket naming, app identity, and existing settings structure.

### Phase 3 — Missing feature integration
- Add only the audited missing Mist features to their appropriate Rocket categories.
- Reuse existing Rocket preference keys and implementations when equivalent functionality exists.
- Put genuinely uncategorized extras in a separate, clearly labeled category.
- Add compatibility guards/fallbacks for ROM-specific capabilities.

### Phase 4 — Validation
- Build the Android app and resolve compile/resource errors.
- Test Settings navigation, search, category rendering, and all affected controls.
- Verify that settings persist across app restarts and that existing Rocket preferences are not reset.
- Check unsupported features on non-MistOS Android behavior and test graceful fallback.
- Run available CI checks; report exact run status and provide an APK only after a successful build/artifact is confirmed.

## Scope guardrails

- Work on `16-dev` only unless explicitly authorized otherwise.
- Do not merge into `main` without explicit permission.
- Do not produce intermediate APK artifacts; provide the final test APK after the planned work and validation are complete.
- Do not remove existing Rocket features to make the dashboard resemble MistOS.
- This plan records intended work; it does not claim that the integration has been implemented or tested.
