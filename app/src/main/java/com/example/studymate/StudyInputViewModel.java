package com.example.studymate;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.studymate.model.StudyNoteModel;
import com.example.studymate.service.AiService;
import com.example.studymate.service.AuthService;
import com.example.studymate.service.FirestoreService;

import java.util.ArrayList;

public class StudyInputViewModel extends ViewModel {
    private final AiService aiService = new AiService();
    private final AuthService authService = new AuthService();
    private final FirestoreService firestoreService = new FirestoreService();
    private final MutableLiveData<State> state = new MutableLiveData<>(State.idle());

    public LiveData<State> getState() {
        return state;
    }

    public void generateSummary(String title, String subject, String content) {
        State current = state.getValue();
        if (current != null && current.isBusy()) {
            return;
        }

        state.setValue(State.generating());
        aiService.generateSummary(content, new AiService.SummaryCallback() {
            @Override
            public void onSuccess(AiService.SummaryResult result) {
                saveStudyNote(title, subject, content, result);
            }

            @Override
            public void onFailure(String errorMessage) {
                state.setValue(State.error(errorMessage));
            }
        });
    }

    public void consumeSuccess() {
        State current = state.getValue();
        if (current != null && current.status == Status.SUCCESS) {
            state.setValue(State.idle());
        }
    }

    private void saveStudyNote(
            String title,
            String subject,
            String content,
            AiService.SummaryResult result
    ) {
        String userId = authService.getCurrentUserId();
        if (userId == null) {
            state.setValue(State.error("로그인이 만료되었습니다. 다시 로그인해주세요."));
            return;
        }

        state.setValue(State.saving());
        StudyNoteModel note = new StudyNoteModel(
                null,
                userId,
                title,
                subject,
                content,
                result.summary,
                result.keywords,
                null
        );

        firestoreService.saveStudyNote(note, new FirestoreService.SaveCallback() {
            @Override
            public void onSuccess(String documentId) {
                state.setValue(State.success(new Result(
                        documentId,
                        title,
                        subject,
                        content,
                        new ArrayList<>(result.summary),
                        new ArrayList<>(result.keywords)
                )));
            }

            @Override
            public void onFailure(String errorMessage) {
                state.setValue(State.error(
                        "요약은 생성됐지만 저장에 실패했습니다. " + errorMessage
                ));
            }
        });
    }

    @Override
    protected void onCleared() {
        aiService.close();
        super.onCleared();
    }

    public enum Status {
        IDLE,
        GENERATING,
        SAVING,
        SUCCESS,
        ERROR
    }

    public static final class State {
        public final Status status;
        public final Result result;
        public final String errorMessage;

        private State(Status status, Result result, String errorMessage) {
            this.status = status;
            this.result = result;
            this.errorMessage = errorMessage;
        }

        public static State idle() {
            return new State(Status.IDLE, null, "");
        }

        public static State generating() {
            return new State(Status.GENERATING, null, "");
        }

        public static State saving() {
            return new State(Status.SAVING, null, "");
        }

        public static State success(Result result) {
            return new State(Status.SUCCESS, result, "");
        }

        public static State error(String errorMessage) {
            return new State(Status.ERROR, null, errorMessage);
        }

        public boolean isBusy() {
            return status == Status.GENERATING || status == Status.SAVING;
        }
    }

    public static final class Result {
        public final String noteId;
        public final String title;
        public final String subject;
        public final String content;
        public final ArrayList<String> summary;
        public final ArrayList<String> keywords;

        private Result(
                String noteId,
                String title,
                String subject,
                String content,
                ArrayList<String> summary,
                ArrayList<String> keywords
        ) {
            this.noteId = noteId;
            this.title = title;
            this.subject = subject;
            this.content = content;
            this.summary = summary;
            this.keywords = keywords;
        }
    }
}
