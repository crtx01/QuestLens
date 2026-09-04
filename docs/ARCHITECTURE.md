# Architecture

QuestLens is one Kotlin/Compose Android module. It launches a normal resizable 2D
activity so the user can close it and resume an immersive game.

- `CaptureService` owns the user-approved MediaProjection, fixed 1920×1080 surface,
  ImageReader and worker thread. It drains images and copies at most five per second.
- `FrameRepository` owns the latest frame and a bounded in-memory history. Opening
  the lens freezes a copy before displaying the panel. Closing resumes collection.
- `HeadsetTapShortcut` listens to the vendor `oculus.sensor.doubletap` sensor during
  capture. A fresh gesture opens or closes only QuestLens's task.
- `TapEventGate` rejects the retained registration event, duplicate/out-of-order
  timestamps and rapid repeats without assuming the vendor and Android clocks match.
- `AutonomousTap` serializes setup/restoration and tracks readiness separately from
  the saved preference. `LocalAdb` uses the app's own authorized RSA key and local TLS.
- `ZoomViewer` receives ordinary Android pointer events inside the image surface.
  Quick controller clicks change zoom; drags pan. Unknown pointers do not select an
  arbitrary hand. Button input and other apps' controls are not intercepted.
- `LaunchPanelActivity` is an optional native Android volume-shortcut destination.
  It is an activity, not an AccessibilityService.

No OpenXR overlay, root, input filter or hidden-API bypass is used. Sensor availability,
VrShell behavior, Android window policy and local wireless debugging are platform
dependencies, not guarantees across all Quest firmware versions.
