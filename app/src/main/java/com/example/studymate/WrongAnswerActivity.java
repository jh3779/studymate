package com.example.studymate;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.studymate.model.QuizModel;
import com.example.studymate.model.StudyNoteModel;
import com.example.studymate.model.WrongAnswerModel;
import com.example.studymate.service.AuthService;
import com.example.studymate.service.FirestoreService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *  원본 AI 생성 퀴즈 기반 오답노트 동적 매핑 핸들러
 */
public class WrongAnswerActivity extends BaseActivity {

    // 결과 화면에서 전달된 오답 또는 Firestore에 저장된 오답을 동적으로 표시한다.
    private ArrayList<QuizModel> quizList = new ArrayList<>();
    private ArrayList<WrongAnswerModel> savedWrongAnswers = new ArrayList<>();
    private ArrayList<Integer> wrongIndices = new ArrayList<>();
    private ArrayList<Integer> userAnswers = new ArrayList<>();
    private int currentWrongIndex = 0;
    private boolean showingSavedWrongAnswers = false;

    // 노트 선택 화면으로 돌아갈 수 있도록 그룹 데이터 캐싱
    private Map<String, List<WrongAnswerModel>> noteGroupedWrongAnswers = null;
    private List<StudyNoteModel> cachedStudyNotes = null;

    private final AuthService authService = new AuthService();
    private final FirestoreService firestoreService = new FirestoreService();

    private LinearLayout noteSelectPanel;
    private LinearLayout noteSelectContainer;
    private LinearLayout wrongAnswerDetailPanel;

