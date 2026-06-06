package com.example.studymate.service;

import com.example.studymate.model.QuizModel;
import com.example.studymate.model.QuizResultModel;
import com.example.studymate.model.StudyNoteModel;
import com.example.studymate.model.UserModel;
import com.example.studymate.model.WrongAnswerModel;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

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

    public void getQuizzesByNoteId(String noteId, ListCallback<QuizModel> callback) {
        if (isBlank(noteId)) {
            callback.onFailure("학습 기록 정보를 확인할 수 없습니다.");
            return;
        }

        firestore.collection(QUIZZES)
                .whereEqualTo("noteId", noteId)
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
        return "데이터 처리에 실패했습니다. 잠시 후 다시 시도해주세요.";
    }
}
