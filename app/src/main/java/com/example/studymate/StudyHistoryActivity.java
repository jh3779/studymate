package com.example.studymate;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.studymate.model.StudyNoteModel;
import com.example.studymate.service.AuthService;
import com.example.studymate.service.FirestoreService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StudyHistoryActivity extends BaseActivity {
    private final AuthService authService = new AuthService();
    private final FirestoreService firestoreService = new FirestoreService();

    private TextView historyStatusText;
    private LinearLayout studyHistoryContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study_history);

        historyStatusText = findViewById(R.id.historyStatusText);
        studyHistoryContainer = findViewById(R.id.studyHistoryContainer);

        bindClick(R.id.studyHistoryBack, v -> finish());
        loadStudyHistory();
    }

    private void loadStudyHistory() {
        String userId = authService.getCurrentUserId();
        if (userId == null) {
            renderStudyHistory(new ArrayList<>());
            return;
        }

        historyStatusText.setText("학습 기록을 불러오는 중입니다.");
        firestoreService.getStudyNotes(userId, new FirestoreService.ListCallback<StudyNoteModel>() {
            @Override
            public void onSuccess(List<StudyNoteModel> notes) {
                renderStudyHistory(notes);
            }

            @Override
            public void onFailure(String errorMessage) {
                historyStatusText.setText("학습 기록을 불러오지 못했습니다.");
                studyHistoryContainer.removeAllViews();
                showShortToast(errorMessage);
            }
        });
    }

    private void renderStudyHistory(List<StudyNoteModel> notes) {
        studyHistoryContainer.removeAllViews();

        if (notes == null || notes.isEmpty()) {
            historyStatusText.setText("아직 저장된 학습 기록이 없습니다.");
            return;
        }

        historyStatusText.setText("총 " + notes.size() + "개의 학습 기록");
        for (StudyNoteModel note : notes) {
            TextView card = new TextView(this);
            card.setText(buildStudyNoteText(note));
            card.setTextColor(getColor(R.color.study_text));
            card.setTextSize(17);
            card.setTypeface(card.getTypeface(), Typeface.BOLD);
            card.setLineSpacing(0, 1.15f);
            card.setBackgroundResource(R.drawable.bg_card);
            int padding = getResources().getDimensionPixelSize(R.dimen.card_padding);
            card.setPadding(padding, padding, padding, padding);
            card.setClickable(true);
            card.setFocusable(true);
            card.setContentDescription(safeTitle(note) + " 학습 기록 열기");
            card.setOnClickListener(v -> openStudyNote(note));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(
                    0,
                    getResources().getDimensionPixelSize(R.dimen.list_item_gap),
                    0,
                    0
            );
            studyHistoryContainer.addView(card, params);
        }
    }

    private String buildStudyNoteText(StudyNoteModel note) {
        String subject = note.getSubject() == null || note.getSubject().trim().isEmpty()
                ? "과목 없음"
                : note.getSubject().trim();
        String date = note.getCreatedAt() == null
                ? "날짜 없음"
                : new SimpleDateFormat("yyyy.MM.dd", Locale.KOREA).format(note.getCreatedAt());
        String summary = note.getSummary() == null || note.getSummary().isEmpty()
                ? "요약 없음"
                : note.getSummary().get(0);
        return safeTitle(note) + "\n\n" + subject + " · " + date + "\n" + summary;
    }

    private String safeTitle(StudyNoteModel note) {
        return note.getTitle() == null || note.getTitle().trim().isEmpty()
                ? "제목 없는 학습"
                : note.getTitle().trim();
    }

    private void openStudyNote(StudyNoteModel note) {
        Intent intent = new Intent(this, SummaryResultActivity.class);
        intent.putExtra("noteId", note.getId());
        intent.putExtra("title", note.getTitle());
        intent.putExtra("subject", note.getSubject());
        intent.putExtra("content", note.getOriginalText());
        intent.putExtra("summaryEntryPoint", "history");
        intent.putStringArrayListExtra("summary", new ArrayList<>(note.getSummary()));
        intent.putStringArrayListExtra("keywords", new ArrayList<>(note.getKeywords()));
        startActivity(intent);
    }
}
