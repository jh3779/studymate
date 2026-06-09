const fs = require("node:fs");
const path = require("node:path");
const { after, before, beforeEach, test } = require("node:test");

const {
  assertFails,
  assertSucceeds,
  initializeTestEnvironment,
} = require("@firebase/rules-unit-testing");
const {
  doc,
  getDoc,
  setDoc,
  Timestamp,
  updateDoc,
} = require("firebase/firestore");

const PROJECT_ID = "studymate-rules-test";
const ALICE = {
  uid: "alice",
  email: "alice@example.com",
};
const BOB = {
  uid: "bob",
  email: "bob@example.com",
};

let testEnv;

function firestoreFor(user) {
  return testEnv.authenticatedContext(user.uid, {
    email: user.email,
  }).firestore();
}

function userData(overrides = {}) {
  return {
    email: ALICE.email,
    nickname: "Alice",
    createdAt: Timestamp.now(),
    ...overrides,
  };
}

function studyNoteData(overrides = {}) {
  return {
    userId: ALICE.uid,
    title: "Firestore rules",
    subject: "Firebase",
    originalText: "Security rules validate client writes.",
    summary: ["Validate ownership.", "Validate fields and types."],
    keywords: ["Firestore", "Rules"],
    createdAt: Timestamp.now(),
    ...overrides,
  };
}

function quizData(overrides = {}) {
  return {
    noteId: "note-1",
    userId: ALICE.uid,
    question: "Which layer validates Firestore client writes?",
    options: ["Activity", "Security Rules", "Layout", "Manifest"],
    answerIndex: 1,
    explanation: "Security Rules run before Firestore accepts the write.",
    createdAt: Timestamp.now(),
    ...overrides,
  };
}

function quizResultData(overrides = {}) {
  return {
    userId: ALICE.uid,
    noteId: "note-1",
    totalCount: 3,
    correctCount: 2,
    score: 67,
    createdAt: Timestamp.now(),
    ...overrides,
  };
}

function wrongAnswerData(overrides = {}) {
  return {
    userId: ALICE.uid,
    quizId: "quiz-1",
    noteId: "note-1",
    selectedIndex: 0,
    correctIndex: 1,
    question: "Which layer validates Firestore client writes?",
    options: ["Activity", "Security Rules", "Layout", "Manifest"],
    explanation: "Security Rules run before Firestore accepts the write.",
    createdAt: Timestamp.now(),
    ...overrides,
  };
}

before(async () => {
  const rulesPath = path.resolve(__dirname, "../../firestore.rules");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules: fs.readFileSync(rulesPath, "utf8"),
    },
  });
});

beforeEach(async () => {
  await testEnv.clearFirestore();
});

after(async () => {
  await testEnv.cleanup();
});

test("allows valid documents that match the Android models", async () => {
  const db = firestoreFor(ALICE);

  await assertSucceeds(setDoc(doc(db, "users", ALICE.uid), userData()));
  await assertSucceeds(setDoc(doc(db, "study_notes", "note-1"), studyNoteData()));
  await assertSucceeds(setDoc(doc(db, "quizzes", "quiz-1"), quizData()));
  await assertSucceeds(
    setDoc(doc(db, "quiz_results", "result-1"), quizResultData())
  );
  await assertSucceeds(
    setDoc(doc(db, "wrong_answers", "wrong-1"), wrongAnswerData())
  );
});

test("rejects unauthenticated and cross-user access", async () => {
  const aliceDb = firestoreFor(ALICE);
  const bobDb = firestoreFor(BOB);
  const guestDb = testEnv.unauthenticatedContext().firestore();
  const noteRef = doc(aliceDb, "study_notes", "note-1");

  await assertSucceeds(setDoc(noteRef, studyNoteData()));
  await assertFails(getDoc(doc(bobDb, "study_notes", "note-1")));
  await assertFails(getDoc(doc(guestDb, "study_notes", "note-1")));
  await assertFails(
    setDoc(doc(bobDb, "study_notes", "note-2"), studyNoteData())
  );
});

test("rejects unknown fields and invalid field types", async () => {
  const db = firestoreFor(ALICE);

  await assertFails(
    setDoc(
      doc(db, "study_notes", "unknown-field"),
      studyNoteData({ admin: true })
    )
  );
  await assertFails(
    setDoc(
      doc(db, "study_notes", "wrong-type"),
      studyNoteData({ summary: "not-a-list" })
    )
  );
  await assertFails(
    setDoc(
      doc(db, "users", ALICE.uid),
      userData({ email: "other@example.com" })
    )
  );
});

test("rejects invalid quiz and score ranges", async () => {
  const db = firestoreFor(ALICE);

  await assertFails(
    setDoc(doc(db, "quizzes", "bad-options"), quizData({
      options: ["A", "B", "C"],
    }))
  );
  await assertFails(
    setDoc(doc(db, "quizzes", "bad-answer"), quizData({
      answerIndex: 4,
    }))
  );
  await assertFails(
    setDoc(doc(db, "quiz_results", "bad-count"), quizResultData({
      correctCount: 4,
    }))
  );
  await assertFails(
    setDoc(doc(db, "quiz_results", "bad-score"), quizResultData({
      score: 101,
    }))
  );
  await assertFails(
    setDoc(doc(db, "wrong_answers", "bad-selected"), wrongAnswerData({
      selectedIndex: -1,
    }))
  );
});

test("allows editable fields but protects ownership and creation time", async () => {
  const db = firestoreFor(ALICE);
  const noteRef = doc(db, "study_notes", "note-1");

  await assertSucceeds(setDoc(noteRef, studyNoteData()));
  await assertSucceeds(updateDoc(noteRef, { title: "Updated title" }));
  await assertFails(updateDoc(noteRef, { userId: BOB.uid }));
  await assertFails(updateDoc(noteRef, { createdAt: Timestamp.now() }));
});
