# QuestLens 0.4.3-preview — experimental preview

By c0rtex. A local 2D magnifier for small text in Quest games, tested on Quest 3S.

We developed QuestLens to make VR more accessible for people with low vision.
Optional [support](https://www.patreon.com/c/c0rtexQuestLens) helps maintain updates,
improve compatibility, and develop new accessibility tools. All app features are free.

## Changes

- Headset double-tap opens and closes the lens window.
- Click the image with the right trigger to zoom in or the left to zoom out.
  Hold and drag to pan. Zoom steps: 1×, 2×, 4×, 6× and 8×.
- Simplified English interface with hover help.
- First-run guide explains the permissions, native confirmations and capture flow.
- Windows installation helper handles the initial ADB setup commands.
- Optional support page on the home screen; all app features remain free.
- Direct **CONTACT US** button for questions, feedback and accessibility suggestions.

The opening/closing gesture, trigger zoom, panning and hover help were confirmed
by the tester on Quest 3S in 0.4.1-dev. Full headset restart recovery, other models
and long-session compatibility remain unverified. See `docs/TESTING.md` for the
new setup/support UI's physical test status.

Read `docs/INSTALL.md` before enabling the optional gesture. Initial developer/ADB
setup is required. It replaces the passthrough shortcut and also affects the
physical action button on the tested Quest 3S. Images remain in headset memory.

This APK is a debug preview for sideload testing. It is not a store production
build. A future build signed with a different key will require uninstalling the
old app and setting it up again.

Assets include the APK, SHA256SUMS.txt, Windows installer, installation guide,
QuestLens source archive, and SPAKE2 dependency source archive with its notices.

Contact: [davidperetta12@gmail.com](mailto:davidperetta12@gmail.com)
