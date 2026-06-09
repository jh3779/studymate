package com.example.studymate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.studymate.model.StudyNoteModel;
import com.example.studymate.service.AiService;
import com.example.studymate.service.AuthService;
import com.example.studymate.service.FirestoreService;

import java.util.ArrayList;

public class StudyInputActivity extends BaseActivity {

    private EditText titleInput;
    private EditText subjectInput;
    private EditText contentInput;
    private TextView errorText;
    private TextView loadingBox;
    private Button generateButton;

    private final AiService aiService = new AiService();
    private final AuthService authService = new AuthService();
    private final FirestoreService firestoreService = new FirestoreService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study_input);

        titleInput = findViewById(R.id.titleInput);
        subjectInput = findViewById(R.id.subjectInput);
        contentInput = findViewById(R.id.contentInput);
        errorText = findViewById(R.id.inputErrorText);
        loadingBox = findViewById(R.id.loadingBox);
        generateButton = findViewById(R.id.generateSummaryButton);

        bindClick(R.id.backHome, v -> finish());
        bindClick(R.id.generateSummaryButton, v -> handleGenerateSummary());
    }

    private void handleGenerateSummary() {
        String title = titleInput.getText().toString().trim();
        String subject = subjectInput.getText().toString().trim();
        String content = contentInput.getText().toString().trim();

        if (title.isEmpty()) {
            showError("⚠ 제목을 입력해주세요.");
            return;
        }
        if (content.length() < 30) {
            showError("⚠ 학습 내용은 30자 이상 입력해주세요. 현재 " + content.length() + "자입니다.");
            return;
        }
        if (content.length() > 5000) {
            showError("⚠ 학습 내용은 5000자 이하로 입력해주세요. 현재 " + content.length() + "자입니다.");
            return;
        }

        errorText.setVisibility(View.GONE);
        loadingBox.setVisibility(View.VISIBLE);
        generateButton.setEnabled(false);
        generateButton.setText("AI 요약 생성 중...");

        aiService.generateSummary(content, new AiService.SummaryCallback() {
            @Override
            public void onSuccess(AiService.SummaryResult result) {
                saveStudyNote(title, subject, content, result);
            }

            @Override
            public void onFailure(String errorMessage) {
                setLoading(false);
                showError("⚠ " + errorMessage);
            }
        });
    }

    private void saveStudyNote(
            String title,
            String subject,
            String content,
            AiService.SummaryResult result
    ) {
        String userId = authService.getCurrentUserId();
        if (userId == null) {
            setLoading(false);
            showError("⚠ 로그인이 만료되었습니다. 다시 로그인해주세요.");
            return;
        }

        loadingBox.setText("⏳ 학습 기록 저장 중...\n잠시만 기다려주세요");
        generateButton.setText("학습 기록 저장 중...");

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
                setLoading(false);

                Intent intent = new Intent(StudyInputActivity.this, SummaryResultActivity.class);
                intent.putExtra("noteId", documentId);
                intent.putExtra("title", title);
                intent.putExtra("subject", subject);
                intent.putExtra("content", content);
                intent.putStringArrayListExtra("summary", new ArrayList<>(result.summary));
                intent.putStringArrayListExtra("keywords", new ArrayList<>(result.keywords));
                startActivity(intent);
            }

            @Override
            public void onFailure(String errorMessage) {
                setLoading(false);
                showError("⚠ 요약은 생성됐지만 저장에 실패했습니다. " + errorMessage);
            }
        });
    }

    private void setLoading(boolean loading) {
        loadingBox.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (!loading) {
            loadingBox.setText("⏳ AI 요약 생성 중...\n잠시만 기다려주세요");
        }
        generateButton.setEnabled(!loading);
        generateButton.setText(loading ? "AI 요약 생성 중..." : "AI 요약 생성");
        titleInput.setEnabled(!loading);
        subjectInput.setEnabled(!loading);
        contentInput.setEnabled(!loading);
    }

    private void showError(String message) {
        errorText.setText(message);
        errorText.setVisibility(View.VISIBLE);
    }
}
