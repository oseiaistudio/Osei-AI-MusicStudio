# Free Development & Testing Setup Guide (Budget: GH₵0)

Welcome to the **Osei AI Music Studio** local development setup guide!

This guide explains how to build, run, and test the entire Android application **100% free of charge**, without:
- Entering credit card or bank details.
- Enabling billing on Google Cloud or Firebase.
- Buying third-party API subscriptions.
- Exposing secrets or API keys.

---

## 1. How Free Mode Works

In Phase 3, the application defaults to **Free Development Mode** (`DisabledMusicGenerationService`).

In this mode:
* **Zero Network Spend:** The app never calls external generative AI APIs, billable Cloud Functions, or paid music services.
* **Full Local Feature Exploration:** You can test all user interface screens:
  - Account authentication and profile management.
  - Exploring Ghanaian and international musical genres (Highlife, Afrobeats, Hiplife, Gospel, Amapiano, etc.).
  - Song creation: writing custom titles, lyrics, selecting vocal preferences and instrumental toggles.
  - Local database caching using Android Jetpack Room (`StudioDatabase`).
  - Cloud synchronization and state tracking using Firebase Firestore.
  - Media3 audio player interface and waveform controls.
* **Truthful UI State:** When you press **Generate Song**, the app registers your track arrangement in your studio library in `queued` status, with the transparent notice:
  > *"Free Development Mode: Song parameters registered. Real AI music generation requires a separately deployed backend and explicit owner billing approval."*
* **No Fake Audio:** The app will never play a fake rain sound or mark a song completed without a real backend response.

---

## 2. Prerequisites

Everything you need is already included in your developer container:
1. **JDK 17 / 21**
2. **Android SDK (Platform 36)**
3. **Gradle**
4. **Firebase CLI & Node.js** (for running local Firestore emulators)

---

## 3. How to Build the App

To compile the Android app from the terminal, run:

```bash
gradle assembleDebug
```

*(Note: In this cloud environment, do NOT use `./gradlew`. Always invoke `gradle` directly).*

The compiled APK will be generated at:
```
app/build/outputs/apk/debug/app-debug.apk
```

---

## 4. How to Run Local Automated Tests

### A. Run all JVM Unit Tests (Free & Offline)
To run all 46+ unit tests covering ViewModel logic, input validation, audio URL verification, and the Room database:

```bash
gradle :app:testDebugUnitTest
```

### B. Run Firestore Security Rules Tests (Free Local Emulator)
To verify that Firestore security rules block unauthorized credit and subscription modifications:

```bash
firebase emulators:exec --only firestore "node --test firestore.test.js"
```

All 10 security tests will execute inside a temporary local Firestore emulator process. No cloud resources or network calls are made.

---

## 5. Environment & Secrets Configuration

The project uses the Secrets Gradle Plugin with `.env` files:
* `.env.example` provides template variables.
* **Never commit real secrets or API keys to git.**
* For free local development, **no `.env` file or API keys are required**. The app functions completely out-of-the-box.

---

## 6. Development Workflow Tips

1. **Resetting Test Data:**
   You can reset local app state anytime by clearing app storage in the Android settings or re-running the test suite.
2. **Testing Song Creation:**
   Navigate to the **Generator** tab, enter a title (e.g., *"Accra Sunset Vibes"*), select *"Ghanaian Highlife"*, input lyrics, and click **Generate**. Notice how the track appears in your library with `queued` status and zero charges incurred.
3. **Cancellation Testing:**
   On the generation progress screen, you can press **Cancel Request** to cancel the queued track cleanly at any time.
