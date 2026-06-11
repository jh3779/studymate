package com.example.studymate;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.studymate.model.QuizResultModel;
import com.example.studymate.model.WrongAnswerModel;
import com.example.studymate.service.FirestoreService;

import java.util.List;

public class QuizResultViewModel extends ViewModel {
    private final MutableLiveData<String> saveStatus = new MutableLiveData<>();
    private boolean saveStarted;

    public LiveData<String> getSaveStatus() {
        return saveStatus;
    }

    public void saveOutcome(
            FirestoreService firestoreService,
            QuizResultModel result,
            List<WrongAnswerModel> wrongAnswers,
            int wrongCount
    ) {
        if (saveStarted) {
            return;
        }
        saveStarted = true;

        if (wrongCount == 0) {
            saveStatus.setValue("저장할 오답이 없습니다.");
        } else {
            saveStatus.setValue("오답을 오답노트에 저장하는 중입니다.");
        }

        firestoreService.saveQuizOutcome(result, wrongAnswers, new FirestoreService.SaveCallback() {
            @Override
            public void onSuccess(String documentId) {
                if (wrongCount == 0) {
                    saveStatus.setValue("저장할 오답이 없습니다.");
                } else {
                    saveStatus.setValue("퀴즈 결과와 오답노트를 저장했습니다.");
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                saveStatus.setValue("저장에 실패했습니다. 잠시 후 다시 시도해주세요.");
            }
        });
    }
}
