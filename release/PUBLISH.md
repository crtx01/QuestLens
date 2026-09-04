# Publishing the repository and preview

The creator authorized publication. Current links and results are in `OUTREACH.md`.

Prepare the local bundle after a successful build using
`tools/Package-Preview.ps1`. It includes the preview APK, SHA256 sums, installer,
project source and complete pinned SPAKE2 source (including the native submodule).
Confirm the support page is published and working before distributing the APK.

1. Complete the physical tests in `docs/TESTING.md`, especially reboot recovery.
   Update the README and release/post drafts with the actual results.
2. Review the included MIT license for QuestLens and retain the dependency notices
   in `docs/licenses`. Third-party components keep their own licenses.
3. Create an empty GitHub repository. Add the project sources, Gradle wrapper, docs
   and release drafts. `.gitignore` excludes build output, local diagnostics and keys.
4. Replace the repository-link placeholder in `release/REDDIT_POST.md`.
5. For a public preview, attach the APK, SHA256SUMS, project source archive and the
   corresponding SPAKE2 dependency source archive. Keep its LGPL notices and source
   available with the binary. All dependency versions are fixed in the Gradle files.
6. For a production release, create a private signing key and sign the release APK.
   Keep the key outside Git and back it up privately. A release signed with a different
   key cannot update this debug installation without uninstalling it first.

Suggested repository description:

> A local 2D magnifier for low-vision Quest users: freeze a game image, zoom and return.

Suggested topics: `accessibility`, `low-vision`, `meta-quest`, `android`, `kotlin`, `magnifier`.

After creating your repository, standard Git commands are:

```sh
git add .
git commit -m "Add QuestLens experimental magnifier"
git branch -M main
git remote add origin YOUR_REPOSITORY_URL
git push -u origin main
```

Review `git diff --cached` before committing. Do not include personal headset logs,
screenshots with private content or the `artifacts` directory in the source repository.
