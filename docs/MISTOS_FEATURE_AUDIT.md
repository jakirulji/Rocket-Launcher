# MistOS Launcher Feature Audit

**Compared branches:** MistOS Launcher3 `17.0` vs Rocket Launcher `16-dev`  
**Status:** Initial source audit; runtime-level verification is still required.

This audit compares MistOS's category preference XML/source with Rocket's current Compose destinations. A similarly named control is not automatically considered equivalent: the underlying behavior must also match.

## Existing Rocket equivalents identified

| MistOS feature | Rocket location | Audit result |
|---|---|---|
| Icon pack selection | General | Existing |
| Notification dots | General | Existing |
| Home-screen icon size and labels | Home Screen | Existing |
| Home-screen grid and padding | Home Screen | Existing |
| Workspace lock | Home Screen | Existing |
| Auto-add shortcuts | Home Screen | Existing |
| Double-tap gesture | Home Screen / Gestures | Existing equivalent; haptic parity needs checking |
| Status-bar visibility and dark icons | Home Screen | Existing |
| Top shadow / system UI scrim | Home Screen | Existing |
| Wallpaper scrolling and depth effect | Home Screen | Existing equivalents |
| Short wallpaper parallax | Home Screen + WallpaperOffsetInterpolator | Ported: same minimum-page-span algorithm, now configurable |
| Center wallpaper on a single page | Home Screen + WallpaperOffsetInterpolator | Ported: single-page zero-minimum-scroll behavior |
| Wallpaper zooming | Home Screen + BaseDepthController | Ported: public WallpaperManager zoom is disabled by preference when off |
| Minus-one feed | Home Screen | Existing |
| Smartspace providers, data-source toggles, date/time/calendar and time format | Smartspace | Existing Rocket implementation |
| App drawer grid, labels and row height | App Drawer | Existing |
| Focus Mode behind app long-press popup | App Drawer + PopupContainerWithArrow | Ported: live RenderEffect blur with scrim fallback and lifecycle cleanup |
| App drawer scrollbar | App Drawer | Existing |
| Two-line app labels | App Drawer | Existing |
| Hidden apps | App Drawer | Existing |
| Recents screenshot/share/Lens/clear-all actions | Recents | Existing quick-action equivalents |
| Recents translucent background and alpha | Recents | Existing equivalent |
| Fixed landscape mode | Misc + Launcher.onResume | Ported for phone devices; preference now applies launcher orientation |
| Background blur depth | Misc + BaseDepthController | Ported as a 0–60 dp control; apply with Restart Launcher |
| Content suggestions shortcut | Misc | Ported conditionally when Google Device Personalization Services and its settings activity are available |
| Suggestions shortcut | Misc | Ported conditionally when DPS and its settings activity are available |
| Restart launcher | Misc | Ported through Rocket's existing restart helper |
| Overscroll tuning | Home Screen / Additional Features | Not ported yet; runtime integration still needs source/API audit |

## Missing or not-yet-verified MistOS features

### Home Screen / Smartspace
- Mist's extended Quickspace styling: clock style/font/colors, PSA, now playing, weather provider/city/text
- Rocket already has Smartspace provider selection, data-source toggles, calendar/date visibility, and time-format controls; the extended Mist-specific clock and weather controls are not yet ported
- Sleep-gesture haptic option parity

### App Drawer
- iOS-style App Library mode and single-page mode
- Pinned apps and configurable pinned-row layout
- Card layout for pinned apps and app list
- Dedicated drawer search visibility, placement and appearance controls
- App-library-specific behavior and category presentation

### Recents
- Recents style selector and style preview
- Memory information / optional ZRAM information
- Per-app Recents privacy protection

### Miscellaneous
- Trust Apps manager entry and behavior (MistOS/Lineage-specific activity is absent from Rocket; intentionally not exposed as a dead control)
- Any ROM-only behavior must be adapted to public Android APIs or clearly marked unsupported.

## Porting rules

1. Reuse Rocket functionality where the behavior already matches.
2. For missing behavior, port the MistOS algorithm as directly as compatible APIs allow; translate Java/XML implementations into Rocket's Compose/Kotlin architecture where required.
3. Do not add a preference toggle until its runtime behavior is implemented and validated.
4. Keep new preference keys stable and avoid collisions with existing Rocket keys.
5. ROM/privileged-only behavior must have a safe fallback on ordinary Android.
6. Keep the MistOS/AOSP attribution and license notices beside adapted source.
