# Server-Side Music Generation Specification: Google DeepMind Lyria on Vertex AI

## 1. Provider & Model Evaluation

### Identified Model Family
* **Model Lineage:** Google DeepMind Lyria
* **Available Model Variants on Vertex AI:**
  1. **Lyria 3 Pro Preview (`lyria-3-pro-preview`):** Generates compositions up to 3 minutes long with full musical architecture (intro, verses, chorus, bridge, outro), realistic vocal synthesis, timed lyrics, and multimodal guidance.
  2. **Lyria 3 Clip Preview (`lyria-3-clip-preview`):** Generates up to 30-second tracks with vocals and lyrics across diverse genres and languages.
  3. **Lyria 2 (`lyria-002`):** Generates instrumental-only tracks up to 32.8 seconds at 48kHz WAV audio with negative prompting and seed reproducibility.
* **Compatibility with Standard Gemini API Key:**
  - **Negative:** A standard Gemini API key (e.g. from Google AI Studio for Gemini 1.5/2.5) does **NOT** grant access to music-generation models. Lyria models run exclusively within Google Cloud Vertex AI / Model Garden.

### Input / Output Capabilities
* **Inputs:** Structured musical prompt, style/genre tags (e.g., Ghanaian Highlife, Afrobeats, Hiplife), mood descriptors, vocal preference (Male, Female, Duet, Instrumental), lyrics text with line breaks, tempo/BPM, language (Twi, English, French, Yoruba, Spanish, etc.).
* **Outputs:** Stereo 44.1 kHz / 48 kHz uncompressed WAV or 320 kbps MP3 stream, timing metadata, and track stem markers.
* **Supported Cloud Regions:** Google Cloud `us-central1` (Vertex AI Generative AI endpoint).

### Authentication & IAM Requirements
* **Client-Side Prohibition:** Google Cloud IAM Service Account keys must **never** be shipped in the Android APK.
* **Server-Side Authentication:**
  - The Android client authenticates to the backend using a short-lived Firebase Auth JWT token (`Authorization: Bearer <ID_TOKEN>`).
  - The backend verifies the token using the Firebase Admin SDK (`admin.auth().verifyIdToken()`).
  - The backend invokes Vertex AI using Google Application Default Credentials (ADC) or a dedicated IAM Service Account with the role:
    `roles/aiplatform.user` (Vertex AI User).

### Pricing & Billing Requirements
* **Official Vertex AI Pricing:**
  - **Lyria 3 Clip Preview:** ~$0.04 per generated song.
  - **Lyria 3 Pro Preview:** ~$0.08 per generated song.
  - **Lyria 2 (lyria-002):** ~$0.06 per 30 seconds of output music.
* **Cloud Storage & CDN:** Standard Google Cloud Storage egress (~$0.02 - $0.05/GB) and Firebase Hosting/CDN caching.
* **Owner Authorization Mandate:** Paid generation calls and Google Cloud billing changes require explicit owner approval prior to execution.

---

## 2. Server Architecture & Endpoint Flow

```
+--------------------------+
| Android Client           |
| (Osei AI Music Studio)   |
+------------+-------------+
             |
             | 1. POST /submitJob (Bearer: Firebase ID Token)
             v
+--------------------------+
| Cloud Function / Run     |
| (Backend Proxy)          |
+------------+-------------+
             | 2. Verify token & user credit balance in Firestore
             | 3. Check billing quota & server rate limits
             v
+--------------------------+
| Google Cloud Vertex AI   |
| (Lyria 3 Pro Model)      |
+------------+-------------+
             | 4. Synthesizes 44.1kHz audio stream
             v
+--------------------------+
| Cloud Storage Bucket     |
| (gs://osei-music-vault)  |
+------------+-------------+
             | 5. Public HTTPS CDN URL generated
             | 6. Update Firestore song status to "completed"
             v
+--------------------------+
| Android Client Receives  |
| Verified Audio URL       |
+--------------------------+
```

---

## 3. Reference Cloud Function Implementation (Node.js 20)

```javascript
const { onRequest } = require("firebase-functions/v2/https");
const admin = require("firebase-admin");
const { PredictionServiceClient } = require("@google-cloud/aiplatform").v1;

admin.initializeApp();
const db = admin.firestore();

exports.submitMusicGenerationJob = onRequest(
  {
    cors: false,
    region: "us-central1",
    secrets: ["VERTEX_AI_PROJECT_ID"],
    maxInstances: 10,
  },
  async (req, res) => {
    try {
      // 1. Verify Firebase Auth Token
      const authHeader = req.headers.authorization;
      if (!authHeader || !authHeader.startsWith("Bearer ")) {
        return res.status(401).json({ error: "Missing or malformed Authorization header" });
      }
      const idToken = authHeader.split("Bearer ")[1];
      const decodedToken = await admin.auth().verifyIdToken(idToken);
      const uid = decodedToken.uid;

      // 2. Validate Request Body
      const { songId, title, genre, mood, lyrics, vocalPreference, isInstrumental } = req.body;
      if (!songId || !title || !genre || !mood) {
        return res.status(400).json({ error: "Missing required parameters (songId, title, genre, mood)" });
      }

      // 3. Verify user document & credits in Firestore
      const userRef = db.collection("users").doc(uid);
      const songRef = userRef.collection("songs").doc(songId);

      const userDoc = await userRef.get();
      if (!userDoc.exists) {
        return res.status(404).json({ error: "User profile not found" });
      }

      const userData = userDoc.data();
      if ((userData.credits || 0) < 5) {
        return res.status(403).json({ error: "Insufficient credits for song generation" });
      }

      // 4. Update status to 'generating'
      await songRef.update({
        status: "generating",
        updatedAt: admin.firestore.FieldValue.serverTimestamp()
      });

      // 5. Call Vertex AI Lyria Model
      // When billing is approved by project owner, initialize Vertex AI prediction client:
      // const client = new PredictionServiceClient({ apiEndpoint: "us-central1-aiplatform.googleapis.com" });
      // ... execute model request and upload audio buffer to Cloud Storage ...

      return res.status(202).json({
        jobId: `job_${songId}_${Date.now()}`,
        songId: songId,
        status: "generating",
        progressPercent: 25,
        serverTimestamp: Date.now()
      });
    } catch (error) {
      console.error("Music generation failure:", error);
      return res.status(500).json({ error: error.message || "Internal server error" });
    }
  }
);
```

---

## 4. Deployment Prerequisites & Required Secrets

1. **Google Cloud Project:** Standard AI Studio / Firebase GCP project.
2. **APIs to Enable:**
   - `aiplatform.googleapis.com` (Vertex AI API)
   - `cloudfunctions.googleapis.com`
   - `storage.googleapis.com`
3. **IAM Role Assignment:**
   - Service account: `applet-service-account@...`
   - Role: `Vertex AI User` (`roles/aiplatform.user`)
4. **Billing Status:**
   - Must have active billing enabled on GCP.
   - Cost alert recommended at $10.00 / month threshold to prevent budget overruns.
