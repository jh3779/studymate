package com.example.studymate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.studymate.service.AiService;

public class StudyInputActivity extends BaseActivity {

    private EditText titleInput;
    private EditText subjectInput;
    private EditText contentInput;
    private TextView errorText;
    private TextView loadingBox;
    private Button generateButton;

    private final AiService aiService = new AiService();

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

        errorText.setVisibility(View.GONE);
        loadingBox.setVisibility(View.VISIBLE);
        generateButton.setEnabled(false);
        generateButton.setText("AI 요약 생성 중...");

        aiService.generateSummary(content, new AiService.SummaryCallback() {
            @Override
            public void onSuccess(AiService.SummaryResult result) {
                loadingBox.setVisibility(View.GONE);
                generateButton.setEnabled(true);
                generateButton.setText("AI 요약 생성");

                Intent intent = new Intent(StudyInputActivity.this, SummaryResultActivity.class);
                intent.putExtra("title", title);
                intent.putExtra("subject", subject.isEmpty() ? "" : subject);
                intent.putExtra("content", content);
                intent.putStringArrayListExtra("summary", new java.util.ArrayList<>(result.summary));
                intent.putStringArrayListExtra("keywords", new java.util.ArrayList<>(result.keywords));
                startActivity(intent);
            }

            @Override
            public void onFailure(String errorMessage) {
                loadingBox.setVisibility(View.GONE);
                generateButton.setEnabled(true);
                generateButton.setText("AI 요약 생성");
                showError("⚠ " + errorMessage);
            }
        });
    }

    private void showError(String message) {
        errorText.setText(message);
        errorText.setVisibility(View.VISIBLE);
    }
}