    private TextView tvWrongProgress;
    private TextView tvWrongQuestion;
    private TextView tvMyAnswer;
    private TextView tvRealAnswer;
    private TextView tvExplanation;
    private Button retryWrongButton;
    private Button retryQuizButton;
    private String noteId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wrong_answer);

        noteSelectPanel = findViewById(R.id.noteSelectPanel);
        noteSelectContainer = findViewById(R.id.noteSelectContainer);
        wrongAnswerDetailPanel = findViewById(R.id.wrongAnswerDetailPanel);

        tvWrongProgress = findViewById(R.id.quizProgressText);
        tvWrongQuestion = findViewById(R.id.questionText);
        tvMyAnswer      = findViewById(R.id.optionOne);
        tvRealAnswer    = findViewById(R.id.optionTwo);
        tvExplanation   = findViewById(R.id.optionThree);
        retryWrongButton = findViewById(R.id.retryWrongButton);
        retryQuizButton = findViewById(R.id.retryQuizButton);

        configureHeader();

        userAnswers = (ArrayList<Integer>) getIntent().getSerializableExtra("userAnswers");
        noteId = getIntent().getStringExtra("noteId");

        // 결과창으로부터 배달 완료된 원본 AI 생성형 퀴즈 데이터 세트 수신
        ArrayList<QuizModel> receivedQuizList = (ArrayList<QuizModel>) getIntent().getSerializableExtra("quizListSerializable");
        if (receivedQuizList != null) {
            quizList = receivedQuizList;
        }

        // 실데이터 검증을 기반으로 한 런타임 오답 선별 처리
        if (userAnswers != null && !userAnswers.isEmpty() && !quizList.isEmpty()) {
            wrongIndices.clear();
            for (int i = 0; i < quizList.size(); i++) {
                if (i < userAnswers.size() && userAnswers.get(i) != quizList.get(i).getAnswerIndex()) {
                    wrongIndices.add(i);
                }
            }
        }

        if (userAnswers != null && !userAnswers.isEmpty() && !quizList.isEmpty()) {
            showDetailPanel();
            displayWrongAnswer();
        } else {
            loadSavedWrongAnswers();
        }

        bindClick(R.id.backResult, v -> finish());
        bindClick(R.id.wrongHomeTab, v -> switchTopLevel(HomeActivity.class));
        bindClick(R.id.wrongMyPageTab, v -> switchTopLevel(MyPageActivity.class));
        bindClick(R.id.retryWrongButton, v -> retryCurrentWrongAnswer());

        if (retryQuizButton != null) {
            retryQuizButton.setOnClickListener(v -> {
                if (showingSavedWrongAnswers) {
                    if (savedWrongAnswers.isEmpty()) {
                        returnToSelectionOrHome();
                        return;
                    }
                    if (currentWrongIndex < savedWrongAnswers.size() - 1) {
                        currentWrongIndex++;
                        displaySavedWrongAnswer();
                    } else {
                        showShortToast("모든 오답 확인을 완료했습니다.");
                        returnToSelectionOrHome();
                    }
                    return;
                }
                if (wrongIndices.isEmpty()) {
                    returnHome();
                    return;
                }
                if (currentWrongIndex < wrongIndices.size() - 1) {
                    currentWrongIndex++;
                    displayWrongAnswer();
                } else {
                    showShortToast("모든 오답 확인을 완료했습니다.");
                    returnHome();
                }
            });
        }
    }

    private void returnHome() {
        goToAndClear(HomeActivity.class);
    }

    /** 저장된 오답 모드 완료 후: 노트 선택 화면으로 복귀하거나 홈으로 이동 */
    private void returnToSelectionOrHome() {
        if (noteGroupedWrongAnswers != null && !noteGroupedWrongAnswers.isEmpty()) {
            currentWrongIndex = 0;
            showingSavedWrongAnswers = false;
            showNoteSelectionList(cachedStudyNotes, noteGroupedWrongAnswers);
        } else {
            returnHome();
        }
    }

    private void retryCurrentWrongAnswer() {
        QuizModel quiz;
        String retryNoteId;

        if (showingSavedWrongAnswers) {
            if (savedWrongAnswers.isEmpty()) {
                showShortToast("다시 풀 오답이 없습니다.");
                return;
            }
            WrongAnswerModel wrongAnswer = savedWrongAnswers.get(currentWrongIndex);
            quiz = quizFromWrongAnswer(wrongAnswer);
            retryNoteId = wrongAnswer.getNoteId();
        } else {
            if (wrongIndices.isEmpty() || quizList.isEmpty()) {
                showShortToast("다시 풀 오답이 없습니다.");
                return;
            }
            quiz = quizList.get(wrongIndices.get(currentWrongIndex));
            retryNoteId = noteId;
        }

        if (retryNoteId == null || retryNoteId.trim().isEmpty()) {
            showShortToast("학습 기록 정보를 확인할 수 없습니다.");
            return;
        }

        ArrayList<QuizModel> retryQuizzes = new ArrayList<>();
        retryQuizzes.add(quiz);

        Intent intent = new Intent(this, QuizActivity.class);
        intent.putExtra("noteId", retryNoteId);
        intent.putExtra("retryMode", true);
        intent.putExtra("quizListSerializable", retryQuizzes);
        startActivity(intent);
    }

    private QuizModel quizFromWrongAnswer(WrongAnswerModel wrongAnswer) {
        return new QuizModel(
                wrongAnswer.getQuizId(),
                wrongAnswer.getNoteId(),
                wrongAnswer.getUserId(),
                wrongAnswer.getQuestion(),
                wrongAnswer.getOptions(),
                wrongAnswer.getCorrectIndex(),
                wrongAnswer.getExplanation(),
                wrongAnswer.getCreatedAt()
        );
    }

    private void configureHeader() {
        View backResult = findViewById(R.id.backResult);
        View eyebrow = findViewById(R.id.wrongAnswerEyebrow);
        boolean openedFromResult = getIntent().hasExtra("userAnswers")
                || getIntent().hasExtra("quizListSerializable");

        backResult.setVisibility(openedFromResult ? View.VISIBLE : View.GONE);
        eyebrow.setVisibility(openedFromResult ? View.GONE : View.VISIBLE);
    }

    private void loadSavedWrongAnswers() {
        String userId = authService.getCurrentUserId();
        if (userId == null) {
            showDetailPanel();
            displayWrongAnswer();
            return;
        }

        firestoreService.getWrongAnswers(userId, new FirestoreService.ListCallback<WrongAnswerModel>() {
            @Override
            public void onSuccess(List<WrongAnswerModel> items) {
                if (items.isEmpty()) {
                    savedWrongAnswers = new ArrayList<>();
                    showingSavedWrongAnswers = true;
                    showDetailPanel();
                    displaySavedWrongAnswer();
                    return;
                }

                // noteId 기준으로 그룹화 (최신순 유지: getWrongAnswers는 createdAt DESC 순)
                Map<String, List<WrongAnswerModel>> grouped = new LinkedHashMap<>();
                for (WrongAnswerModel item : items) {
                    String nId = item.getNoteId();
                    if (!grouped.containsKey(nId)) grouped.put(nId, new ArrayList<>());
                    grouped.get(nId).add(item);
                }

                // 학습 노트 제목 조회
                firestoreService.getStudyNotes(userId, new FirestoreService.ListCallback<StudyNoteModel>() {
                    @Override
                    public void onSuccess(List<StudyNoteModel> notes) {
                        showNoteSelectionList(notes, grouped);
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        // 제목 조회 실패 시 전체 오답 바로 표시
                        savedWrongAnswers = new ArrayList<>(items);
                        showingSavedWrongAnswers = true;
                        showDetailPanel();
                        displaySavedWrongAnswer();
                    }
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                showShortToast(errorMessage);
                showDetailPanel();
                displayWrongAnswer();
            }
        });
    }

    /** 노트 선택 목록을 표시한다. notes와 grouped는 재진입을 위해 캐싱된다. */
    private void showNoteSelectionList(List<StudyNoteModel> notes,
                                       Map<String, List<WrongAnswerModel>> grouped) {
        cachedStudyNotes = notes;
        noteGroupedWrongAnswers = grouped;

        Map<String, String> noteTitleById = new HashMap<>();
        if (notes != null) {
            for (StudyNoteModel note : notes) {
                noteTitleById.put(note.getId(), note.getTitle());
            }
        }

        noteSelectContainer.removeAllViews();
        int gapPx = getResources().getDimensionPixelSize(R.dimen.list_item_gap);
        int padPx = getResources().getDimensionPixelSize(R.dimen.card_padding);

        for (Map.Entry<String, List<WrongAnswerModel>> entry : grouped.entrySet()) {
            String nId = entry.getKey();
            List<WrongAnswerModel> wrongList = entry.getValue();
            String title = noteTitleById.containsKey(nId) ? noteTitleById.get(nId) : "학습 기록";

            TextView card = new TextView(this);
            card.setText(title + "\n오답 " + wrongList.size() + "문제");
            card.setContentDescription(title + ", 오답 " + wrongList.size() + "문제. 탭하여 오답 보기");
            card.setTextColor(ContextCompat.getColor(this, R.color.study_text));
            card.setTextSize(17f);
            card.setTypeface(null, Typeface.BOLD);
            card.setBackgroundResource(R.drawable.bg_card);
            card.setPadding(padPx, padPx, padPx, padPx);
            card.setClickable(true);
            card.setFocusable(true);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, gapPx, 0, 0);
            card.setLayoutParams(params);

            card.setOnClickListener(v -> {
                savedWrongAnswers = new ArrayList<>(wrongList);
                currentWrongIndex = 0;
                showingSavedWrongAnswers = true;
                showDetailPanel();
                displaySavedWrongAnswer();
            });

            noteSelectContainer.addView(card);
        }

        noteSelectPanel.setVisibility(View.VISIBLE);
        wrongAnswerDetailPanel.setVisibility(View.GONE);
    }

    private void showDetailPanel() {
        if (noteSelectPanel != null) noteSelectPanel.setVisibility(View.GONE);
        if (wrongAnswerDetailPanel != null) wrongAnswerDetailPanel.setVisibility(View.VISIBLE);
    }

    /**
     * AI 데이터 주입형 실시간 렌더링 함수
     */
    private void displayWrongAnswer() {
        if (wrongIndices.isEmpty() || quizList.isEmpty()) {
            if (tvWrongProgress != null) tvWrongProgress.setText("오답 0/0");
            if (tvWrongQuestion != null) tvWrongQuestion.setText("틀린 문제가 전혀 없습니다! 모든 문제를 마스터하셨습니다. 👍");
            if (tvMyAnswer != null) tvMyAnswer.setText("");
            if (tvRealAnswer != null) tvRealAnswer.setText("");
            if (tvExplanation != null) tvExplanation.setText("");
            if (retryWrongButton != null) retryWrongButton.setVisibility(View.GONE);
            if (retryQuizButton != null) retryQuizButton.setText("돌아가기");
            return;
        }

        if (retryWrongButton != null) retryWrongButton.setVisibility(View.VISIBLE);

        int originalIdx = wrongIndices.get(currentWrongIndex);
        QuizModel currentQuiz = quizList.get(originalIdx);

        if (tvWrongProgress != null) {
            tvWrongProgress.setText("오답 " + (currentWrongIndex + 1) + "/" + wrongIndices.size());
        }

        if (tvWrongQuestion != null) {
            tvWrongQuestion.setText("Q" + (originalIdx + 1) + ". " + currentQuiz.getQuestion());
        }

        if (userAnswers != null && originalIdx < userAnswers.size()) {
            int userSelection = userAnswers.get(originalIdx);
            if (tvMyAnswer != null && userSelection >= 0 && userSelection < currentQuiz.getOptions().size()) {
                tvMyAnswer.setText("✕ 내 답: " + currentQuiz.getOptions().get(userSelection));
            }
        } else {
            if (tvMyAnswer != null) tvMyAnswer.setText("✕ 내 답: 선택 안 함");
        }

        int correctIndex = currentQuiz.getAnswerIndex();
        if (tvRealAnswer != null && correctIndex >= 0 && correctIndex < currentQuiz.getOptions().size()) {
            tvRealAnswer.setText("✓ 정답: " + currentQuiz.getOptions().get(correctIndex));
        }

        if (tvExplanation != null) {
            tvExplanation.setText("💡 해설: " + currentQuiz.getExplanation());
        }

        if (retryQuizButton != null) {
            retryQuizButton.setText(currentWrongIndex < wrongIndices.size() - 1
                    ? "다음 오답 보기"
                    : "돌아가기");
        }
    }

    private void displaySavedWrongAnswer() {
        if (savedWrongAnswers.isEmpty()) {
            if (tvWrongProgress != null) tvWrongProgress.setText("오답 0/0");
            if (tvWrongQuestion != null) tvWrongQuestion.setText("저장된 오답이 없습니다.");
            if (tvMyAnswer != null) tvMyAnswer.setText("");
            if (tvRealAnswer != null) tvRealAnswer.setText("");
            if (tvExplanation != null) tvExplanation.setText("");
            if (retryWrongButton != null) retryWrongButton.setVisibility(View.GONE);
            if (retryQuizButton != null) retryQuizButton.setText("돌아가기");
            return;
        }

        if (retryWrongButton != null) retryWrongButton.setVisibility(View.VISIBLE);

        WrongAnswerModel current = savedWrongAnswers.get(currentWrongIndex);
        if (tvWrongProgress != null) {
            tvWrongProgress.setText("오답 " + (currentWrongIndex + 1) + "/" + savedWrongAnswers.size());
        }
        if (tvWrongQuestion != null) {
            tvWrongQuestion.setText("Q. " + current.getQuestion());
        }
        if (tvMyAnswer != null) {
            tvMyAnswer.setText("✕ 내 답: " + optionText(current, current.getSelectedIndex()));
        }
        if (tvRealAnswer != null) {
            tvRealAnswer.setText("✓ 정답: " + optionText(current, current.getCorrectIndex()));
        }
        if (tvExplanation != null) {
            tvExplanation.setText("💡 해설: " + current.getExplanation());
        }
        if (retryQuizButton != null) {
            retryQuizButton.setText(currentWrongIndex < savedWrongAnswers.size() - 1
                    ? "다음 오답 보기"
                    : "목록으로");
        }
    }

    private String optionText(WrongAnswerModel wrongAnswer, int index) {
        List<String> options = wrongAnswer.getOptions();
        if (index >= 0 && index < options.size()) {
            return options.get(index);
        }
        return "선택 안 함";
    }
}
