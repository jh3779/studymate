package com.example.studymate.service;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public final class AiResponseParser {
    private AiResponseParser() {
    }

    public static SummaryData parseSummary(String raw) throws Exception {
        JSONObject object = new JSONObject(extractJson(raw));
        List<String> summary = toStringList(object.getJSONArray("summary"));
        List<String> keywords = toStringList(object.getJSONArray("keywords"));

        if (summary.size() < 3 || summary.size() > 5) {
            throw new Exception("summary 필드는 3~5개여야 함");
        }
        if (keywords.size() != 3) {
            throw new Exception("keywords 필드는 3개여야 함");
        }
        return new SummaryData(summary, keywords);
    }

    public static List<QuizData> parseQuizzes(String raw) throws Exception {
        JSONArray array = new JSONArray(extractJson(raw));
        List<QuizData> quizzes = new ArrayList<>();

        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.getJSONObject(i);
            String question = object.getString("question").trim();
            List<String> options = toStringList(object.getJSONArray("options"));
            int answerIndex = object.getInt("answerIndex");
            String explanation = object.getString("explanation").trim();

            if (question.isEmpty()
                    || options.size() != 4
                    || answerIndex < 0
                    || answerIndex > 3
                    || explanation.isEmpty()) {
                throw new Exception("퀴즈 항목이 올바르지 않음: index=" + i);
            }
            quizzes.add(new QuizData(question, options, answerIndex, explanation));
        }

        if (quizzes.size() != 3) {
            throw new Exception("유효한 퀴즈는 3개여야 함");
        }
        return quizzes;
    }

    static String extractJson(String text) {
        if (text == null) {
            return "";
        }
        int objectStart = text.indexOf('{');
        int arrayStart = text.indexOf('[');

        if (objectStart == -1 && arrayStart == -1) {
            return text;
        }
        if (objectStart == -1) {
            return text.substring(arrayStart);
        }
        if (arrayStart == -1) {
            return text.substring(objectStart);
        }
        return text.substring(Math.min(objectStart, arrayStart));
    }

    private static List<String> toStringList(JSONArray array) throws Exception {
        List<String> values = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            String value = array.getString(i).trim();
            if (value.isEmpty()) {
                throw new Exception("빈 문자열 항목은 허용되지 않음");
            }
            values.add(value);
        }
        return values;
    }

    public static final class SummaryData {
        public final List<String> summary;
        public final List<String> keywords;

        private SummaryData(List<String> summary, List<String> keywords) {
            this.summary = new ArrayList<>(summary);
            this.keywords = new ArrayList<>(keywords);
        }
    }

    public static final class QuizData {
        public final String question;
        public final List<String> options;
        public final int answerIndex;
        public final String explanation;

        private QuizData(
                String question,
                List<String> options,
                int answerIndex,
                String explanation
        ) {
            this.question = question;
            this.options = new ArrayList<>(options);
            this.answerIndex = answerIndex;
            this.explanation = explanation;
        }
    }
}
