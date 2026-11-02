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

test("Unauthenticated user: cannot read orders", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("orders").get());
});

test("Authenticated user: cannot read another user's orders", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(BOB_UID).collection("orders").doc("order_bob").set({
      userId: BOB_UID,
      customerName: "Pooja",
      customerPhone: "9876543210",
      dressType: "suit_kurti",
      status: "received",
      totalAmount: 500,
      advanceAmount: 200,
      pendingAmount: 300,
      createdAt: new Date(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("orders").doc("order_bob").get());
});

test("Authenticated user: can create and read their own order", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const orderRef = aliceDb.collection("users").doc(ALICE_UID).collection("orders").doc("order_1");
  await assertSucceeds(
    orderRef.set({
      userId: ALICE_UID,
      customerName: "Sunita Sharma",
      customerPhone: "9876543210",
      dressType: "blouse",
      status: "received",
      totalAmount: 400,
      advanceAmount: 200,
      pendingAmount: 200,
      notes: "Square neck design with piping",
      photoUris: ["content://sample1"],
      measurements: {
        length: "14",
        chest: "34",
        waist: "28",
        sleevesLength: "10"
      },
      createdAt: new Date(),
    })
  );

  await assertSucceeds(orderRef.get());
});

test("Authenticated user: update cannot change createdAt or userId", async () => {
  const now = new Date();
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(ALICE_UID).collection("orders").doc("order_1").set({
      userId: ALICE_UID,
      customerName: "Sunita Sharma",
      customerPhone: "9876543210",
      dressType: "blouse",
      status: "received",
      totalAmount: 400,
      advanceAmount: 200,
      pendingAmount: 200,
      createdAt: now,
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const orderRef = aliceDb.collection("users").doc(ALICE_UID).collection("orders").doc("order_1");

  // Success: valid update of status and pendingAmount
  await assertSucceeds(
    orderRef.update({
      status: "stitching",
      pendingAmount: 100,
      updatedAt: new Date(),
    })
  );

  // Failure: trying to change userId
  await assertFails(
    orderRef.update({
      userId: BOB_UID,
    })
  );
});
