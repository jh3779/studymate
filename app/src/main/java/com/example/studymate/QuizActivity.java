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
import java.util.Date;
/**
 * [이슈 #14 해결] AI 생성 퀴즈 데이터 동적 바인딩 액티비티
 */
public class QuizActivity extends BaseActivity {

    // AI가 생성하여 전달해 준 실제 퀴즈 모델 객체 리스트 (하드코딩 제거)
    private ArrayList<QuizModel> quizList = new ArrayList<>();

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

                // 순정 모델의 기본 생성자 및 Setter 주입 파이프라인 싱크 완료
                QuizModel quiz = new QuizModel();
                quiz.setQuestion(obj.optString("question", "질문 로딩 실패"));

                JSONArray optsArray = obj.optJSONArray("options");
                List<String> optionsList = new ArrayList<>();
                if (optsArray != null) {
                    for (int j = 0; j < optsArray.length(); j++) {
                        optionsList.add(optsArray.getString(j));
                    }
                }
                quiz.setOptions(optionsList);
                quiz.setAnswerIndex(obj.optInt("answerIndex", 0));
                quiz.setExplanation(obj.optString("explanation", "해설이 제공되지 않는 문제입니다."));
                quiz.setCreatedAt(new Date()); // 현재 시간 주입
                quiz.setNoteId(getIntent().getStringExtra("noteId")); // 노트 ID 바인딩 보완

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
            }
        }
    }

    private void selectOption(int index) {
        selectedIndex = index;
        nextButton.setEnabled(true);

        for (int i = 0; i < optionViews.length; i++) {
            optionViews[i].setBackgroundResource(i == index ? R.drawable.bg_option_selected : R.drawable.bg_option);
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
            intent.putIntegerArrayListExtra("userAnswers", userAnswers); // 단일 정식 통로 규격 사용
            intent.putExtra("quizListSerializable", quizList);
            startActivity(intent);
            finish();
            return;
        }

        currentIndex++;
        renderQuestion();
    }
}