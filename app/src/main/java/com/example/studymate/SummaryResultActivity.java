package com.example.studymate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.studymate.model.QuizModel;

import java.util.List;

public class SummaryResultActivity extends BaseActivity {
    private SummaryResultViewModel viewModel;
    private Button createQuizButton;
    private TextView quizLoadingBox;
    private String noteId;
    private String quizSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary_result);

        Intent intent = getIntent();
        noteId = intent.getStringExtra("noteId");
        String title = intent.getStringExtra("title");
        String subject = intent.getStringExtra("subject");
        java.util.ArrayList<String> summary = intent.getStringArrayListExtra("summary");
        java.util.ArrayList<String> keywords = intent.getStringArrayListExtra("keywords");
        String content = intent.getStringExtra("content");

        bindSummaryData(title, subject, summary, keywords);
        quizSource = (content != null && !content.trim().isEmpty())
                ? content
                : (summary != null && !summary.isEmpty()
                ? android.text.TextUtils.join("\n", summary)
                : "");

        bindClick(R.id.backInput, v -> finish());

        createQuizButton = findViewById(R.id.createQuizButton);
        quizLoadingBox = findViewById(R.id.quizLoadingBox);
        viewModel = new ViewModelProvider(this).get(SummaryResultViewModel.class);
        viewModel.getState().observe(this, this::renderQuizState);

        createQuizButton.setOnClickListener(
                v -> viewModel.generateQuizzes(noteId, quizSource)
        );
    }

    private void renderQuizState(SummaryResultViewModel.State state) {
        switch (state.status) {
            case GENERATING:
                setQuizLoading(true, "⏳ AI 퀴즈 생성 중...\n잠시만 기다려주세요", "퀴즈 생성 중...");
                break;
            case SAVING:
                setQuizLoading(true, "⏳ 퀴즈 저장 중...\n잠시만 기다려주세요", "퀴즈 저장 중...");
                break;
            case SUCCESS:
                List<QuizModel> quizzes = state.quizzes;
                viewModel.consumeTerminalState();
                setQuizLoading(false, "", "");
                Intent quizIntent = new Intent(SummaryResultActivity.this, QuizActivity.class);
                quizIntent.putExtra("noteId", noteId);
                quizIntent.putExtra("quizzesJson", quizzesToJson(quizzes));
                startActivity(quizIntent);
                break;
            case ERROR:
                String errorMessage = state.errorMessage;
                viewModel.consumeTerminalState();
                setQuizLoading(false, "", "");
                showShortToast(errorMessage);
                break;
            case IDLE:
            default:
                setQuizLoading(false, "", "");
                break;
        }
    }

    private void setQuizLoading(boolean loading, String message, String buttonText) {
        quizLoadingBox.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (loading) {
            quizLoadingBox.setText(message);
        }
        createQuizButton.setEnabled(!loading);
        createQuizButton.setText(loading ? buttonText : "퀴즈 생성하기");
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
                chip.setTextColor(getColor(R.color.study_text));
                chip.setTextSize(16);
                chip.setBackground(ResourcesCompat.getDrawable(
                        getResources(),
                        R.drawable.bg_chip,
                        getTheme()
                ));
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

    // QuizModel 리스트를 JSON 문자열로 직렬화해서 QuizActivity에 전달
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
