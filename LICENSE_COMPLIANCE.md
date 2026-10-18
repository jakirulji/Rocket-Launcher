# License compliance checklist

This file documents the release checks for Rocket Launcher. It is not a legal opinion or a claim that every dependency has already been approved.

## Before distributing a source archive or APK

1. Preserve `LICENSE.txt`, `NOTICE`, original source headers, and each component's own license files.
2. Build with Git submodules enabled; the SystemUI submodule is part of the build and must be included in source review.
3. Run the `License audit` GitHub Actions workflow and inspect the generated Licensee report. Resolve unknown, unapproved, or incompatible licenses rather than automatically allowing them.
4. Verify that the generated `licenses.json` is packaged in the app and that the in-app license/notices screen can display the notices to users.
5. Review licenses for bundled prebuilt JARs, native binaries, assets, fonts, icons, and any dependencies not detected by Gradle Licensee. Gradle reports do not automatically prove that every checked-in binary or asset is covered.
6. For each modified upstream file, retain existing copyright/license notices and add a prominent modification notice where required by Apache-2.0.
7. Keep Rocket Launcher branding distinct from Lawnchair and AOSP branding; upstream names and logos are not automatically granted for product branding by the Apache license.
8. Re-run the audit whenever dependencies, submodules, bundled assets, or build variants change.

## Currently identified source-level license

- `lawnchair/src/app/lawnchair/search/algorithms/data/calculator/LICENSE`: MIT License, Copyright (c) 2018 Keelar. The original license file must remain with the source.

## Important limitations

The root `LICENSE.txt` contains the upstream Apache License 2.0 text and upstream copyright notices. `NOTICE` records the known upstream attribution and the identified MIT component. The final license inventory must be based on the exact dependencies and files shipped in each release; source-tree inspection alone is not a complete binary license audit.
