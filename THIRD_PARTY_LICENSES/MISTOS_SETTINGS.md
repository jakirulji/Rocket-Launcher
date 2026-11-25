# MistOS Launcher Settings UI algorithm

- **Project:** MistOS
- **Source:** https://github.com/Project-Mist-OS/packages_apps_Launcher3/tree/17.0
- **License:** Apache License 2.0
- **Adapted component:** `ExpressiveShape` radial path-generation algorithm, adapted from Java/Android Canvas to Jetpack Compose Canvas for Rocket Launcher.
- **Original source file:** `src/com/android/launcher3/settings/expressive/ExpressiveShape.java`
- **Rocket adaptation:** `rocketlauncher/src/app/lawnchair/ui/preferences/components/layout/MistExpressiveShape.kt`

The Rocket adaptation retains the original lobe-count/depth parameters and radial point-generation approach. The implementation is not a verbatim Java copy; it translates the same algorithm to Compose.

The app-drawer Focus Mode controller is also adapted from MistOS Launcher3's `src/com/android/launcher3/popup/FocusModeController.kt`. That source carries the Android Open Source Project Apache-2.0 copyright header, which is retained in Rocket's copied controller. Rocket adds popup lifecycle integration and the corresponding preference/resource wiring.

The five expressive category glyph vectors under `res/drawable/ic_settings_*_mist.xml` are adapted from MistOS Launcher3's expressive category glyphs. Their original headers identify Copyright (C) 2025 crDroid Android Project and SPDX-License-Identifier: Apache-2.0; those notices are retained in each vector resource.

Copyright (C) 2026 MistOS

The original Apache-2.0 license text is included in the repository's root `LICENSE.txt`. Changes made for Rocket Launcher are documented in the adapted source file.
