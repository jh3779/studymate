package com.example.studymate;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class StudyInputActivity extends BaseActivity {
    private EditText titleInput;
    private EditText contentInput;
    private TextView errorText;
    private TextView loadingBox;
    private Button generateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study_input);

        titleInput = findViewById(R.id.titleInput);
        contentInput = findViewById(R.id.contentInput);
        errorText = findViewById(R.id.inputErrorText);
        loadingBox = findViewById(R.id.loadingBox);
        generateButton = findViewById(R.id.generateSummaryButton);

        titleInput.setText("데이터베이스 기본키 정리");
        contentInput.setText("기본키는 데이터베이스 테이블에서 각 행을 고유하게 식별하기 위해 사용하는 속성이다. 기본키 값은 중복될 수 없으며 NULL 값을 가질 수 없다.");

        bindClick(R.id.backHome, v -> finish());
        bindClick(R.id.generateSummaryButton, v -> handleGenerateSummary());
    }

    private void handleGenerateSummary() {
        String title = titleInput.getText().toString().trim();
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

        // TODO: 윤재이 담당 AIService 요약 연동 시 더미 지연 처리를 교체한다.
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            generateButton.setEnabled(true);
            generateButton.setText("AI 요약 생성");
            goTo(SummaryResultActivity.class);
        }, 700);
    }

    private void showError(String message) {
        errorText.setText(message);
        errorText.setVisibility(View.VISIBLE);
    }
}
