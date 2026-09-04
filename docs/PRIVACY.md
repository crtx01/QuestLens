# Privacy and permissions

QuestLens processes captured images on the headset. It has no analytics, advertising,
cloud account, OCR service, audio recording or image-upload feature.

The optional **SUPPORT US** buttons open the creator's verified support pages in an
external browser. Those providers handle their own accounts, cookies and payments;
QuestLens does not receive payment credentials or track whether you contributed.

Captured frames and a short image history stay in process memory. The app does not
save screenshots to storage. Ending capture releases its buffers; the operating
system controls when memory previously used by UI snapshots is reclaimed.

## Permissions

| Permission | Purpose |
| --- | --- |
| MediaProjection consent and foreground service | Capture the current display during a user-approved session. |
| Display over other apps | Allow the normal 2D window to open from the headset gesture. No overlay window is drawn. |
| WRITE_SECURE_SETTINGS | Optional, explicit one-time setup grant; enables local wireless debugging for shortcut preparation. |
| INTERNET | Optional local ADB transport to `127.0.0.1` and discovery of the headset's TLS port. No external image transport. |

The app's private ADB credential is generated on the headset and stored in its
private no-backup directory. It is not copied from a computer or bundled in an APK.
The system authorization grants debugging access, which is broader than a normal
app permission. QuestLens's implementation exposes no remote terminal and uses only
fixed commands to query, suspend or restore the VrShell sensor state.

Build/debug logs contain lifecycle events, frame dimensions and timing. The temporary
controller-input diagnostic used during development has been removed. Do not upload
full device logs or your private signing/debugging keys when reporting a problem.
