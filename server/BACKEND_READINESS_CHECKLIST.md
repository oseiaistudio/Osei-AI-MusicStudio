# Backend Readiness Checklist for Real AI Music Generation

This checklist outlines the prerequisites, technical boundaries, and approval gates required before transitioning **Osei AI Music Studio** from Free Development Mode to a live neural music generation backend.

---

## Category 1: Can Be Completed Locally for Free (Current Phase)

* [x] **Zero-Trust Audio URL Validator:** Client-side `AudioReferenceValidator` rejects null, non-HTTPS, rain-sound, and placeholder audio assets.
* [x] **Truthful State Modeling:** Songs initialize in `queued` status without claiming completed generation.
* [x] **Disabled Generation Service:** `DisabledMusicGenerationService` enforces Free Mode (GH₵0 spend) by default.
* [x] **Firestore Security Rules:** Client elevation of credits, subscriptions, song completion, and audio URLs is strictly denied.
* [x] **Automated Test Suites:** 51 local unit and emulator tests validating authentication, validation, state transitions, and database DAOs.
* [x] **ExoPlayer Lazy Loading:** Media3 audio player initializes on demand, eliminating main-thread startup stalls (`SurfaceSyncGroup` timeout fixed).
* [x] **Job Cancellation Support:** UI allows users to cancel requests without leaving loading spinners stuck.
* [x] **Local Room Persistence:** `StudioDatabase` caches song drafts, user profiles, and pending sync states offline.

---

## Category 2: Requires Further Official Documentation Verification

* [ ] **Vertex AI Lyria General Availability Status:** Confirm whether `lyria-3-pro-preview` and `lyria-3-clip-preview` are available without an enterprise-tier Google Cloud allowlist.
* [ ] **Exact Model Resource Path:** Verify the canonical Vertex AI publisher endpoint format (e.g., `projects/{project}/locations/us-central1/publishers/google/models/lyria-3-pro-preview`).
* [ ] **Per-Song Output Limits:** Verify whether Lyria 3 Pro supports arbitrary lyric line structures or requires specific timestamped bracket notations (`[Verse]`, `[Chorus]`, `[Bridge]`).
* [ ] **Supported Audio Encoding Options:** Confirm whether Vertex AI returns raw audio buffers (PCM 48kHz WAV) or pre-encoded MP3 streams, to determine if the Cloud Function needs an FFmpeg transcoding layer.
* [ ] **Regional Endpoint Latency:** Evaluate synthesis duration (estimated 15–45 seconds) to ensure Cloud Function HTTP timeouts (default 60s) are configured to 300s.

---

## Category 3: Requires Credentials or Provider-Access Verification

* [ ] **Firebase Admin SDK Service Account:** Verify that the backend hosting environment has a dedicated service account with least-privilege IAM roles.
* [ ] **Vertex AI IAM Role:** Assign `roles/aiplatform.user` to the Firebase Functions runtime service account in the GCP Console.
* [ ] **Firebase Storage Bucket CORS:** Configure `cors.json` on the audio bucket (`gs://<project-id>.firebasestorage.app`) to permit streaming playback via Android `Media3AudioPlayer`:
  ```json
  [
    {
      "origin": ["*"],
      "method": ["GET", "HEAD"],
      "responseHeader": ["Content-Type", "Content-Range", "Accept-Ranges"],
      "maxAgeSeconds": 3600
    }
  ]
  ```
* [ ] **Idempotency Key Strategy:** Add `idempotencyKey` field in Firestore transactions to deduplicate identical generation requests from the same user within 60 seconds.

---

## Category 4: Requires Billing or Paid Deployment (MANDATORY OWNER APPROVAL)

> **CRITICAL STOP GATE:** Do NOT execute any item in this category without explicit, written owner authorization and budget confirmation.

* [ ] **Link Google Cloud Billing Account:** Upgrade Firebase project from Spark (Free) to Blaze (Pay-as-you-go).
* [ ] **Set Google Cloud Budget Alert:** Configure a hard spending alert at **$10.00 / month** in the Google Cloud Console to prevent unexpected charges.
* [ ] **Enable Vertex AI API:** Run `gcloud services enable aiplatform.googleapis.com` or activate in the Google Cloud Console.
* [ ] **Deploy Firebase Cloud Functions:**
  ```bash
  firebase deploy --only functions:submitMusicGenerationJob
  ```
* [ ] **Verify First Paid Live Synthesis Test:** Execute one controlled generation request with owner present, verify audio asset in Cloud Storage, and confirm cost (approx. $0.08 per track).
