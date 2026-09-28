# QuestLens

QuestLens is a free, open-source accessibility project for people with low vision using Meta Quest.

It currently supports Quest 2, Quest 3 and Quest 3S, and there is also a 2D version of the app.

The goal is simple: make small text, menus and visual details easier to read in XR without relying on OCR, cloud processing or an account.

## Current development

QuestLens started as a frozen-image magnifier: capture the current view, open it in a large 2D window, zoom in, read what you need, then go back to the experience.

Recent development also includes:

- **Live passthrough magnification**, so real-world content can be enlarged without first freezing a frame.
- **An updated 2D app**, keeping the interface simple and usable outside the immersive viewer.
- Continued work on readability, controls, setup and low-vision usability across Quest 2, Quest 3 and Quest 3S.

The latest downloadable GitHub preview is still **0.4.3-preview**. Newer development work is being documented here before the next packaged public release.

**[Download the 0.4.3 preview APK and installer](https://github.com/crtx01/QuestLens/releases/tag/v0.4.3-preview)**
 · **[Setup instructions](docs/INSTALL.md)** · **[Report an issue](https://github.com/crtx01/QuestLens/issues)**

**Contact:** [davidperetta12@gmail.com](mailto:davidperetta12@gmail.com)

## Why I built it

I have low vision myself, and reading small text and UI elements in VR can be difficult. QuestLens grew out of that problem.

Quest has accessibility settings, but I could not find a built-in magnifier that solved the in-game reading workflow I needed on my headset. QuestLens is an independent project built around that gap while better platform-level accessibility continues to develop.

## Frozen-image magnifier

The currently published 0.4.3 preview uses the frozen-image workflow:

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

Images stay in headset memory. There are no image uploads, saved screenshots, audio, OCR, analytics or user accounts. Local ADB is used only to prepare or restore the optional headset gesture in the currently published preview.

See [privacy and permissions](docs/PRIVACY.md).

## Build

Requires JDK 17 and Android SDK 36:

```sh
./gradlew assembleDebug testDebugUnitTest lintDebug
```

On Windows use `gradlew.bat`. The APK is generated at
`app/build/outputs/apk/debug/app-debug.apk`. The source includes the Gradle wrapper.

The current published preview previously recorded **15 tests passed and zero lint errors**. See the [physical test record](docs/TESTING.md) for the test status of that build.

## Compatibility and limits

- Game/system capture restrictions still apply to the currently published frozen-image build.
- There is no protected-content bypass.
- The 0.4.3 preview was physically tested most extensively on Quest 3S; current development also targets Quest 2 and Quest 3.
- The system capture flow requires user consent for each new session in the published preview.
- QuestLens is independent and is not affiliated with Meta.

Contributions and low-vision usability feedback are welcome. Read the
[architecture notes](docs/ARCHITECTURE.md) and [contribution guide](CONTRIBUTING.md).

## Support accessibility development

If this project helps you, you can optionally [support c0rtex on Patreon](https://www.patreon.com/c/c0rtexQuestLens).

All QuestLens features are free. Support helps with testing, compatibility and continued accessibility work, but does not guarantee a release schedule.

## License

QuestLens project code is licensed under [MIT](LICENSE). Dependencies retain their own licenses, including LibADB, Conscrypt, Bouncy Castle and LGPL-licensed SPAKE2.

See [third-party notices](THIRD_PARTY_NOTICES.md).
