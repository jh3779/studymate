const fs = require("node:fs");
const path = require("node:path");
const { after, before, beforeEach, test } = require("node:test");

const {
  assertFails,
  assertSucceeds,
  initializeTestEnvironment,
} = require("@firebase/rules-unit-testing");
const {
  collection,
  doc,
  getDoc,
  getDocs,
  orderBy,
  query,
  serverTimestamp,
  setDoc,
  Timestamp,
  updateDoc,
  where,
  writeBatch,
} = require("firebase/firestore");

const PROJECT_ID = "studymate-rules-test";
const ALICE = {
  uid: "alice",
  email: "alice@example.com",
  emailVerified: true,
};
const BOB = {
  uid: "bob",
  email: "bob@example.com",
  emailVerified: true,
};

let testEnv;

function firestoreFor(user) {
  return testEnv.authenticatedContext(user.uid, {
    email: user.email,
    email_verified: user.emailVerified,
  }).firestore();
}

function userData(overrides = {}) {
  return {
    email: ALICE.email,
    nickname: "Alice",
    createdAt: serverTimestamp(),
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
    createdAt: serverTimestamp(),
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
    createdAt: serverTimestamp(),
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
    createdAt: serverTimestamp(),
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
    createdAt: serverTimestamp(),
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
  if (testEnv) {
    await testEnv.cleanup();
  }
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

test("allows optional user and study note fields to be omitted", async () => {
  const db = firestoreFor(ALICE);
  const userWithoutNickname = userData();
  const noteWithoutSubject = studyNoteData();

  delete userWithoutNickname.nickname;
  delete noteWithoutSubject.subject;

  await assertSucceeds(
    setDoc(doc(db, "users", ALICE.uid), userWithoutNickname)
  );
  await assertSucceeds(
    setDoc(doc(db, "study_notes", "note-without-subject"), noteWithoutSubject)
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

test("allows profile creation but blocks unverified users from app data", async () => {
  const unverifiedAlice = {
    ...ALICE,
    emailVerified: false,
  };
  const unverifiedDb = firestoreFor(unverifiedAlice);

  await assertSucceeds(
    setDoc(doc(unverifiedDb, "users", ALICE.uid), userData())
  );
  await assertFails(getDoc(doc(unverifiedDb, "users", ALICE.uid)));
  await assertFails(
    setDoc(
      doc(unverifiedDb, "study_notes", "unverified-note"),
      studyNoteData()
    )
  );

  const missingClaimDb = testEnv.authenticatedContext(ALICE.uid, {
    email: ALICE.email,
  }).firestore();
  await assertFails(
    setDoc(
      doc(missingClaimDb, "study_notes", "missing-claim-note"),
      studyNoteData()
    )
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
    setDoc(doc(db, "users", ALICE.uid), userData({ nickname: 123 }))
  );
  await assertFails(
    setDoc(
      doc(db, "study_notes", "wrong-optional-type"),
      studyNoteData({ subject: 123 })
    )
  );
  await assertFails(
    setDoc(
      doc(db, "users", ALICE.uid),
      userData({ email: "other@example.com" })
    )
  );
  await assertFails(
    setDoc(
      doc(db, "study_notes", "client-created-at"),
      studyNoteData({ createdAt: Timestamp.now() })
    )
  );
});

test("rejects invalid quiz and score ranges", async () => {
  const db = firestoreFor(ALICE);

  await assertSucceeds(
    setDoc(doc(db, "study_notes", "note-1"), studyNoteData())
  );
  await assertSucceeds(setDoc(doc(db, "quizzes", "quiz-1"), quizData()));

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
    setDoc(doc(db, "quiz_results", "inconsistent-score"), quizResultData({
      score: 66,
    }))
  );
  await assertFails(
    setDoc(doc(db, "wrong_answers", "bad-selected"), wrongAnswerData({
      selectedIndex: -1,
    }))
  );
  await assertFails(
    setDoc(doc(db, "wrong_answers", "not-wrong"), wrongAnswerData({
      selectedIndex: 1,
    }))
  );
});

test("rejects references to another user's or missing documents", async () => {
  const aliceDb = firestoreFor(ALICE);
  const bobDb = firestoreFor(BOB);

  await assertSucceeds(
    setDoc(doc(bobDb, "study_notes", "bob-note"), studyNoteData({
      userId: BOB.uid,
    }))
  );
  await assertFails(
    setDoc(doc(aliceDb, "quizzes", "missing-note"), quizData({
      noteId: "missing-note",
    }))
  );
  await assertFails(
    setDoc(doc(aliceDb, "quizzes", "bob-note-quiz"), quizData({
      noteId: "bob-note",
    }))
  );

  await assertSucceeds(
    setDoc(doc(aliceDb, "study_notes", "note-1"), studyNoteData())
  );
  await assertSucceeds(
    setDoc(doc(aliceDb, "quizzes", "quiz-1"), quizData())
  );
  await assertFails(
    setDoc(doc(aliceDb, "wrong_answers", "mismatched-quiz"), wrongAnswerData({
      question: "Tampered question",
    }))
  );
});

test("supports FirestoreService batch write contracts", async () => {
  const db = firestoreFor(ALICE);

  await assertSucceeds(
    setDoc(doc(db, "study_notes", "note-1"), studyNoteData())
  );

  const quizBatch = writeBatch(db);
  quizBatch.set(doc(db, "quizzes", "quiz-1"), quizData());
  quizBatch.set(doc(db, "quizzes", "quiz-2"), quizData({
    question: "What is validated before a Firestore write?",
    answerIndex: 1,
  }));
  await assertSucceeds(quizBatch.commit());

  const resultBatch = writeBatch(db);
  resultBatch.set(
    doc(db, "quiz_results", "result-1"),
    quizResultData()
  );
  resultBatch.set(
    doc(db, "wrong_answers", "wrong-1"),
    wrongAnswerData()
  );
  await assertSucceeds(resultBatch.commit());
});

test("supports user-scoped ordered query contracts", async () => {
  const aliceDb = firestoreFor(ALICE);
  const bobDb = firestoreFor(BOB);

  await assertSucceeds(
    setDoc(doc(aliceDb, "study_notes", "alice-note"), studyNoteData())
  );
  await assertSucceeds(
    setDoc(doc(bobDb, "study_notes", "bob-note"), studyNoteData({
      userId: BOB.uid,
      title: "Bob note",
    }))
  );
  await assertSucceeds(
    setDoc(doc(aliceDb, "quizzes", "quiz-1"), quizData({
      noteId: "alice-note",
    }))
  );
  await assertSucceeds(
    setDoc(doc(aliceDb, "quiz_results", "result-1"), quizResultData({
      noteId: "alice-note",
    }))
  );
  await assertSucceeds(
    setDoc(doc(aliceDb, "wrong_answers", "wrong-1"), wrongAnswerData({
      quizId: "quiz-1",
      noteId: "alice-note",
    }))
  );

  const aliceNotesQuery = query(
    collection(aliceDb, "study_notes"),
    where("userId", "==", ALICE.uid),
    orderBy("createdAt", "desc")
  );
  const aliceNotes = await assertSucceeds(getDocs(aliceNotesQuery));
  if (aliceNotes.size !== 1) {
    throw new Error(`Expected one Alice note, received ${aliceNotes.size}`);
  }

  const quizzesQuery = query(
    collection(aliceDb, "quizzes"),
    where("noteId", "==", "alice-note"),
    where("userId", "==", ALICE.uid),
    orderBy("createdAt", "asc")
  );
  const quizzes = await assertSucceeds(getDocs(quizzesQuery));
  if (quizzes.size !== 1) {
    throw new Error(`Expected one Alice quiz, received ${quizzes.size}`);
  }

  const wrongAnswersQuery = query(
    collection(aliceDb, "wrong_answers"),
    where("userId", "==", ALICE.uid),
    orderBy("createdAt", "desc")
  );
  const wrongAnswers = await assertSucceeds(getDocs(wrongAnswersQuery));
  if (wrongAnswers.size !== 1) {
    throw new Error(
      `Expected one Alice wrong answer, received ${wrongAnswers.size}`
    );
  }

  const quizResultsQuery = query(
    collection(aliceDb, "quiz_results"),
    where("userId", "==", ALICE.uid)
  );
  const quizResults = await assertSucceeds(getDocs(quizResultsQuery));
  if (quizResults.size !== 1) {
    throw new Error(
      `Expected one Alice quiz result, received ${quizResults.size}`
    );
  }

  await assertFails(getDocs(collection(aliceDb, "study_notes")));
});

test("allows editable fields but protects ownership and creation time", async () => {
  const db = firestoreFor(ALICE);
  const noteRef = doc(db, "study_notes", "note-1");

  await assertSucceeds(setDoc(noteRef, studyNoteData()));
  await assertSucceeds(updateDoc(noteRef, { title: "Updated title" }));
  await assertFails(updateDoc(noteRef, { userId: BOB.uid }));
  await assertFails(updateDoc(noteRef, { createdAt: Timestamp.now() }));
});
