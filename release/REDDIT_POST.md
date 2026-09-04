# Published Reddit post

Title: We developed QuestLens: a free magnifier for low-vision Quest users

I'm c0rtex, and I'm involved in developing QuestLens. We built it to make VR more
accessible for people with low vision, starting with one basic problem: text, HUD
elements and physical details can be too small to read, while Quest does not provide
a system-wide magnifier that solved this workflow for us.

## What QuestLens does

QuestLens freezes the current headset view and opens that image in a large 2D panel.
It works with rendered game content and can also be opened from passthrough to inspect
the physical world. You can zoom up to 8x, pan around the image, close the panel and
immediately return to the game or passthrough.

It is a frozen-image magnifier, not live zoom. For a game, that means pausing visually
to read a menu, subtitle, map, inventory item or HUD element. In passthrough, it can
be used to inspect a label, object or other static detail. You then close it and
continue normally.

## How the flow works

1. Install the APK and open QuestLens from Unknown Sources.
2. Select **Start Capture** and accept the Quest screen-capture permission.
3. Select **Close Panel and Play**, then open your game or passthrough.
4. With the optional shortcut configured, double-tap the side of the headset whenever
   you need the magnifier.
5. Use the right controller trigger to zoom in and the left trigger to zoom out. Hold
   and drag to pan. Large on-screen + and - controls are also available, with hover
   help.
6. Double-tap again or select **Return to Game** to close the lens and continue where
   you stopped.

After the one-time shortcut setup, the normal magnifier workflow runs on the headset
without a PC. The preview includes an English first-run guide explaining each
permission and setup step.

## Current status

We tested opening and closing the lens, trigger zoom, dragging and the complete
reading flow on Quest 3S. Images remain in headset memory: there is no cloud upload,
OCR, telemetry or QuestLens account.

The preview is experimental. It requires sideloading, and the optional one-handed
headset gesture requires initial developer/ADB configuration. That gesture replaces
the native passthrough double-tap shortcut and affected the physical action button on
our Quest 3S; the original behavior can be restored from Settings. Recovery after a
complete headset restart and compatibility with Quest 2, Quest 3 and Quest Pro still
need broader testing.

## What we want to build next

We want to explore live magnification, a supported one-handed system shortcut, better
contrast and reading controls, and more tools requested by people with low vision.
Community reports about specific games, menus, passthrough tasks and different Quest
models will guide that work.

## Download, source and setup

https://github.com/crtx01/QuestLens

Contact us: davidperetta12@gmail.com

QuestLens is free and open source. If you'd like to support continued updates and new
low-vision accessibility tools, the repository includes an optional support link.
Every current app feature is available without paying.

Feedback is especially useful if you include your Quest model, the game or passthrough
task, what you were trying to read, and whether the shortcut and controls worked for
you. QuestLens is independent and is not affiliated with Meta.

The publication and replies are recorded separately in `OUTREACH.md`.
