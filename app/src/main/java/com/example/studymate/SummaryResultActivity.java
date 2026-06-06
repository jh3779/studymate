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
            String quizSource = (summary != null && !summary.isEmpty())
                    ? android.text.TextUtils.join("\n", summary)
                    : content;

            quizLoadingBox.setVisibility(View.VISIBLE);
            createQuizButton.setEnabled(false);
            createQuizButton.setText("퀴즈 생성 중...");

            aiService.generateQuizzes(quizSource, new AiService.QuizCallback() {
                @Override
                public void onSuccess(List<AiService.QuizItem> quizzes) {
                    saveQuizzes(noteId, quizzes, createQuizButton, quizLoadingBox);
                }

                @Override
                public void onFailure(String errorMessage) {
                    setQuizLoading(false, createQuizButton, quizLoadingBox);
                    showShortToast(errorMessage);
                }
            });
        });
    }

    private void saveQuizzes(
            String noteId,
            List<AiService.QuizItem> generatedQuizzes,
            Button createQuizButton,
            TextView quizLoadingBox
    ) {
        String userId = authService.getCurrentUserId();
        if (noteId == null || noteId.isEmpty() || userId == null) {
            setQuizLoading(false, createQuizButton, quizLoadingBox);
            showShortToast("학습 기록 또는 로그인 정보를 확인할 수 없습니다.");
            return;
        }

        quizLoadingBox.setText("⏳ 퀴즈 저장 중...\n잠시만 기다려주세요");
        createQuizButton.setText("퀴즈 저장 중...");

        List<QuizModel> quizzes = new ArrayList<>();
        for (AiService.QuizItem item : generatedQuizzes) {
            quizzes.add(new QuizModel(
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

        firestoreService.saveQuizzes(quizzes, new FirestoreService.SaveListCallback() {
            @Override
            public void onSuccess(List<String> documentIds) {
                setQuizLoading(false, createQuizButton, quizLoadingBox);

                Intent quizIntent = new Intent(
                        SummaryResultActivity.this,
                        QuizActivity.class
                );
                quizIntent.putExtra("noteId", noteId);
                quizIntent.putExtra("quizzesJson", quizzesToJson(quizzes));
                startActivity(quizIntent);
            }

            @Override
            public void onFailure(String errorMessage) {
                setQuizLoading(false, createQuizButton, quizLoadingBox);
                showShortToast("퀴즈 저장에 실패했습니다. " + errorMessage);
            }
        });
    }

    private void setQuizLoading(
            boolean loading,
            Button createQuizButton,
            TextView quizLoadingBox
    ) {
        quizLoadingBox.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (!loading) {
            quizLoadingBox.setText("⏳ AI 퀴즈 생성 중...\n잠시만 기다려주세요");
        }
        createQuizButton.setEnabled(!loading);
        createQuizButton.setText(loading ? "퀴즈 생성 중..." : "퀴즈 생성하기");
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

    private String quizzesToJson(List<QuizModel> quizzes) {
        try {
            org.json.JSONArray arr = new org.json.JSONArray();
            for (QuizModel item : quizzes) {
                org.json.JSONObject obj = new org.json.JSONObject();
                obj.put("id", item.getId());
                obj.put("noteId", item.getNoteId());
                obj.put("userId", item.getUserId());
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
