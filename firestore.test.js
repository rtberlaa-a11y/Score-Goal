const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = "demo-no-project";
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

test("Unauthenticated user: cannot read matches", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("matches").get());
});

test("Authenticated user: can read and query matches", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("matches").get());
});

test("Authenticated user: can create valid match", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("matches").doc("match_1").set({
      authorId: ALICE_UID,
      homeTeam: "Arsenal",
      awayTeam: "Chelsea",
      homeScore: 0,
      awayScore: 0,
      status: "upcoming",
      createdAt: new Date(),
    })
  );
});

test("Player profile: user can create own profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      name: "Alice Walker",
      mobileNumber: "+19876543210",
      jerseyNumber: 10,
      location: "San Francisco Ground",
      position: "Midfielder",
      strongFoot: "Right",
      backgroundTheme: "emerald_turf",
      goals: 5,
      assists: 8,
      passes: 120,
      matchesPlayed: 4,
      mvpCount: 1,
      followersCount: 0,
      createdAt: new Date(),
      updatedAt: new Date()
    })
  );
});

test("Player profile: user can view another player profile", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      name: "Alice Walker",
      mobileNumber: "+19876543210",
      jerseyNumber: 10,
      location: "San Francisco Ground",
      createdAt: new Date()
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertSucceeds(bobDb.collection("users").doc(ALICE_UID).get());
});

test("Team: user can create a team", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("teams").doc("team_1").set({
      name: "Red Hawks FC",
      shortName: "RHK",
      city: "San Francisco",
      colorHex: "#E53935",
      authorId: ALICE_UID,
      captainName: "Alice Walker",
      contactMobile: "+19876543210",
      createdAt: new Date()
    })
  );
});
