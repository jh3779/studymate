package com.example.studymate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.studymate.model.QuizModel;
import com.example.studymate.service.AiService;
import com.example.studymate.service.AuthService;
import com.example.studymate.service.FirestoreService;

import java.util.ArrayList;
import java.util.List;

public class SummaryResultActivity extends BaseActivity {

    private final AiService aiService = new AiService();
    private final AuthService authService = new AuthService();
    private final FirestoreService firestoreService = new FirestoreService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary_result);

        Intent intent = getIntent();
        String noteId = intent.getStringExtra("noteId");
        String title = intent.getStringExtra("title");
        String subject = intent.getStringExtra("subject");
        ArrayList<String> summary = intent.getStringArrayListExtra("summary");
        ArrayList<String> keywords = intent.getStringArrayListExtra("keywords");
        String content = intent.getStringExtra("content");

        bindSummaryData(title, subject, summary, keywords);

        bindClick(R.id.backInput, v -> finish());

        Button createQuizButton = findViewById(R.id.createQuizButton);
        TextView quizLoadingBox = findViewById(R.id.quizLoadingBox);

        createQuizButton.setOnClickListener(v -> {
            String quizSource = (content != null && !content.trim().isEmpty())
                    ? content
                    : (summary != null && !summary.isEmpty()
                    ? android.text.TextUtils.join("\n", summary)
                    : "");

            if (quizSource.trim().isEmpty()) {
                showShortToast("퀴즈를 만들 학습 내용이 없습니다.");
                return;
            }

            quizLoadingBox.setVisibility(View.VISIBLE);
            createQuizButton.setEnabled(false);
            createQuizButton.setText("퀴즈 생성 중...");

            aiService.generateQuizzes(quizSource, new AiService.QuizCallback() {
                @Override
                public void onSuccess(List<AiService.QuizItem> quizzes) {
                    saveQuizzesAndStart(noteId, quizzes, quizLoadingBox, createQuizButton);
                }

                @Override
                public void onFailure(String errorMessage) {
                    quizLoadingBox.setVisibility(View.GONE);
                    createQuizButton.setEnabled(true);
                    createQuizButton.setText("퀴즈 생성하기");
                    showShortToast(errorMessage);
                }
            });
        });
    }

    private void saveQuizzesAndStart(
            String noteId,
            List<AiService.QuizItem> quizzes,
            TextView quizLoadingBox,
            Button createQuizButton
    ) {
        String userId = authService.getCurrentUserId();
        if (noteId == null || noteId.trim().isEmpty() || userId == null) {
            quizLoadingBox.setVisibility(View.GONE);
            createQuizButton.setEnabled(true);
            createQuizButton.setText("퀴즈 생성하기");
            showShortToast("학습 기록 정보를 확인할 수 없습니다.");
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

        firestoreService.saveQuizzes(quizModels, new FirestoreService.SaveListCallback() {
            @Override
            public void onSuccess(List<String> documentIds) {
                quizLoadingBox.setVisibility(View.GONE);
                createQuizButton.setEnabled(true);
                createQuizButton.setText("퀴즈 생성하기");

                Intent quizIntent = new Intent(SummaryResultActivity.this, QuizActivity.class);
                quizIntent.putExtra("noteId", noteId);
                quizIntent.putExtra("quizzesJson", quizzesToJson(quizModels));
                startActivity(quizIntent);
            }

            @Override
            public void onFailure(String errorMessage) {
                quizLoadingBox.setVisibility(View.GONE);
                createQuizButton.setEnabled(true);
                createQuizButton.setText("퀴즈 생성하기");
                showShortToast(errorMessage);
            }
        });
    }

    private void bindSummaryData(String title, String subject,
                                  List<String> summary, List<String> keywords) {
        TextView titleView = findViewById(R.id.summaryTitle);
        TextView subjectView = findViewById(R.id.summarySubject);
        TextView summaryText = findViewById(R.id.summaryText);
        LinearLayout keywordsContainer = findViewById(R.id.keywordsContainer);

        if (title != null) titleView.setText(title);
        if (subject != null && !subject.isEmpty()) {
            subjectView.setText("과목: " + subject);
        } else {
            subjectView.setVisibility(View.GONE);
        }

        if (summary != null) {
            summaryText.setText(android.text.TextUtils.join("\n", summary));
        }

        if (keywords != null) {
            for (String keyword : keywords) {
                TextView chip = new TextView(this);
                chip.setText("# " + keyword);
                chip.setTextColor(getResources().getColor(R.color.study_text, null));
                chip.setTextSize(16);
                chip.setBackground(getResources().getDrawable(R.drawable.bg_chip, null));
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMarginEnd(16);
                chip.setLayoutParams(params);
                keywordsContainer.addView(chip);
            }
        }
    }

    // QuizItem 리스트를 JSON 문자열로 직렬화해서 QuizActivity에 전달
    private String quizzesToJson(List<QuizModel> quizzes) {
        try {
            org.json.JSONArray arr = new org.json.JSONArray();
            for (QuizModel item : quizzes) {
                org.json.JSONObject obj = new org.json.JSONObject();
                obj.put("id", item.getId());
                obj.put("question", item.getQuestion());
                org.json.JSONArray options = new org.json.JSONArray();
                for (String opt : item.getOptions()) options.put(opt);
                obj.put("options", options);
                obj.put("answerIndex", item.getAnswerIndex());
                obj.put("explanation", item.getExplanation());
                arr.put(obj);
            }
            return arr.toString();
        } catch (Exception e) {
            return "[]";
        }
    }
}
