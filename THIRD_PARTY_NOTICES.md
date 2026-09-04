# Third-party notices

QuestLens 0.4.3-preview by c0rtex uses the following libraries. Project code is MIT;
each dependency retains its own copyright and license. Full notices are in
`docs/licenses/` and the corresponding upstream sources.

| Component | Version | License and source |
| --- | --- | --- |
| LibADB Android | 3.1.1 | Apache-2.0 option of its dual license; additional BSD-3-Clause/MIT notices. Copyright Muntashir Al-Islam and contributors, including Cameron Gutman. [Source](https://github.com/MuntashirAkon/libadb-android/tree/3.1.1). |
| Conscrypt Android | 2.5.3 | Apache-2.0 and bundled native notices. Preserve LICENSE, NOTICE, Harmony and Netty notices in `docs/licenses`. [Source](https://github.com/google/conscrypt/tree/2.5.3). |
| Bouncy Castle (bcpkix, bcprov, bcutil, jdk15to18) | 1.81 | Bouncy Castle permissive license; see `bouncycastle-LICENSE.txt`. [Source](https://github.com/bcgit/bc-java/tree/r1rv81). |
| SPAKE2 Android, transitively through LibADB | 2.2.1 | LGPL-3.0; includes SPAKE2 C native source and additional upstream notices. [Source](https://github.com/MuntashirAkon/spake2-java/tree/2.2.1). |
| AndroidX / Jetpack Compose | Versions resolved by Gradle | Apache-2.0. [Source](https://android.googlesource.com/platform/frameworks/support/). |
| Kotlin runtime and coroutines | Versions resolved by Gradle | Apache-2.0. [Kotlin](https://github.com/JetBrains/kotlin), [coroutines](https://github.com/Kotlin/kotlinx.coroutines). |

The local preview bundle includes `spake2-java-2.2.1-source.zip`, including its pinned
native submodule, build scripts and licenses. Upstream tag `2.2.1` resolves to
`7615ddd680b990e14513ebb66eac4cb0dbf82464`; its `spake2-c` submodule resolves to
`0d15933e5ba3e662cb01245a7ac0dc9fca3eac31`. The upstream Android module still declares
an internal version of 2.2.0; the distributed Gradle/JitPack dependency uses tag 2.2.1.

## Rebuilding with a modified LGPL component

The complete QuestLens source and build scripts are provided with the preview.
You may modify or replace the LGPL component and rebuild/relink the app. There is
no app-level signature check, licensing server or restriction against debugging
such modifications. Use the included SPAKE2 source (and Android SDK/NDK/CMake
versions required by its Gradle project) to build a modified Android AAR. Replace
the transitive module in `app/build.gradle.kts`, for example:

```kotlin
implementation("com.github.MuntashirAkon:libadb-android:3.1.1") {
    exclude(group = "com.github.MuntashirAkon.spake2-java", module = "spake2-android")
}
implementation(files("libs/spake2-modified.aar"))
```

Check `./gradlew :app:dependencies --configuration debugRuntimeClasspath` for the
resolved coordinate and adjust the exclusion if it differs. Build with
`./gradlew assembleDebug`. Android requires uninstalling an existing app first
when the replacement APK is signed with a different key; that clears app settings.
Publish the corresponding dependency source alongside each distributed APK.

QuestLens does not include code from Oculus Wireless ADB; that project was consulted
as a reference for preparing wireless debugging locally on the headset.
