package com.example.studymate.model;

import java.io.Serializable;
import java.util.List;

public class QuizModel implements Serializable {
    private String question;       // 문제 내용
    private List<String> options;  // 4지선다 보기 (크기 4)
    private int answerIndex;       // 정답 인덱스 (0~3)
    private String explanation;    // 오답노트용 해설
    private int userSelectedIndex = -1; // 사용자가 선택한 답 (초기값 -1)

    // 생성자
    public QuizModel(String question, List<String> options, int answerIndex, String explanation) {
        this.question = question;
        this.options = options;
        this.answerIndex = answerIndex;
        this.explanation = explanation;
    }

    // Getter & Setter들
    public String getQuestion() { return question; }
    public List<String> getOptions() { return options; }
    public int getAnswerIndex() { return answerIndex; }
    public String getExplanation() { return explanation; }
    public int getUserSelectedIndex() { return userSelectedIndex; }
    public void setUserSelectedIndex(int userSelectedIndex) { this.userSelectedIndex = userSelectedIndex; }

    // 정답 여부 확인용 편리한 메서드
    public boolean isCorrect() {
        return userSelectedIndex == answerIndex;
    }
}