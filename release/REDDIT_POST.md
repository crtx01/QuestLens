# Draft Reddit post

Title: We developed QuestLens: a free magnifier for low-vision Quest users

I'm c0rtex, and I'm involved in developing QuestLens. We developed this small
Android app to make VR more accessible for people with low vision, starting with
the difficulty of reading small text in games.

Quest has accessibility settings, but we could not find a built-in magnifier that
solved this reading workflow on our Quest 3S. We'd like to see stronger official
accessibility support; QuestLens is a practical step we can share now.

It freezes the game image and opens a 2D magnifier. You can zoom up to 8×, pan around,
close the window and return to the game. On my Quest 3S, a double-tap on the side of
the headset already opens the lens without going through the system menu.

The app also supports double-tap to close, right-trigger click to zoom in,
left-trigger click to zoom out, a simpler English interface and hover help.
These interactions have been tested on my Quest 3S. The new preview includes an
English first-run guide explaining setup and permissions.

This is an experimental accessibility project, not a live in-game zoom overlay.
Images stay in headset memory: no cloud, OCR or account. The optional headset gesture
needs an initial developer/ADB setup and replaces the passthrough shortcut; on my
Quest 3S it also affects the physical action button. The app can restore that setting.

Local preparation already runs inside the app without repeating the initial
authorization. I still need to verify the complete flow after a headset restart
before promising that part.

I’d appreciate feedback from people with low vision: readability, trigger controls,
window size and game compatibility would be especially useful.

Download and installation: https://github.com/crtx01/QuestLens

If you'd like to support this work, optional contributions help us maintain
updates, improve compatibility, and develop new accessibility tools for people
with low vision. The repository has the support link. All app features are free.

Posting record is maintained separately in `OUTREACH.md`.
