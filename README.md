# Kestrel Study Assistant

Android native starter project (Kotlin) inspired by the supplied video: a small floating bubble, expandable menu, study-answer flow, detailed explanations, ChatGPT handoff, and a privacy/compact mode.

## Implemented
- `SYSTEM_ALERT_WINDOW` permission flow and `TYPE_APPLICATION_OVERLAY` bubble.
- Foreground service with an ongoing notification while the bubble is active.
- Draggable bubble and expandable floating menu.
- Privacy mode: collapses the menu and changes the bubble to a subdued dot. It is a visual privacy feature, not stealth or exam-monitoring evasion.
- AI answer and detailed explanation for user-entered/shared practice questions through the OpenAI Chat Completions API.
- ChatGPT handoff through Android share sheet; choose the ChatGPT app if installed.
- Local model, API key, and target-score (KKM) preferences.
- GitHub Actions workflow to build a debug APK and publish a signed release when repository secrets are configured.

## Not implemented / not inferred from the video
- No automatic reading of another app's screen, Accessibility scraping, auto-tapping, or answer injection into a quiz.
- No lock-screen overlay, reboot persistence, or hidden operation. The video does not establish these capabilities.
- “Auto nilai di atas KKM” is represented as a target-score setting used to frame study assistance, not a guarantee or an automated grade manipulation feature.

## Build locally
Requirements: Android Studio with JDK 17 and Android SDK 35.

1. Open this folder in Android Studio.
2. Allow Gradle sync.
3. Run the `app` configuration on an Android 8.0+ device.
4. In the app, grant **Display over other apps**, then tap **Jalankan bubble**.
5. Open **Buka asisten AI**, enter your own OpenAI API key and model (default `gpt-4o-mini`), then save.

The API key is stored in the app's private SharedPreferences on-device. Do not commit keys to Git, screenshots, or issue reports. For a public production release, move API requests behind your own authenticated backend rather than shipping a reusable provider key in a client app.

## GitHub release
The workflow `.github/workflows/android-release.yml` builds and publishes a signed release APK when you push a tag such as `v1.0.0`. Configure these repository secrets with a signing keystore:
- `ANDROID_KEYSTORE_BASE64`
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`

The workflow fails if signing secrets are missing. The workflow is a starter CI pipeline and has not been executed in this environment; verify the build in GitHub Actions before distributing the APK.
