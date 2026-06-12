package com.example.studymate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.studymate.model.StudyNoteModel;
import com.example.studymate.model.UserStatsModel;
import com.example.studymate.service.AuthService;
import com.example.studymate.service.FirestoreService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends BaseActivity {
    private static final int RECENT_NOTE_LIMIT = 3;

    private final AuthService authService = new AuthService();
    private final FirestoreService firestoreService = new FirestoreService();

    private TextView totalStudyStatText;
    private TextView quizCountStatText;
    private TextView averageScoreStatText;
    private TextView emptyRecentNotesText;
    private LinearLayout recentNotesContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        totalStudyStatText = findViewById(R.id.totalStudyStatText);
        quizCountStatText = findViewById(R.id.quizCountStatText);
        averageScoreStatText = findViewById(R.id.averageScoreStatText);
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
        String userId = authService.getCurrentUserId();
        if (userId == null) {
            showEmptyHome();
            return;
        }

        firestoreService.getUserStats(userId, new FirestoreService.StatsCallback() {
            @Override
            public void onSuccess(UserStatsModel stats) {
                totalStudyStatText.setText(stats.getStudyNoteCount() + "\n총 학습");
                quizCountStatText.setText(stats.getQuizResultCount() + "\n퀴즈 풀이");
                averageScoreStatText.setText(stats.getAverageScore() + "%\n평균 정답률");
            }

            @Override
            public void onFailure(String errorMessage) {
                showShortToast(errorMessage);
            }
        });

        firestoreService.getStudyNotes(userId, new FirestoreService.ListCallback<StudyNoteModel>() {
            @Override
            public void onSuccess(List<StudyNoteModel> notes) {
                renderRecentNotes(notes);
            }

            @Override
            public void onFailure(String errorMessage) {
                showShortToast(errorMessage);
                renderRecentNotes(new ArrayList<>());
            }
        });
    }

    private void showEmptyHome() {
        totalStudyStatText.setText("0\n총 학습");
        quizCountStatText.setText("0\n퀴즈 풀이");
        averageScoreStatText.setText("0%\n평균 정답률");
        renderRecentNotes(new ArrayList<>());
    }

    private void renderRecentNotes(List<StudyNoteModel> notes) {
        recentNotesContainer.removeAllViews();

        if (notes == null || notes.isEmpty()) {
            emptyRecentNotesText.setVisibility(View.VISIBLE);
            recentNotesContainer.setVisibility(View.GONE);
            return;
        }

        emptyRecentNotesText.setVisibility(View.GONE);
        recentNotesContainer.setVisibility(View.VISIBLE);

        int count = Math.min(notes.size(), RECENT_NOTE_LIMIT);
        for (int i = 0; i < count; i++) {
            StudyNoteModel note = notes.get(i);
            TextView card = new TextView(this);
            card.setText(buildRecentNoteText(note));
            card.setTextColor(getColor(R.color.study_text));
            card.setTextSize(17);
            card.setTypeface(card.getTypeface(), android.graphics.Typeface.BOLD);
            card.setBackgroundResource(R.drawable.bg_card);
            card.setPadding(
                    getResources().getDimensionPixelSize(R.dimen.card_padding),
                    getResources().getDimensionPixelSize(R.dimen.card_padding),
                    getResources().getDimensionPixelSize(R.dimen.card_padding),
                    getResources().getDimensionPixelSize(R.dimen.card_padding)
            );
            card.setOnClickListener(v -> openStudyNote(note));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            int itemGap = getResources().getDimensionPixelSize(R.dimen.list_item_gap);
            params.setMargins(0, itemGap, 0, 0);
            recentNotesContainer.addView(card, params);
        }
    }

    private String buildRecentNoteText(StudyNoteModel note) {
        String subject = note.getSubject() == null || note.getSubject().trim().isEmpty()
                ? "과목 없음"
                : note.getSubject();
        String date = note.getCreatedAt() == null
                ? "날짜 없음"
                : new SimpleDateFormat("yyyy.MM.dd", Locale.KOREA).format(note.getCreatedAt());
        return note.getTitle() + "\n\n" + subject + " · " + date;
    }

    private void openStudyNote(StudyNoteModel note) {
        Intent intent = new Intent(this, SummaryResultActivity.class);
        intent.putExtra("noteId", note.getId());
        intent.putExtra("title", note.getTitle());
        intent.putExtra("subject", note.getSubject());
        intent.putExtra("content", note.getOriginalText());
        intent.putStringArrayListExtra("summary", new ArrayList<>(note.getSummary()));
        intent.putStringArrayListExtra("keywords", new ArrayList<>(note.getKeywords()));
        startActivity(intent);
    }
}
