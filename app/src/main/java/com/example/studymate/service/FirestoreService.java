package com.example.studymate.service;

import com.example.studymate.model.QuizModel;
import com.example.studymate.model.QuizResultModel;
import com.example.studymate.model.StudyNoteModel;
import com.example.studymate.model.UserModel;
import com.example.studymate.model.UserStatsModel;
import com.example.studymate.model.WrongAnswerModel;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.android.gms.tasks.Tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirestoreService {
    private static final String USERS = "users";
    private static final String STUDY_NOTES = "study_notes";
    private static final String QUIZZES = "quizzes";
    private static final String QUIZ_RESULTS = "quiz_results";
    private static final String WRONG_ANSWERS = "wrong_answers";

    private final FirebaseFirestore firestore;

    public FirestoreService() {
        firestore = FirebaseFirestore.getInstance();
    }

    public interface SaveCallback {
        void onSuccess(String documentId);

        void onFailure(String errorMessage);
    }

    public interface SaveListCallback {
        void onSuccess(List<String> documentIds);

        void onFailure(String errorMessage);
    }

    public interface ListCallback<T> {
        void onSuccess(List<T> items);

        void onFailure(String errorMessage);
    }

    public interface StatsCallback {
        void onSuccess(UserStatsModel stats);

        void onFailure(String errorMessage);
    }

    public interface HomeDataCallback {
        void onSuccess(
                List<StudyNoteModel> studyNotes,
                List<QuizResultModel> quizResults
        );

        void onStudyNotesOnly(
                List<StudyNoteModel> studyNotes,
                String errorMessage
        );

        void onFailure(String errorMessage);
    }

    public interface DeleteCallback {
        void onSuccess();

        void onFailure(String errorMessage);
    }

    public void saveUser(UserModel user, SaveCallback callback) {
        if (user == null || isBlank(user.getId())) {
            callback.onFailure("사용자 정보가 올바르지 않습니다.");
            return;
        }

        Map<String, Object> values = withCreatedAt(user.toMap());
        firestore.collection(USERS)
                .document(user.getId())
                .set(values)
                .addOnSuccessListener(unused -> callback.onSuccess(user.getId()))
                .addOnFailureListener(error -> callback.onFailure(toUserMessage(error)));
    }

    public void saveStudyNote(StudyNoteModel note, SaveCallback callback) {
        if (note == null || isBlank(note.getUserId()) || isBlank(note.getTitle())) {
            callback.onFailure("학습 기록 정보가 올바르지 않습니다.");
            return;
        }

        DocumentReference document = firestore.collection(STUDY_NOTES).document();
        note.setId(document.getId());
        document.set(withCreatedAt(note.toMap()))
                .addOnSuccessListener(unused -> callback.onSuccess(document.getId()))
                .addOnFailureListener(error -> callback.onFailure(toUserMessage(error)));
    }

    public void getStudyNotes(String userId, ListCallback<StudyNoteModel> callback) {
        if (isBlank(userId)) {
            callback.onFailure("로그인 사용자 정보를 확인할 수 없습니다.");
            return;
        }

        firestore.collection(STUDY_NOTES)
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<StudyNoteModel> notes = new ArrayList<>();
                    for (DocumentSnapshot document : snapshot.getDocuments()) {
                        notes.add(StudyNoteModel.fromMap(
                                document.getId(),
                                dataWithDate(document)
                        ));
                    }
                    callback.onSuccess(notes);
                })
                .addOnFailureListener(error -> callback.onFailure(toUserMessage(error)));
    }

    public void saveQuizzes(List<QuizModel> quizzes, SaveListCallback callback) {
        if (quizzes == null || quizzes.isEmpty()) {
            callback.onFailure("저장할 퀴즈가 없습니다.");
            return;
        }

        WriteBatch batch = firestore.batch();
        List<String> documentIds = new ArrayList<>();

        for (QuizModel quiz : quizzes) {
            if (quiz == null || isBlank(quiz.getUserId()) || isBlank(quiz.getNoteId())) {
                callback.onFailure("퀴즈 정보가 올바르지 않습니다.");
                return;
            }

            DocumentReference document = firestore.collection(QUIZZES).document();
            quiz.setId(document.getId());
            documentIds.add(document.getId());
            batch.set(document, withCreatedAt(quiz.toMap()));
        }

        batch.commit()
                .addOnSuccessListener(unused -> callback.onSuccess(documentIds))
                .addOnFailureListener(error -> callback.onFailure(toUserMessage(error)));
    }

    public void getQuizzesByNoteId(
            String noteId,
            String userId,
            ListCallback<QuizModel> callback
    ) {
        if (isBlank(noteId) || isBlank(userId)) {
            callback.onFailure("학습 기록 정보를 확인할 수 없습니다.");
            return;
        }

        firestore.collection(QUIZZES)
                .whereEqualTo("noteId", noteId)
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<QuizModel> quizzes = new ArrayList<>();
                    for (DocumentSnapshot document : snapshot.getDocuments()) {
                        quizzes.add(QuizModel.fromMap(
                                document.getId(),
                                dataWithDate(document)
                        ));
                    }
                    callback.onSuccess(quizzes);
                })
                .addOnFailureListener(error -> callback.onFailure(toUserMessage(error)));
    }

    public void saveQuizResult(QuizResultModel result, SaveCallback callback) {
        if (result == null || isBlank(result.getUserId()) || isBlank(result.getNoteId())) {
            callback.onFailure("퀴즈 결과 정보가 올바르지 않습니다.");
            return;
        }

        DocumentReference document = firestore.collection(QUIZ_RESULTS).document();
        result.setId(document.getId());
        document.set(withCreatedAt(result.toMap()))
                .addOnSuccessListener(unused -> callback.onSuccess(document.getId()))
                .addOnFailureListener(error -> callback.onFailure(toUserMessage(error)));
    }

    public void getHomeData(String userId, HomeDataCallback callback) {
        if (isBlank(userId)) {
            callback.onFailure("로그인 사용자 정보를 확인할 수 없습니다.");
            return;
        }

        com.google.android.gms.tasks.Task<QuerySnapshot> studyNotesTask =
                firestore.collection(STUDY_NOTES)
                        .whereEqualTo("userId", userId)
                        .orderBy("createdAt", Query.Direction.DESCENDING)
                        .get();
        com.google.android.gms.tasks.Task<QuerySnapshot> quizResultsTask =
                firestore.collection(QUIZ_RESULTS)
                        .whereEqualTo("userId", userId)
                        .get();

        Tasks.whenAllComplete(studyNotesTask, quizResultsTask)
                .addOnSuccessListener(unused -> {
                    if (!studyNotesTask.isSuccessful()) {
                        callback.onFailure(toUserMessage(studyNotesTask.getException()));
                        return;
                    }

                    QuerySnapshot studyNoteSnapshot = studyNotesTask.getResult();
                    List<StudyNoteModel> studyNotes = new ArrayList<>();
                    for (DocumentSnapshot document : studyNoteSnapshot.getDocuments()) {
                        studyNotes.add(StudyNoteModel.fromMap(
                                document.getId(),
                                dataWithDate(document)
                        ));
                    }

                    if (!quizResultsTask.isSuccessful()) {
                        callback.onStudyNotesOnly(
                                studyNotes,
                                toUserMessage(quizResultsTask.getException())
                        );
                        return;
                    }

                    QuerySnapshot quizResultSnapshot = quizResultsTask.getResult();
                    List<QuizResultModel> quizResults = new ArrayList<>();
                    for (DocumentSnapshot document : quizResultSnapshot.getDocuments()) {
                        quizResults.add(QuizResultModel.fromMap(
                                document.getId(),
                                dataWithDate(document)
                        ));
                    }
                    callback.onSuccess(studyNotes, quizResults);
                });
    }

    public void deleteStudyNoteWithRelatedData(
            String noteId,
            String userId,
            DeleteCallback callback
    ) {
        if (isBlank(noteId) || isBlank(userId)) {
            callback.onFailure("삭제할 학습 기록 정보를 확인할 수 없습니다.");
            return;
        }

        com.google.android.gms.tasks.Task<QuerySnapshot> quizzesTask =
                firestore.collection(QUIZZES)
                        .whereEqualTo("noteId", noteId)
                        .whereEqualTo("userId", userId)
                        .get();
        com.google.android.gms.tasks.Task<QuerySnapshot> resultsTask =
                firestore.collection(QUIZ_RESULTS)
                        .whereEqualTo("noteId", noteId)
                        .whereEqualTo("userId", userId)
                        .get();
        com.google.android.gms.tasks.Task<QuerySnapshot> wrongAnswersTask =
                firestore.collection(WRONG_ANSWERS)
                        .whereEqualTo("noteId", noteId)
                        .whereEqualTo("userId", userId)
                        .get();

        Tasks.whenAllSuccess(quizzesTask, resultsTask, wrongAnswersTask)
                .addOnSuccessListener(results -> {
                    QuerySnapshot quizzes = (QuerySnapshot) results.get(0);
                    QuerySnapshot quizResults = (QuerySnapshot) results.get(1);
                    QuerySnapshot wrongAnswers = (QuerySnapshot) results.get(2);
                    int deleteCount = 1
                            + quizzes.size()
                            + quizResults.size()
                            + wrongAnswers.size();
                    if (deleteCount > 500) {
                        callback.onFailure("연결된 기록이 너무 많아 한 번에 삭제할 수 없습니다.");
                        return;
                    }

                    WriteBatch batch = firestore.batch();
                    for (DocumentSnapshot document : wrongAnswers.getDocuments()) {
                        batch.delete(document.getReference());
                    }
                    for (DocumentSnapshot document : quizResults.getDocuments()) {
                        batch.delete(document.getReference());
                    }
                    for (DocumentSnapshot document : quizzes.getDocuments()) {
                        batch.delete(document.getReference());
                    }
                    batch.delete(firestore.collection(STUDY_NOTES).document(noteId));
                    batch.commit()
                            .addOnSuccessListener(unused -> callback.onSuccess())
                            .addOnFailureListener(
                                    error -> callback.onFailure(toUserMessage(error))
                            );
                })
                .addOnFailureListener(error -> callback.onFailure(
                        toUserMessage((Exception) error)
                ));
    }

    public void getQuizResults(String userId, ListCallback<QuizResultModel> callback) {
        if (isBlank(userId)) {
            callback.onFailure("로그인 사용자 정보를 확인할 수 없습니다.");
            return;
        }

        firestore.collection(QUIZ_RESULTS)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<QuizResultModel> results = new ArrayList<>();
                    for (DocumentSnapshot document : snapshot.getDocuments()) {
                        results.add(QuizResultModel.fromMap(
                                document.getId(),
                                dataWithDate(document)
                        ));
                    }
                    callback.onSuccess(results);
                })
                .addOnFailureListener(error -> callback.onFailure(toUserMessage(error)));
    }

    public void saveQuizResultWithWrongAnswers(
            QuizResultModel result,
            List<WrongAnswerModel> wrongAnswers,
            SaveCallback callback
    ) {
        if (result == null || isBlank(result.getUserId()) || isBlank(result.getNoteId())) {
            callback.onFailure("퀴즈 결과 정보가 올바르지 않습니다.");
            return;
        }

        WriteBatch batch = firestore.batch();
        DocumentReference resultDocument = firestore.collection(QUIZ_RESULTS).document();
        result.setId(resultDocument.getId());
        batch.set(resultDocument, withCreatedAt(result.toMap()));

        if (wrongAnswers != null) {
            for (WrongAnswerModel wrongAnswer : wrongAnswers) {
                if (wrongAnswer == null
                        || isBlank(wrongAnswer.getUserId())
                        || isBlank(wrongAnswer.getQuizId())
                        || isBlank(wrongAnswer.getNoteId())) {
                    callback.onFailure("오답 정보가 올바르지 않습니다.");
                    return;
                }

                DocumentReference wrongDocument =
                        firestore.collection(WRONG_ANSWERS).document();
                wrongAnswer.setId(wrongDocument.getId());
                batch.set(wrongDocument, withCreatedAt(wrongAnswer.toMap()));
            }
        }

        batch.commit()
                .addOnSuccessListener(unused -> callback.onSuccess(resultDocument.getId()))
                .addOnFailureListener(error -> callback.onFailure(toUserMessage(error)));
    }

    public void saveQuizOutcome(
            QuizResultModel result,
            List<WrongAnswerModel> wrongAnswers,
            SaveCallback callback
    ) {
        if (result == null
                || isBlank(result.getId())
                || isBlank(result.getUserId())
                || isBlank(result.getNoteId())) {
            callback.onFailure("퀴즈 결과 정보가 올바르지 않습니다.");
            return;
        }

        DocumentReference resultDocument = firestore.collection(QUIZ_RESULTS)
                .document(result.getId());
        result.setId(resultDocument.getId());

        List<WrongAnswerModel> safeWrongAnswers = wrongAnswers == null
                ? new ArrayList<>()
                : wrongAnswers;
        WriteBatch batch = firestore.batch();
        batch.set(resultDocument, withCreatedAt(result.toMap()));

        for (WrongAnswerModel wrongAnswer : safeWrongAnswers) {
            if (wrongAnswer == null
                    || isBlank(wrongAnswer.getUserId())
                    || isBlank(wrongAnswer.getQuizId())
                    || isBlank(wrongAnswer.getNoteId())) {
                callback.onFailure("오답 정보가 올바르지 않습니다.");
                return;
            }

            String documentId = resultDocument.getId() + "_" + wrongAnswer.getQuizId();
            DocumentReference wrongDocument =
                    firestore.collection(WRONG_ANSWERS).document(documentId);
            wrongAnswer.setId(documentId);
            batch.set(wrongDocument, withCreatedAt(wrongAnswer.toMap()));
        }

        batch.commit()
                .addOnSuccessListener(unused -> callback.onSuccess(resultDocument.getId()))
                .addOnFailureListener(error -> callback.onFailure(toUserMessage(error)));
    }

    public void saveWrongAnswer(WrongAnswerModel wrongAnswer, SaveCallback callback) {
        if (wrongAnswer == null
                || isBlank(wrongAnswer.getUserId())
                || isBlank(wrongAnswer.getQuizId())
                || isBlank(wrongAnswer.getNoteId())) {
            callback.onFailure("오답 정보가 올바르지 않습니다.");
            return;
        }

        DocumentReference document = firestore.collection(WRONG_ANSWERS).document();
        wrongAnswer.setId(document.getId());
        document.set(withCreatedAt(wrongAnswer.toMap()))
                .addOnSuccessListener(unused -> callback.onSuccess(document.getId()))
                .addOnFailureListener(error -> callback.onFailure(toUserMessage(error)));
    }

    public void getWrongAnswers(String userId, ListCallback<WrongAnswerModel> callback) {
        if (isBlank(userId)) {
            callback.onFailure("로그인 사용자 정보를 확인할 수 없습니다.");
            return;
        }

        firestore.collection(WRONG_ANSWERS)
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<WrongAnswerModel> wrongAnswers = new ArrayList<>();
                    for (DocumentSnapshot document : snapshot.getDocuments()) {
                        wrongAnswers.add(WrongAnswerModel.fromMap(
                                document.getId(),
                                dataWithDate(document)
                        ));
                    }
                    callback.onSuccess(wrongAnswers);
                })
                .addOnFailureListener(error -> callback.onFailure(toUserMessage(error)));
    }

    public void deleteWrongAnswers(
            List<WrongAnswerModel> wrongAnswers,
            DeleteCallback callback
    ) {
        if (wrongAnswers == null || wrongAnswers.isEmpty()) {
            callback.onFailure("삭제할 오답 기록이 없습니다.");
            return;
        }
        if (wrongAnswers.size() > 500) {
            callback.onFailure("오답 기록이 너무 많아 한 번에 삭제할 수 없습니다.");
            return;
        }

        WriteBatch batch = firestore.batch();
        for (WrongAnswerModel wrongAnswer : wrongAnswers) {
            if (wrongAnswer == null || isBlank(wrongAnswer.getId())) {
                callback.onFailure("삭제할 오답 기록 정보를 확인할 수 없습니다.");
                return;
            }
            batch.delete(firestore.collection(WRONG_ANSWERS).document(wrongAnswer.getId()));
        }
        batch.commit()
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(error -> callback.onFailure(toUserMessage(error)));
    }

    public void getUserStats(String userId, StatsCallback callback) {
        if (isBlank(userId)) {
            callback.onFailure("로그인 사용자 정보를 확인할 수 없습니다.");
            return;
        }

        com.google.android.gms.tasks.Task<com.google.firebase.firestore.QuerySnapshot>
                studyNotesTask = firestore.collection(STUDY_NOTES)
                .whereEqualTo("userId", userId)
                .get();
        com.google.android.gms.tasks.Task<com.google.firebase.firestore.QuerySnapshot>
                quizResultsTask = firestore.collection(QUIZ_RESULTS)
                .whereEqualTo("userId", userId)
                .get();

        Tasks.whenAllSuccess(studyNotesTask, quizResultsTask)
                .addOnSuccessListener(results -> {
                    com.google.firebase.firestore.QuerySnapshot studyNotes =
                            (com.google.firebase.firestore.QuerySnapshot) results.get(0);
                    com.google.firebase.firestore.QuerySnapshot quizResults =
                            (com.google.firebase.firestore.QuerySnapshot) results.get(1);

                    int totalScore = 0;
                    for (DocumentSnapshot document : quizResults.getDocuments()) {
                        Long score = document.getLong("score");
                        if (score != null) {
                            totalScore += score.intValue();
                        }
                    }

                    int quizResultCount = quizResults.size();
                    int averageScore = quizResultCount == 0
                            ? 0
                            : Math.round((float) totalScore / quizResultCount);
                    callback.onSuccess(new UserStatsModel(
                            studyNotes.size(),
                            quizResultCount,
                            averageScore
                    ));
                })
                .addOnFailureListener(error -> callback.onFailure(
                        toUserMessage((Exception) error)
                ));
    }

    private Map<String, Object> withCreatedAt(Map<String, Object> source) {
        Map<String, Object> values = new HashMap<>(source);
        if (values.get("createdAt") == null) {
            values.put("createdAt", FieldValue.serverTimestamp());
        }
        return values;
    }

    private Map<String, Object> dataWithDate(DocumentSnapshot document) {
        Map<String, Object> values = document.getData() == null
                ? new HashMap<>()
                : new HashMap<>(document.getData());
        values.put("createdAt", document.getDate("createdAt"));
        return values;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String toUserMessage(Exception error) {
        if (error instanceof FirebaseNetworkException) {
            return "네트워크 연결을 확인한 후 다시 시도해주세요.";
        }
        if (error instanceof FirebaseFirestoreException) {
            FirebaseFirestoreException firestoreError = (FirebaseFirestoreException) error;
            if (firestoreError.getCode() == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                return "데이터 저장 권한이 없습니다. 로그인 및 이메일 인증 상태를 확인해주세요.";
            }
        }
        return "데이터 처리에 실패했습니다. 잠시 후 다시 시도해주세요.";
    }
}
