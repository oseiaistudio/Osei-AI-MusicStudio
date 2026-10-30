const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read users", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").get());
});

test("Authenticated user: can create their own profile with free tier and <= 50 credits", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice Kwame",
      email: "alice@example.com",
      credits: 50,
      subscriptionTier: "free",
      createdAt: new Date(),
    })
  );
});

test("Security Hardening: User cannot forge unauthorized initial credits (> 50)", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice Kwame",
      email: "alice@example.com",
      credits: 9999, // Unauthorized forged credits
      subscriptionTier: "free",
      createdAt: new Date(),
    })
  );
});

test("Security Hardening: User cannot forge initial premium subscription tier", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice Kwame",
      email: "alice@example.com",
      credits: 50,
      subscriptionTier: "pro", // Unauthorized forged tier
      createdAt: new Date(),
    })
  );
});

test("Authenticated user: cannot create another user's profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).set({
      userId: BOB_UID,
      displayName: "Bob Mensah",
      email: "bob@example.com",
      credits: 50,
      subscriptionTier: "free",
      createdAt: new Date(),
    })
  );
});

test("Security Hardening: User cannot update credits or subscriptionTier directly", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice Kwame",
      email: "alice@example.com",
      credits: 50,
      subscriptionTier: "free",
      createdAt: new Date(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();

  // Attempting to elevate credits directly must fail
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      credits: 100,
    })
  );

  // Attempting to change subscriptionTier directly must fail
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      subscriptionTier: "pro",
    })
  );

  // Updating allowable profile fields must succeed
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      displayName: "Alice Kwame Updated",
      preferredLanguage: "Twi",
      preferredGenre: "Ghanaian Highlife",
    })
  );
});

test("Security Hardening: User cannot forge fake completed song on creation", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("songs").doc("song_fake").set({
      userId: ALICE_UID,
      title: "Fabricated Track",
      genre: "Ghanaian Highlife",
      mood: "Uplifting",
      vocalPreference: "Male",
      language: "Twi",
      isInstrumental: false,
      status: "completed", // Forged completed status
      audioUrl: "https://actions.google.com/sounds/v1/water/rain_heavy.ogg", // Forged audio
      createdAt: new Date(),
    })
  );
});

test("Authenticated user: can create a legitimate song draft or queued song", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("songs").doc("song_legit").set({
      userId: ALICE_UID,
      title: "Accra Nights Highlife",
      genre: "Ghanaian Highlife",
      mood: "Uplifting",
      vocalPreference: "Male",
      language: "Twi",
      isInstrumental: false,
      status: "queued",
      createdAt: new Date(),
    })
  );
});

test("Security Hardening: User cannot elevate song status to completed or forge audioUrl", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(ALICE_UID).collection("songs").doc("song_1").set({
      userId: ALICE_UID,
      title: "Accra Nights Highlife",
      genre: "Ghanaian Highlife",
      mood: "Uplifting",
      vocalPreference: "Male",
      language: "Twi",
      isInstrumental: false,
      status: "queued",
      createdAt: new Date(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();

  // Attempting to forge completion status must fail
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("songs").doc("song_1").update({
      status: "completed",
      audioUrl: "https://fake.url/audio.mp3",
    })
  );

  // Updating allowed fields (lyrics, title, mood, isFavorite) must succeed
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("songs").doc("song_1").update({
      lyrics: "Ohemaa, come dance to Highlife rhythm",
      isFavorite: true,
    })
  );
});

test("Authenticated user: cannot access another user's songs", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(BOB_UID).collection("songs").doc("song_bob").set({
      userId: BOB_UID,
      title: "Tema Afrobeats",
      genre: "Afrobeats",
      mood: "Energetic",
      vocalPreference: "Female",
      language: "English",
      isInstrumental: false,
      status: "queued",
      createdAt: new Date(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("songs").doc("song_bob").get());
});
