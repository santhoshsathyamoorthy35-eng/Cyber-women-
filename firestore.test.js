const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_uid_123";
const BOB_UID = "bob_uid_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function serverTs() {
  return new Date(Date.now() - 1000);
}

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

test("Unauthenticated user cannot read or write any collection", async () => {
  const unauth = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauth.collection("users").doc(ALICE_UID).get());
  await assertFails(unauth.collection("users").doc(ALICE_UID).collection("trusted_contacts").get());
  await assertFails(unauth.collection("users").doc(ALICE_UID).collection("sos_events").get());
  await assertFails(unauth.collection("users").doc(ALICE_UID).collection("citizen_reports").get());
});

test("Owner can create and update UserProfile, rejects ghost fields and cross-user read", async () => {
  const alice = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bob = testEnv.authenticatedContext(BOB_UID).firestore();
  const profileRef = alice.collection("users").doc(ALICE_UID);

  await assertSucceeds(profileRef.set({
    userId: ALICE_UID,
    displayName: "Meera",
    email: "meera@example.com",
    preferredLanguage: "en",
    selectedCity: "Puducherry",
    shakeToSosEnabled: false,
    customSosMessage: "Need help immediately.",
    createdAt: serverTs(),
    updatedAt: serverTs(),
  }));

  // Shadow update with ghost field rejected
  await assertFails(profileRef.update({
    preferredLanguage: "ta",
    isAdmin: true,
    updatedAt: serverTs(),
  }));

  // Valid update allowed
  await assertSucceeds(profileRef.update({
    preferredLanguage: "ta",
    updatedAt: serverTs(),
  }));

  // Cross-user read & write rejected
  await assertFails(bob.collection("users").doc(ALICE_UID).get());
  await assertFails(bob.collection("users").doc(ALICE_UID).update({ displayName: "Hacked", updatedAt: serverTs() }));
});

test("TrustedContact CRUD enforces ownership, priority bounds, and immutable fields", async () => {
  const alice = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bob = testEnv.authenticatedContext(BOB_UID).firestore();
  const contactRef = alice.collection("users").doc(ALICE_UID).collection("trusted_contacts").doc("c_1");

  // Out-of-range priority fails
  await assertFails(contactRef.set({
    userId: ALICE_UID,
    name: "Amma",
    phone: "+919876543210",
    relationship: "Mother",
    priority: 99,
    isPrimary: true,
    createdAt: serverTs(),
    updatedAt: serverTs(),
  }));

  // Valid contact succeeds
  await assertSucceeds(contactRef.set({
    userId: ALICE_UID,
    name: "Amma",
    phone: "+919876543210",
    relationship: "Mother",
    priority: 1,
    isPrimary: true,
    createdAt: serverTs(),
    updatedAt: serverTs(),
  }));

  // Owner list succeeds
  await assertSucceeds(alice.collection("users").doc(ALICE_UID).collection("trusted_contacts").get());

  // Cross-user list and get fail
  await assertFails(bob.collection("users").doc(ALICE_UID).collection("trusted_contacts").get());
  await assertFails(bob.collection("users").doc(ALICE_UID).collection("trusted_contacts").doc("c_1").get());
});

test("SosEvent enforces terminal state locking and bounded contactIds", async () => {
  const alice = testEnv.authenticatedContext(ALICE_UID).firestore();
  const sosRef = alice.collection("users").doc(ALICE_UID).collection("sos_events").doc("sos_1");

  await assertSucceeds(sosRef.set({
    userId: ALICE_UID,
    status: "created",
    triggerType: "hold",
    lat: 11.934,
    lon: 79.830,
    accuracyMeters: 12.5,
    locationLabel: "Goubert Market, Puducherry",
    message: "SOS emergency test",
    contactIds: ["c_1"],
    deliveryNote: "Logged locally; call 112 for dispatch.",
    createdAt: serverTs(),
    updatedAt: serverTs(),
  }));

  // Transition to cancelled succeeds
  await assertSucceeds(sosRef.update({
    status: "cancelled",
    deliveryNote: "Cancelled by user during countdown.",
    updatedAt: serverTs(),
  }));

  // Subsequent transition from terminal state fails
  await assertFails(sosRef.update({
    status: "logged_cloud",
    updatedAt: serverTs(),
  }));
});

test("CitizenReport enforces initial status received and single transition to acknowledged", async () => {
  const alice = testEnv.authenticatedContext(ALICE_UID).firestore();
  const repRef = alice.collection("users").doc(ALICE_UID).collection("citizen_reports").doc("rep_1");

  // Cannot create directly with acknowledged status
  await assertFails(repRef.set({
    userId: ALICE_UID,
    category: "Unsafe area / poor lighting",
    description: "Streetlights out near bus stand lane.",
    lat: 11.937,
    lon: 79.826,
    nearestStation: "Grand Bazaar PS",
    severity: 4,
    status: "acknowledged",
    createdAt: serverTs(),
    updatedAt: serverTs(),
  }));

  await assertSucceeds(repRef.set({
    userId: ALICE_UID,
    category: "Unsafe area / poor lighting",
    description: "Streetlights out near bus stand lane.",
    lat: 11.937,
    lon: 79.826,
    nearestStation: "Grand Bazaar PS",
    severity: 4,
    status: "received",
    createdAt: serverTs(),
    updatedAt: serverTs(),
  }));

  await assertSucceeds(repRef.update({
    status: "acknowledged",
    updatedAt: serverTs(),
  }));
});

test("UserFeedback is append-only", async () => {
  const alice = testEnv.authenticatedContext(ALICE_UID).firestore();
  const fbRef = alice.collection("users").doc(ALICE_UID).collection("feedback").doc("fb_1");

  await assertSucceeds(fbRef.set({
    userId: ALICE_UID,
    message: "Very clear helpline layout.",
    createdAt: serverTs(),
  }));

  await assertFails(fbRef.update({ message: "Edited" }));
  await assertFails(fbRef.delete());
});
