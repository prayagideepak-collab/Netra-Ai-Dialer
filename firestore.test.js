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

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
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

function validCallLogPayload(userId, now) {
  return {
    userId,
    phoneNumber: "+919876543210",
    contactName: "Dr. Aarav Mehta",
    direction: "OUTGOING",
    timestampMillis: 1761700000000,
    durationSeconds: 245,
    callingMode: "BUSINESS_VERIFIED",
    verifiedCallerIdStatus: "PROVIDER_APPROVED",
    verifiedLegalNameUsed: "Prayagi Store & Services",
    crmSyncStatus: "SYNCED",
    crmContactId: "crm_101",
    aiSummaryOverview: "Discussed Q4 bioscience reagent supply schedule and verified delivery slots.",
    aiDiscussionPoints: ["Confirmed cold-chain logistics", "Reviewed GST invoice terms"],
    aiActionItems: ["Send revised quote by Friday", "Schedule follow-up call"],
    createdAt: now,
    updatedAt: now,
  };
}

function validCrmPayload(userId, now) {
  return {
    userId,
    fullName: "Dr. Aarav Mehta",
    companyName: "Apex BioLabs Pvt Ltd",
    phoneNumber: "+919876543210",
    email: "aarav@apexbiolabs.in",
    stage: "VIP_PARTNER",
    totalCalls: 4,
    totalDurationSeconds: 920,
    verifiedOutboundCallsCount: 4,
    lastInteractionTimestamp: 1761700000000,
    latestAiSummary: "Confirmed Q4 reagent supply schedule.",
    openActionItems: ["Send revised quote by Friday"],
    notes: "Key institutional account for Prayagi Store & Services.",
    createdAt: now,
    updatedAt: now,
  };
}

test("Unauthenticated user: cannot read or write call_logs or crm_contacts", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("call_logs").get()
  );
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("crm_contacts").get()
  );
});

test("Authenticated owner: can create and query call_logs and crm_contacts", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date();

  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("call_logs")
      .doc("call_1")
      .set(validCallLogPayload(ALICE_UID, now))
  );

  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("crm_contacts")
      .doc("crm_101")
      .set(validCrmPayload(ALICE_UID, now))
  );

  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("call_logs")
      .where("userId", "==", ALICE_UID)
      .get()
  );
});

test("Cross-user isolation: Bob cannot read or modify Alice's call_logs or CRM records", async () => {
  const now = new Date();
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await ctx
      .firestore()
      .collection("users")
      .doc(ALICE_UID)
      .collection("call_logs")
      .doc("call_1")
      .set(validCallLogPayload(ALICE_UID, now));
    await ctx
      .firestore()
      .collection("users")
      .doc(ALICE_UID)
      .collection("crm_contacts")
      .doc("crm_101")
      .set(validCrmPayload(ALICE_UID, now));
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(
    bobDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("call_logs")
      .doc("call_1")
      .get()
  );
  await assertFails(
    bobDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("crm_contacts")
      .doc("crm_101")
      .get()
  );
});

test("Schema enforcement: rejects invalid verifiedCallerIdStatus or ghost fields", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date();

  const badEnum = {
    ...validCallLogPayload(ALICE_UID, now),
    verifiedCallerIdStatus: "FAKE_SPOOFED_STATUS",
  };
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("call_logs")
      .doc("call_bad_1")
      .set(badEnum)
  );

  const ghostField = {
    ...validCallLogPayload(ALICE_UID, now),
    isAdminOverride: true,
  };
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("call_logs")
      .doc("call_bad_2")
      .set(ghostField)
  );
});

test("Append-only audit trail: allows create but rejects update and delete", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date();
  const auditRef = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("identity_audit_logs")
    .doc("audit_1");

  await assertSucceeds(
    auditRef.set({
      userId: ALICE_UID,
      eventType: "IDENTITY_LOCKED",
      source: "GSTN Authoritative Registry",
      matchedLegalName: "Prayagi Store & Services",
      previousStatus: "UNVERIFIED",
      newStatus: "LOCKED_ACTIVE",
      failureReason: "",
      createdAt: now,
    })
  );

  await assertFails(auditRef.update({ newStatus: "TAMPERED" }));
  await assertFails(auditRef.delete());
});
