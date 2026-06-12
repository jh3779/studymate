package com.example.studymate;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.studymate.model.QuizModel;
import com.example.studymate.service.AiService;
import com.example.studymate.service.AuthService;
import com.example.studymate.service.FirestoreService;

import java.util.ArrayList;
import java.util.List;

public class SummaryResultViewModel extends ViewModel {
    private final AiService aiService = new AiService();
    private final AuthService authService = new AuthService();
    private final FirestoreService firestoreService = new FirestoreService();
    private final MutableLiveData<State> state = new MutableLiveData<>(State.idle());

    public LiveData<State> getState() {
        return state;
    }

    public void generateQuizzes(String noteId, String sourceText) {
        State current = state.getValue();
        if (current != null && current.isBusy()) {
            return;
        }
        if (sourceText == null || sourceText.trim().isEmpty()) {
            state.setValue(State.error("퀴즈를 만들 학습 내용이 없습니다."));
            return;
        }

        state.setValue(State.generating());
        aiService.generateQuizzes(sourceText, new AiService.QuizCallback() {
            @Override
            public void onSuccess(List<AiService.QuizItem> quizzes) {
                saveQuizzes(noteId, quizzes);
            }

            @Override
            public void onFailure(String errorMessage) {
                state.setValue(State.error(errorMessage));
            }
        });
    }

    public void consumeTerminalState() {
        State current = state.getValue();
        if (current != null
                && (current.status == Status.SUCCESS || current.status == Status.ERROR)) {
            state.setValue(State.idle());
        }
    }

    private void saveQuizzes(String noteId, List<AiService.QuizItem> quizzes) {
        String userId = authService.getCurrentUserId();
        if (noteId == null || noteId.trim().isEmpty() || userId == null) {
            state.setValue(State.error("학습 기록 정보를 확인할 수 없습니다."));
            return;
        }

        List<QuizModel> quizModels = new ArrayList<>();
        for (AiService.QuizItem item : quizzes) {
            quizModels.add(new QuizModel(
                    null,
                    noteId,
                    userId,
                    item.question,
                    item.options,
                    item.answerIndex,
                    item.explanation,
                    null
            ));
        }

        state.setValue(State.saving());
        firestoreService.saveQuizzes(quizModels, new FirestoreService.SaveListCallback() {
            @Override
            public void onSuccess(List<String> documentIds) {
                state.setValue(State.success(new ArrayList<>(quizModels)));
            }

            @Override
            public void onFailure(String errorMessage) {
                state.setValue(State.error(errorMessage));
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
        public final ArrayList<QuizModel> quizzes;
        public final String errorMessage;

        private State(Status status, ArrayList<QuizModel> quizzes, String errorMessage) {
            this.status = status;
            this.quizzes = quizzes;
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

        public static State success(ArrayList<QuizModel> quizzes) {
            return new State(Status.SUCCESS, quizzes, "");
        }

        public static State error(String errorMessage) {
            return new State(Status.ERROR, null, errorMessage);
        }

        public boolean isBusy() {
            return status == Status.GENERATING || status == Status.SAVING;
        }
    }
}
