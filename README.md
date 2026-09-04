# QuestLens

A local magnifier for small text in Quest games, built with low-vision users in mind.

We developed QuestLens to make VR more accessible for people with low vision.
Quest has official accessibility settings, but we could not find a built-in
magnifier that solved this in-game reading workflow on our Quest 3S. This project
addresses that gap while we continue to advocate for better official support.

QuestLens freezes the game image, opens a large 2D window and lets you zoom in to
read. Close the window to return to your game. **This is a frozen-image magnifier,
not a live OpenXR zoom overlay.**

**By c0rtex.** Experimental Android app, currently tested on Quest 3S. Double-tap
opening/closing, trigger zoom, dragging and hover help were confirmed in 0.4.1-dev.
Version 0.4.3-preview adds a direct contact option alongside the first-run setup
guide and optional support links.
Full restart recovery is not yet verified.

**[Download the 0.4.3 preview APK and installer](https://github.com/crtx01/QuestLens/releases/tag/v0.4.3-preview)**
 · **[Setup instructions](docs/INSTALL.md)** · **[Report an issue](https://github.com/crtx01/QuestLens/issues)**

**Contact us:** [davidperetta12@gmail.com](mailto:davidperetta12@gmail.com) for
questions, accessibility suggestions and feedback.

## Support accessibility development

If this work helps you, please consider [supporting c0rtex on Patreon](https://www.patreon.com/c/c0rtexQuestLens).
Support helps us maintain updates, improve compatibility and setup, and develop
new accessibility tools for people with low vision. Every QuestLens feature is
free. Contributions are optional and do not guarantee a release schedule.

## Use it

1. Open QuestLens, select **START CAPTURE**, and allow the system capture prompt.
2. Select **RETURN TO GAME** and open your game.
3. With the headset shortcut configured, **double-tap the side of your headset**
   to open the lens. Double-tap again to close it.
4. Point at the image and briefly click the **right trigger to zoom in**, or the
   **left trigger to zoom out**. Hold a trigger and drag to move the image.
5. Use **− / +**, **RESET** and **BACK TO GAME** as on-screen alternatives.
   Point at an option for help. **END SESSION** stops capture.

Zoom steps are 1×, 2×, 4×, 6× and 8×. Shortcut configuration is kept in **SETTINGS**
on the start screen, out of the image viewer.

## Install and configure

Read the [installation guide](docs/INSTALL.md). Sideloading requires developer mode.
The optional double-tap shortcut needs a one-time ADB permission/key setup; subsequent
sessions attempt preparation locally on the headset with Wi-Fi enabled. It replaces
the native passthrough shortcut and also affected the physical action button on the
tested Quest 3S. Settings includes an option to restore it.

The optional volume-button shortcut is another way to open the lens.

## Local by design

Images stay in headset memory. No image uploads, saved screenshots, audio, OCR,
analytics or account. Local ADB is used only to prepare or restore the optional
headset gesture. See [privacy and permissions](docs/PRIVACY.md).

## Build

Requires JDK 17 and Android SDK 36:

```sh
./gradlew assembleDebug testDebugUnitTest lintDebug
```

On Windows use `gradlew.bat`. The APK is generated at
`app/build/outputs/apk/debug/app-debug.apk`. The source includes the Gradle wrapper.

Current automated checks: **15 tests passed, zero lint errors**. See the
[physical test record](docs/TESTING.md) for what has and has not been confirmed.

## Limits and compatibility

- Game/system capture restrictions still apply; there is no protected-content bypass.
- No native accessibility service or global controller-input filter is installed.
- Trigger IDs and the double-tap sensor were observed on Quest 3S. Other firmware
  or devices may differ; on-screen controls remain available.
- The system requires capture consent for each new session. No automatic capture
  starts at boot. Recovery of the shortcut after a full restart is still a test item.
- This project is independent and is not affiliated with Meta.

Contributions and low-vision usability feedback are welcome. Read the
[architecture notes](docs/ARCHITECTURE.md) and [contribution guide](CONTRIBUTING.md).
You can also email [davidperetta12@gmail.com](mailto:davidperetta12@gmail.com).

## License

QuestLens project code is licensed under [MIT](LICENSE). Dependencies retain their
own licenses, including LibADB, Conscrypt, Bouncy Castle and LGPL-licensed SPAKE2.
See [third-party notices](THIRD_PARTY_NOTICES.md).
