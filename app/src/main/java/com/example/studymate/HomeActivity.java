package com.example.studymate;

import android.content.Intent;
import android.os.Bundle;
import android.graphics.Color;
import android.view.View;
import android.view.Gravity;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.studymate.model.QuizResultModel;
import com.example.studymate.model.StudyNoteModel;
import com.example.studymate.model.UserStatsModel;
import com.example.studymate.service.AuthService;
import com.example.studymate.service.FirestoreService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeActivity extends BaseActivity {
    private static final int RECENT_NOTE_LIMIT = 3;

    private final AuthService authService = new AuthService();
    private final FirestoreService firestoreService = new FirestoreService();

    private TextView totalStudyStatText;
    private TextView quizCountStatText;
    private TextView averageScoreStatText;
    private TextView homeStatsStatusText;
    private TextView emptyRecentNotesText;
    private LinearLayout recentNotesContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        totalStudyStatText = findViewById(R.id.totalStudyStatText);
        quizCountStatText = findViewById(R.id.quizCountStatText);
        averageScoreStatText = findViewById(R.id.averageScoreStatText);
        homeStatsStatusText = findViewById(R.id.homeStatsStatusText);
        emptyRecentNotesText = findViewById(R.id.emptyRecentNotesText);
        recentNotesContainer = findViewById(R.id.recentNotesContainer);

        bindClick(R.id.totalStudyStatText, v -> goTo(StudyHistoryActivity.class));
        bindClick(R.id.startStudyButton, v -> goTo(StudyInputActivity.class));
        bindClick(R.id.wrongTab, v -> switchTopLevel(WrongAnswerActivity.class));
        bindClick(R.id.myPageTab, v -> switchTopLevel(MyPageActivity.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHomeData();
    }

    private void loadHomeData() {
        showHomeLoading();
        String userId = authService.getCurrentUserId();
        if (userId == null) {
            showSignedOutHome();
            return;
        }

        firestoreService.getUserStats(userId, new FirestoreService.StatsCallback() {
            @Override
            public void onSuccess(UserStatsModel stats) {
                totalStudyStatText.setText(stats.getStudyNoteCount() + "\n총 학습");
                quizCountStatText.setText(stats.getQuizResultCount() + "\n퀴즈 풀이");
                averageScoreStatText.setText(stats.getAverageScore() + "%\n평균 정답률");
                homeStatsStatusText.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(String errorMessage) {
                showStatsError(errorMessage);
            }
        });

        firestoreService.getStudyNotes(userId, new FirestoreService.ListCallback<StudyNoteModel>() {
            @Override
            public void onSuccess(List<StudyNoteModel> notes) {
                loadRecentNoteScores(userId, notes);
            }

            @Override
            public void onFailure(String errorMessage) {
                showRecentNotesError(errorMessage);
            }
        });
    }

    private void loadRecentNoteScores(String userId, List<StudyNoteModel> notes) {
        firestoreService.getQuizResults(userId, new FirestoreService.ListCallback<QuizResultModel>() {
            @Override
            public void onSuccess(List<QuizResultModel> results) {
                renderRecentNotes(notes, latestScoresByNote(results));
            }

            @Override
            public void onFailure(String errorMessage) {
                renderRecentNotes(notes, new HashMap<>());
                showRecentStatus(
                        "정답률을 불러오지 못했습니다. 눌러서 다시 시도해주세요.",
                        true
                );
            }
        });
    }

    private void showHomeLoading() {
        totalStudyStatText.setText("...\n총 학습");
        quizCountStatText.setText("...\n퀴즈 풀이");
        averageScoreStatText.setText("...\n평균 정답률");
        homeStatsStatusText.setText("학습 통계를 불러오는 중입니다.");
        homeStatsStatusText.setTextColor(getColor(R.color.study_text_muted));
        homeStatsStatusText.setOnClickListener(null);
        homeStatsStatusText.setClickable(false);
        homeStatsStatusText.setVisibility(View.VISIBLE);

        recentNotesContainer.removeAllViews();
        recentNotesContainer.setVisibility(View.GONE);
        showRecentStatus("최근 학습 기록을 불러오는 중입니다.", false);
    }

    private void showSignedOutHome() {
        totalStudyStatText.setText("0\n총 학습");
        quizCountStatText.setText("0\n퀴즈 풀이");
        averageScoreStatText.setText("0%\n평균 정답률");
        homeStatsStatusText.setVisibility(View.GONE);
        showRecentStatus("로그인 사용자 정보를 확인할 수 없습니다.", false);
    }

    private void showStatsError(String errorMessage) {
        totalStudyStatText.setText("-\n총 학습");
        quizCountStatText.setText("-\n퀴즈 풀이");
        averageScoreStatText.setText("-\n평균 정답률");
        homeStatsStatusText.setText(errorMessage + "\n눌러서 다시 시도해주세요.");
        homeStatsStatusText.setTextColor(getColor(R.color.study_error));
        homeStatsStatusText.setClickable(true);
        homeStatsStatusText.setOnClickListener(v -> loadHomeData());
        homeStatsStatusText.setVisibility(View.VISIBLE);
    }

    private void showRecentNotesError(String errorMessage) {
        recentNotesContainer.removeAllViews();
        recentNotesContainer.setVisibility(View.GONE);
        showRecentStatus(errorMessage + "\n눌러서 다시 시도해주세요.", true);
    }

    private void showRecentStatus(String message, boolean retryable) {
        emptyRecentNotesText.setText(message);
        emptyRecentNotesText.setTextColor(getColor(
                retryable ? R.color.study_error : R.color.study_text
        ));
        emptyRecentNotesText.setClickable(retryable);
        emptyRecentNotesText.setFocusable(retryable);
        emptyRecentNotesText.setOnClickListener(retryable ? v -> loadHomeData() : null);
        emptyRecentNotesText.setVisibility(View.VISIBLE);
    }

    private void renderRecentNotes(
            List<StudyNoteModel> notes,
            Map<String, Integer> latestScores
    ) {
        recentNotesContainer.removeAllViews();

        if (notes == null || notes.isEmpty()) {
            recentNotesContainer.setVisibility(View.GONE);
            showRecentStatus(
                    "아직 학습 기록이 없습니다.\n\n오늘의 학습을 시작해 보세요.",
                    false
            );
            return;
        }

        emptyRecentNotesText.setVisibility(View.GONE);
        recentNotesContainer.setVisibility(View.VISIBLE);

        int count = Math.min(notes.size(), RECENT_NOTE_LIMIT);
        for (int i = 0; i < count; i++) {
            StudyNoteModel note = notes.get(i);
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setBackgroundResource(R.drawable.bg_card);
            int padding = getResources().getDimensionPixelSize(R.dimen.card_padding);
            row.setPadding(padding, padding, padding, padding);

            TextView card = new TextView(this);
            card.setText(buildRecentNoteText(note, latestScores.get(note.getId())));
            card.setTextColor(getColor(R.color.study_text));
            card.setTextSize(17);
            card.setTypeface(card.getTypeface(), android.graphics.Typeface.BOLD);
            card.setPadding(0, 0, dpToPx(8), 0);
            card.setClickable(true);
            card.setFocusable(true);
            card.setContentDescription(buildRecentNoteText(
                    note,
                    latestScores.get(note.getId())
            ) + ". 학습 기록 열기");
            card.setOnClickListener(v -> openStudyNote(note));
            row.addView(card, new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
            ));

            ImageButton deleteButton = createDeleteButton(
                    safeTitle(note) + " 학습 기록 삭제"
            );
            deleteButton.setOnClickListener(v -> confirmDeleteStudyNote(note));
            row.addView(deleteButton);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            int itemGap = getResources().getDimensionPixelSize(R.dimen.list_item_gap);
            params.setMargins(0, itemGap, 0, 0);
            recentNotesContainer.addView(row, params);
        }
    }

    private ImageButton createDeleteButton(String description) {
        ImageButton button = new ImageButton(this);
        int size = dpToPx(48);
        button.setLayoutParams(new LinearLayout.LayoutParams(size, size));
        button.setImageResource(R.drawable.ic_delete);
        button.setBackgroundColor(Color.TRANSPARENT);
        button.setContentDescription(description);
        button.setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12));
        return button;
    }

    private void confirmDeleteStudyNote(StudyNoteModel note) {
        showDeleteConfirmation(
                "학습 기록을 삭제할까요?",
                "'" + safeTitle(note)
                        + "'와 연결된 퀴즈, 결과, 오답 기록도 함께 삭제됩니다.",
                () -> deleteStudyNote(note)
        );
    }

    private void deleteStudyNote(StudyNoteModel note) {
        String userId = authService.getCurrentUserId();
        firestoreService.deleteStudyNoteWithRelatedData(
                note.getId(),
                userId,
                new FirestoreService.DeleteCallback() {
                    @Override
                    public void onSuccess() {
                        showShortToast("학습 기록을 삭제했습니다.");
                        loadHomeData();
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        showShortToast(errorMessage);
                    }
                }
        );
    }

    private String safeTitle(StudyNoteModel note) {
        return note.getTitle() == null || note.getTitle().trim().isEmpty()
                ? "제목 없는 학습"
                : note.getTitle().trim();
    }

    private Map<String, Integer> latestScoresByNote(List<QuizResultModel> results) {
        Map<String, QuizResultModel> latestResults = new HashMap<>();
        if (results == null) {
            return new HashMap<>();
        }

        for (QuizResultModel result : results) {
            if (result == null || result.getNoteId() == null || result.getNoteId().trim().isEmpty()) {
                continue;
            }
            QuizResultModel current = latestResults.get(result.getNoteId());
            if (current == null || isNewer(result, current)) {
                latestResults.put(result.getNoteId(), result);
            }
        }

        Map<String, Integer> scores = new HashMap<>();
        for (Map.Entry<String, QuizResultModel> entry : latestResults.entrySet()) {
            scores.put(entry.getKey(), entry.getValue().getScore());
        }
        return scores;
    }

    private boolean isNewer(QuizResultModel candidate, QuizResultModel current) {
        if (candidate.getCreatedAt() == null) {
            return false;
        }
        return current.getCreatedAt() == null
                || candidate.getCreatedAt().after(current.getCreatedAt());
    }

    private String buildRecentNoteText(StudyNoteModel note, Integer latestScore) {
        String subject = note.getSubject() == null || note.getSubject().trim().isEmpty()
                ? "과목 없음"
                : note.getSubject();
        String date = note.getCreatedAt() == null
                ? "날짜 없음"
                : new SimpleDateFormat("yyyy.MM.dd", Locale.KOREA).format(note.getCreatedAt());
        String scoreText = latestScore == null
                ? "퀴즈 미응시"
                : "최근 정답률 " + latestScore + "%";
        return note.getTitle() + "\n\n" + subject + " · " + date + "\n" + scoreText;
    }

    private void openStudyNote(StudyNoteModel note) {
        Intent intent = new Intent(this, SummaryResultActivity.class);
        intent.putExtra("noteId", note.getId());
        intent.putExtra("title", note.getTitle());
        intent.putExtra("subject", note.getSubject());
        intent.putExtra("content", note.getOriginalText());
        intent.putExtra("summaryEntryPoint", "home");
        intent.putStringArrayListExtra("summary", new ArrayList<>(note.getSummary()));
        intent.putStringArrayListExtra("keywords", new ArrayList<>(note.getKeywords()));
        startActivity(intent);
    }
}
