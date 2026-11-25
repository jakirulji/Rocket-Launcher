# MistOS Launcher Settings UI algorithm

- **Project:** MistOS
- **Source:** https://github.com/Project-Mist-OS/packages_apps_Launcher3/tree/17.0
- **License:** Apache License 2.0
- **Adapted component:** `ExpressiveShape` radial path-generation algorithm, adapted from Java/Android Canvas to Jetpack Compose Canvas for Rocket Launcher.
- **Original source file:** `src/com/android/launcher3/settings/expressive/ExpressiveShape.java`
- **Rocket adaptation:** `rocketlauncher/src/app/lawnchair/ui/preferences/components/layout/MistExpressiveShape.kt`

The Rocket adaptation retains the original lobe-count/depth parameters and radial point-generation approach. The implementation is not a verbatim Java copy; it translates the same algorithm to Compose.

Copyright (C) 2026 MistOS

The original Apache-2.0 license text is included in the repository's root `LICENSE.txt`. Changes made for Rocket Launcher are documented in the adapted source file.
