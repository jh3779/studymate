package com.example.studymate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import com.example.studymate.model.QuizModel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

/**
 * [이슈 #14 해결] AI 생성 퀴즈 데이터 동적 바인딩 액티비티
 */
public class QuizActivity extends BaseActivity {

    // AI가 생성하여 전달해 준 실제 퀴즈 모델 객체 리스트 (하드코딩 제거)
    private List<QuizModel> quizList = new ArrayList<>();

    private TextView progressText;
    private TextView questionText;
    private TextView[] optionViews;
    private Button nextButton;

    private int currentIndex = 0;
    private int selectedIndex = -1;
    private int correctCount = 0;
    private ArrayList<Integer> userAnswers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        progressText = findViewById(R.id.quizProgressText);
        questionText = findViewById(R.id.questionText);
        optionViews = new TextView[]{
                findViewById(R.id.optionOne),
                findViewById(R.id.optionTwo),
                findViewById(R.id.optionThree),
                findViewById(R.id.optionFour)
        };
        nextButton = findViewById(R.id.nextQuizButton);

        bindClick(R.id.backSummary, v -> finish());

        for (int i = 0; i < optionViews.length; i++) {
            final int index = i;
            optionViews[i].setOnClickListener(v -> selectOption(index));
        }
        nextButton.setOnClickListener(v -> moveNext());

        // 요청 사항: Intent extra로부터 AI가 생성한 JSON 문자열 수신
        String quizzesJson = getIntent().getStringExtra("quizzesJson");
        parseQuizzesJson(quizzesJson);

        // 데이터가 정상 로드되었다면 첫 번째 문제 렌더링
        if (!quizList.isEmpty()) {
            renderQuestion();
        } else {
            showShortToast("퀴즈 데이터를 불러오지 못했습니다. 이전 화면으로 돌아갑니다.");
            finish();
        }
    }

    /**
     * AI 퀴즈 데이터 파싱 알고리즘 (JSONArray -> List<QuizModel>)
     */
    private void parseQuizzesJson(String jsonStr) {
        if (jsonStr == null || jsonStr.isEmpty()) return;
        try {
            JSONArray jsonArray = new JSONArray(jsonStr);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);

                String question = obj.optString("question", "질문 로딩 실패");
                JSONArray optsArray = obj.optJSONArray("options");
                List<String> optionsList = new ArrayList<>();
                if (optsArray != null) {
                    for (int j = 0; j < optsArray.length(); j++) {
                        optionsList.add(optsArray.getString(j));
                    }
                }
                int answerIndex = obj.optInt("answerIndex", 0);
                String explanation = obj.optString("explanation", "해설이 제공되지 않는 문제입니다.");

                // 추출된 데이터를 토대로 공용 QuizModel 인스턴스 생성 및 바인딩
                QuizModel quiz = new QuizModel();
                quiz.setQuestion(question);
                quiz.setOptions(optionsList);
                quiz.setAnswerIndex(answerIndex);
                quiz.setExplanation(explanation);

                quizList.add(quiz);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void renderQuestion() {
        selectedIndex = -1;
        nextButton.setEnabled(false);

        QuizModel currentQuiz = quizList.get(currentIndex);

        nextButton.setText(currentIndex == quizList.size() - 1 ? "결과 보기" : "다음 문제");
        progressText.setText((currentIndex + 1) + "/" + quizList.size());
        questionText.setText(currentQuiz.getQuestion());

        for (int i = 0; i < optionViews.length; i++) {
            if (i < currentQuiz.getOptions().size()) {
                optionViews[i].setText(currentQuiz.getOptions().get(i));
                optionViews[i].setBackgroundResource(R.drawable.bg_option);
                optionViews[i].setContentDescription(currentQuiz.getOptions().get(i));
            }
        }
    }

    private void selectOption(int index) {
        selectedIndex = index;
        nextButton.setEnabled(true);
        QuizModel currentQuiz = quizList.get(currentIndex);
        for (int i = 0; i < optionViews.length; i++) {
            optionViews[i].setBackgroundResource(i == index ? R.drawable.bg_option_selected : R.drawable.bg_option);
            optionViews[i].setContentDescription(currentQuiz.getOptions().get(i) + (i == index ? ", 선택됨" : ""));
        }
    }

    private void moveNext() {
        userAnswers.add(selectedIndex);

        QuizModel currentQuiz = quizList.get(currentIndex);

        // 정답 검증 로직 반영
        if (selectedIndex == currentQuiz.getAnswerIndex()) {
            correctCount++;
        }

        if (currentIndex == quizList.size() - 1) {
            Intent intent = new Intent(this, QuizResultActivity.class);
            intent.putExtra("correctCount", correctCount);
            intent.putExtra("totalCount", quizList.size());
            intent.putExtra("userAnswers", userAnswers);

            // ★ 중요: 결과창에서 오답노트를 Firestore에 업로드할 수 있도록 원본 AI 퀴즈 데이터 세트 재패스
            intent.putExtra("quizListSerializable", (ArrayList<QuizModel>) quizList);

            startActivity(intent);
            finish();
            return;
        }

        currentIndex++;
        renderQuestion();
    }
}