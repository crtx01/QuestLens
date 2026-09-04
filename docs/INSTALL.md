# Installation and first-time setup

QuestLens is an experimental Android app for Meta Quest. The current physical
test device is a Quest 3S. A developer-enabled headset and sideloading are required.

## Install

Build the debug APK with `./gradlew assembleDebug`, or use the APK from the release
you are testing. With Android platform-tools available:

```sh
adb devices
adb install -r QuestLens-0.4.3-preview.apk
```

If more than one device is connected, add `-s DEVICE_SERIAL` after `adb` in each
command. On Windows, use `gradlew.bat` to build. Open QuestLens from Unknown Sources.

Windows users can instead run `tools/Install-QuestLens.ps1 -Apk PATH_TO_APK` from
PowerShell. It installs the APK, grants the optional shortcut setup permission,
opens QuestLens and enables the initial ADB connection. Use `-Device SERIAL` if
multiple devices are connected, or `-SkipHeadsetShortcut` for installation only.

On a new installation, the English setup guide explains each permission and
native confirmation. It can be reopened from **SETTINGS → SETUP GUIDE**. Existing
configured installations keep their settings and do not interrupt capture with it.

## Headset double-tap: configure once

The optional shortcut uses authenticated local ADB inside QuestLens to prepare
the headset gesture. The computer is used for initial setup. Subsequent capture
sessions attempt preparation on the headset itself using wireless debugging TLS.
Restart recovery still needs a physical test before it is advertised as confirmed.

Run once after installation, while connected to the headset:

```sh
adb shell pm grant dev.questlens android.permission.WRITE_SECURE_SETTINGS
adb tcpip 5555
```

Then, in QuestLens:

1. Open **SETTINGS** and select **ALLOW WINDOW**, if shown. Allow displaying over
   other apps in the system screen, then return to QuestLens.
2. Select **SET UP SHORTCUT**.
3. Accept the native wireless-debugging network prompt and select **Always allow
   on this network**. Also accept the app's separate debugging-key prompt and
   select **Always allow**. The app generates its own key on the headset.
4. Wait for **Shortcut ready**. If a prompt took too long, select **RECONNECT**.
5. Select **DONE**, then **START CAPTURE**. Allow the system capture prompt.
6. Select **RETURN TO GAME**, then open your game. Double-tap the side of the headset
   to open QuestLens; double-tap again to close it.

The gesture replaces the native passthrough double-tap shortcut. On the tested
Quest 3S it also disables passthrough switching with the physical action button.
To undo this, end the capture session, open **SETTINGS**, and select
**RESTORE PASSTHROUGH SHORTCUT**. Rebooting also clears the temporary sensor override;
QuestLens will only try to reapply it when capture starts with the option enabled.

Keep Wi-Fi enabled for local preparation. Images are not sent over this connection.
Each new capture session requires normal system capture consent. Installing an
update signed with the same key preserves app configuration; uninstalling clears it.

## Optional volume shortcut

This alternative opens QuestLens by holding both headset volume buttons for about
three seconds. It uses an Android activity shortcut, not an AccessibilityService.
In **SETTINGS → VOLUME SHORTCUT**, choose **QuestLens — open window** if available.
During development, the target can also be set once using:

```sh
adb shell settings put secure accessibility_shortcut_target_service dev.questlens/dev.questlens.LaunchPanelActivity
```

Accept the native shortcut confirmation. Availability and layout vary by Horizon
version. Do not enable a key-filtering accessibility service: QuestLens does not
include or need one.

## Troubleshooting

- **No recent image:** return to the game, wait briefly, then reopen the lens.
  Some content cannot be captured. The app does not bypass protected content.
- **Double-tap does nothing:** check Wi-Fi and window permission; open Settings
  and reconnect. Check that capture is running. Use the volume shortcut as a fallback.
- **System gesture not restored:** try Restore again with Wi-Fi enabled, or restart
  the headset. A disabled preference is not proof that restoration succeeded.
- **Debugging authorization revoked or expired:** repeating enrollment may be
  necessary. This prototype's reconnect path does not yet include a key-reset wizard.
- **Trigger zoom unavailable:** use the on-screen − / + controls. Controller IDs were
  observed on a Quest 3S; another Horizon version may deliver different input.

For setup help, questions or accessibility suggestions, contact
[davidperetta12@gmail.com](mailto:davidperetta12@gmail.com).
