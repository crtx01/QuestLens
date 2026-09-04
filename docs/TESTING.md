# Validation

Run:

```sh
./gradlew assembleDebug testDebugUnitTest lintDebug
```

Current local result: 15 unit tests passed, lint has zero errors. Tests cover RGBA
packing, view geometry, bounded temporal history, vendor timestamp clock differences,
gesture cooldown, left/right trigger discrimination, pointer jitter, drag cancellation
and zoom boundaries.

## Physical test record

| Behavior | Status |
| --- | --- |
| Captured game image opens without the system menu | Confirmed by the tester. |
| Closing the window returns to the game | Confirmed by the tester. |
| Volume-button shortcut | Confirmed by the tester. |
| Headset double-tap opens the lens through local ADB preparation | Confirmed by the tester in 0.4.0-dev. |
| Local TLS preparation and restoration without repeating authorization | Confirmed in device logs. |
| Separate right/left controller events | Observed on Quest 3S: virtual device IDs `0x100002` / `0x100001`. |
| Double-tap closes the lens, trigger zoom and hover help in 0.4.1-dev | Confirmed by the tester on Quest 3S. |
| First-run guide and support dialog in 0.4.2-preview | Installed through Wi-Fi ADB; all four guide pages and the support dialog were opened and inspected through device UI automation. New-user enrollment and external payment completion have not been retested. |
| Contact option in 0.4.3-preview | Debug build, unit tests and Android lint passed. Opening the headset email client remains a physical test item. |
| Autonomous preparation after a full headset restart | Pending. |
| Other headsets, Horizon versions and long sessions | Pending. |

## Test the current UI

1. Start and allow capture, return to a game, then double-tap to open the lens.
2. Point at the image. Brief right-trigger clicks should step 1× → 2× → 4× → 6× → 8×;
   left-trigger clicks should step down. Holding and dragging should pan without zoom.
3. Click the visible buttons with either hand. They should perform only their normal
   actions. Point at each option for about half a second to show its help bubble.
4. Double-tap to close, wait in a different scene, and double-tap to open again.
   Confirm a new image, correct controller clicks and successful return to the game.
5. End the session. Settings should be available on the start screen; the frozen
   image viewer should not display shortcut setup options.
6. Save and exit the game before reboot testing. Restart the headset, disconnect USB,
   perform no PC setup commands, start capture again and repeat steps 1–4.

The ADB-only test receiver is included in debug builds and requires Android's DUMP
permission. Release builds do not include it. Its open/close actions do not prove a
physical gesture worked; record user feedback and sensor events separately.
