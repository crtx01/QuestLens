# Contributing to QuestLens

Bug reports, code improvements and feedback from low-vision users are welcome.
Use plain language and describe the reading task you were trying to complete.
Questions and accessibility suggestions can also be sent to
[davidperetta12@gmail.com](mailto:davidperetta12@gmail.com).

For a bug report, include the Quest model, Horizon version, QuestLens version,
steps to reproduce, expected result and actual result. Note whether the volume
shortcut, headset gesture or on-screen button was used. Avoid uploading complete
device logs, private images or debugging/signing keys.

For code changes:

1. Keep the reading flow simple, with large controls and clear English help.
2. Keep captured images local and transient.
3. Do not introduce an AccessibilityService, input filter, root requirement or a
   permanent overlay that blocks the underlying game.
4. Build with JDK 17 and run `./gradlew assembleDebug testDebugUnitTest lintDebug`.
5. Include a physical-test description for changes affecting controller input,
   sensor gestures, MediaProjection or returning to the game.

Do not infer cross-device compatibility from a simulator or an injected ADB event.
Document platform-specific behavior and fail clearly when optional preparation fails.
