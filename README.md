# QuestLens

QuestLens is a free, open-source accessibility project for people with low vision using Meta Quest.

It currently supports Quest 2, Quest 3 and Quest 3S, and there is also a 2D version of the app.

The goal is simple: make small text, menus and visual details easier to read in XR without relying on OCR, cloud processing or an account.

## Current preview: 0.5.5-preview

The new preview brings live magnification for real-world passthrough into the same QuestLens APK as the frozen-image magnifier. It also refreshes the visuals, makes setup easier, and adds English, Portuguese, Spanish, French and German language options. Some text may still appear in English. Zoom controls within the QuestLens UI are still in development.

**[Download QuestLens 0.5.5-preview](https://github.com/crtx01/QuestLens/releases/tag/v0.5.5-preview)** · **[Project update](docs/PROJECT_UPDATE_2026-09-28.md)** · **[Setup instructions](docs/INSTALL.md)** · **[Report an issue](https://github.com/crtx01/QuestLens/issues)**

The 0.5.5-preview is a sideloaded debug build. Capture features require Quest system permission.

**Contact:** [davidperetta12@gmail.com](mailto:davidperetta12@gmail.com)

I also make free, by-request modified APKs with real-time zoom for compatible XR apps. If you have low vision and would like to discuss a build, contact me.

## Why I built it

I have low vision myself, and reading small text and UI elements in VR can be difficult. QuestLens grew out of that problem.

Quest has accessibility settings, but I could not find a built-in magnifier that solved the in-game reading workflow I needed on my headset. QuestLens is an independent project built around that gap while better platform-level accessibility continues to develop.

## Frozen-image magnifier

The frozen-image workflow in the 0.4.3 preview is:

1. Open QuestLens, select **START CAPTURE**, and allow the system capture prompt.
2. Select **RETURN TO GAME** and open your game.
3. With the headset shortcut configured, **double-tap the side of your headset** to open the lens. Double-tap again to close it.
4. Point at the image and briefly click the **right trigger to zoom in**, or the **left trigger to zoom out**. Hold a trigger and drag to move the image.
5. Use **− / +**, **RESET** and **BACK TO GAME** as on-screen alternatives. Point at an option for help. **END SESSION** stops capture.

Zoom steps are 1×, 2×, 4×, 6× and 8×. Shortcut configuration is kept in **SETTINGS** on the start screen, out of the image viewer.

## Install and configure

Read the [installation guide](docs/INSTALL.md). Sideloading requires developer mode.

The optional double-tap shortcut needs a one-time ADB permission/key setup; subsequent sessions attempt preparation locally on the headset with Wi-Fi enabled. It replaces the native passthrough shortcut and also affected the physical action button on the tested Quest 3S. Settings includes an option to restore it.

The optional volume-button shortcut is another way to open the lens.

## Privacy

QuestLens is local by design.

Images stay in headset memory. There are no image uploads, saved screenshots, audio, OCR, analytics or user accounts. Local ADB was used to prepare or restore the optional headset gesture in the 0.4.3 preview.

See [privacy and permissions](docs/PRIVACY.md).

## Build

Requires JDK 17 and Android SDK 36:

```sh
./gradlew assembleDebug testDebugUnitTest lintDebug
```

On Windows use `gradlew.bat`. The APK is generated at
`app/build/outputs/apk/debug/app-debug.apk`. The source includes the Gradle wrapper.

The 0.4.3 preview recorded **15 tests passed and zero lint errors**. See the [physical test record](docs/TESTING.md) for the test status of that build.

## Compatibility and limits

- Game/system capture restrictions still apply.
- There is no protected-content bypass.
- The 0.4.3 preview was physically tested most extensively on Quest 3S. The 0.5.5 preview targets Quest 2, Quest 3 and Quest 3S.
- The system capture flow requires user consent for each new capture session.
- QuestLens is independent and is not affiliated with Meta.

Contributions and low-vision usability feedback are welcome. Read the
[architecture notes](docs/ARCHITECTURE.md) and [contribution guide](CONTRIBUTING.md).

## Support accessibility development

If this project helps you, you can optionally [support c0rtex on Patreon](https://www.patreon.com/c/c0rtexQuestLens).

All QuestLens features are free. Support helps with testing, compatibility and continued accessibility work, but does not guarantee a release schedule.

## License

QuestLens project code is licensed under [MIT](LICENSE). Dependencies retain their own licenses, including LibADB, Conscrypt, Bouncy Castle and LGPL-licensed SPAKE2.

See [third-party notices](THIRD_PARTY_NOTICES.md).
