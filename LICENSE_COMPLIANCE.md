# License compliance checklist

This file documents release checks for Rocket Launcher. It is not a legal opinion or a claim that every dependency has already been approved.

## Before distributing a source archive or APK

1. Preserve `LICENSE.txt`, `COPYING`, `NOTICE`, original source headers, and each component's own license files.
2. The inherited Lawnchair/AOSP-derived code retains its applicable Apache-2.0 notices. Dragon Launcher-derived Digital Wellbeing code is GPLv3-covered; see `THIRD_PARTY_LICENSES/DRAGON_LAUNCHER.md`.
3. When distributing a combined covered work, review and satisfy GPLv3 requirements, including corresponding source code, license text, copyright notices, modification notices, and any other applicable obligations.
4. Build with Git submodules enabled; the SystemUI submodule is part of the build and must be included in source review.
5. Run the `License audit` GitHub Actions workflow and inspect the generated Licensee report. Resolve unknown, unapproved, or incompatible licenses rather than automatically allowing them.
6. Verify that the generated `licenses.json` is packaged in the app and that the in-app license/notices screen can display the notices to users.
7. Review licenses for bundled prebuilt JARs, native binaries, assets, fonts, icons, and any dependencies not detected by Gradle Licensee. Gradle reports do not automatically prove that every checked-in binary or asset is covered.
8. For each modified upstream file, retain existing copyright/license notices and add a prominent modification notice where required by its license.
9. Keep Rocket Launcher branding distinct from Lawnchair and AOSP branding; upstream names and logos are not automatically granted for product branding.
10. Re-run the audit whenever dependencies, submodules, bundled assets, or build variants change.

## Currently identified source-level licenses

- Lawnchair/AOSP-derived code: applicable Apache License 2.0 notices and terms; see `LICENSE.txt`.
- Dragon Launcher Digital Wellbeing: GNU GPL version 3; see `COPYING` and `THIRD_PARTY_LICENSES/DRAGON_LAUNCHER.md`.
- Calculator data/algorithm component: MIT License, Copyright (c) 2018 Keelar. The original license file must remain with the source.

## Important limitations

The root `LICENSE.txt` contains the upstream Apache License 2.0 text and upstream copyright notices. `COPYING` contains the GPLv3 license text. `NOTICE` records known upstream attribution. The final license inventory must be based on the exact files, dependencies, assets, and submodules shipped in each release; source-tree inspection alone is not a complete binary license audit.
