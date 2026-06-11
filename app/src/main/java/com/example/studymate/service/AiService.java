package com.example.studymate.service;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.example.studymate.BuildConfig;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AiService {

    // local.properties의 ai.base.url 값을 사용 (git에 포함되지 않음)
    private static final String BASE_URL = normalizeBaseUrl(BuildConfig.AI_BASE_URL);

    private static final String TAG = "AiService";
    private static final MediaType JSON_TYPE = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)
            .build();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // ─── 콜백 인터페이스 ───────────────────────────────────────────

    public interface SummaryCallback {
        void onSuccess(SummaryResult result);
        void onFailure(String errorMessage);
    }

    public interface QuizCallback {
        void onSuccess(List<QuizItem> quizzes);
        void onFailure(String errorMessage);
    }

    // ─── 데이터 클래스 ────────────────────────────────────────────

    public static class SummaryResult {
        public final List<String> summary;
        public final List<String> keywords;

        public SummaryResult(List<String> summary, List<String> keywords) {
            this.summary = summary;
            this.keywords = keywords;
        }
    }

    public static class QuizItem {
        public final String question;
        public final List<String> options;
        public final int answerIndex;
        public final String explanation;

        public QuizItem(String question, List<String> options, int answerIndex, String explanation) {
            this.question = question;
            this.options = options;
            this.answerIndex = answerIndex;
            this.explanation = explanation;
        }
    }

    // ─── 요약 생성 ────────────────────────────────────────────────

    public void generateSummary(String text, SummaryCallback callback) {
        executor.execute(() -> {
            if (BASE_URL.isEmpty()) {
                failOnMain(callback, "AI 서버 주소가 설정되지 않았습니다. local.properties의 ai.base.url을 확인해주세요.");
                return;
            }
            if (text == null || text.trim().isEmpty()) {
                failOnMain(callback, "요약할 학습 내용이 없습니다.");
                return;
            }
            try {
                String token = fetchIdToken();
                if (token == null) {
                    failOnMain(callback, "로그인이 필요합니다. 다시 로그인해주세요.");
                    return;
                }

                JSONObject body = new JSONObject();
                body.put("text", text);

                Request request = new Request.Builder()
                        .url(BASE_URL + "/summary")
                        .addHeader("Authorization", "Bearer " + token)
                        .post(RequestBody.create(body.toString(), JSON_TYPE))
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        failOnMain(callback, extractErrorMessage(response, "서버 오류가 발생했습니다. 다시 시도해주세요."));
                        return;
                    }
                    if (!isJsonResponse(response)) {
                        failOnMain(callback, invalidServerResponseMessage());
                        return;
                    }
                    if (response.body() == null) {
                        failOnMain(callback, "서버 오류가 발생했습니다. 다시 시도해주세요.");
                        return;
                    }
                    String raw = response.body().string();
                    SummaryResult result = parseSummary(raw);
                    mainHandler.post(() -> callback.onSuccess(result));
                }
            } catch (Exception e) {
                Log.e(TAG, "generateSummary 실패", e);
                failOnMain(callback, "요약 생성에 실패했습니다. 다시 시도해주세요.");
            }
        });
    }

    // ─── 퀴즈 생성 ────────────────────────────────────────────────

    public void generateQuizzes(String text, QuizCallback callback) {
        executor.execute(() -> {
            if (BASE_URL.isEmpty()) {
                failOnMain(callback, "AI 서버 주소가 설정되지 않았습니다. local.properties의 ai.base.url을 확인해주세요.");
                return;
            }
            if (text == null || text.trim().isEmpty()) {
                failOnMain(callback, "퀴즈를 만들 학습 내용이 없습니다.");
                return;
            }
            try {
                String token = fetchIdToken();
                if (token == null) {
                    failOnMain(callback, "로그인이 필요합니다. 다시 로그인해주세요.");
                    return;
                }

                JSONObject body = new JSONObject();
                body.put("text", text);

                Request request = new Request.Builder()
                        .url(BASE_URL + "/quiz")
                        .addHeader("Authorization", "Bearer " + token)
                        .post(RequestBody.create(body.toString(), JSON_TYPE))
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        failOnMain(callback, extractErrorMessage(response, "서버 오류가 발생했습니다. 다시 시도해주세요."));
                        return;
                    }
                    if (!isJsonResponse(response)) {
                        failOnMain(callback, invalidServerResponseMessage());
                        return;
                    }
                    if (response.body() == null) {
                        failOnMain(callback, "서버 오류가 발생했습니다. 다시 시도해주세요.");
                        return;
                    }
                    String raw = response.body().string();
                    List<QuizItem> quizzes = parseQuizzes(raw);
                    mainHandler.post(() -> callback.onSuccess(quizzes));
                }
            } catch (Exception e) {
                Log.e(TAG, "generateQuizzes 실패", e);
                failOnMain(callback, "퀴즈 생성에 실패했습니다. 다시 시도해주세요.");
            }
        });
    }

    // ─── JSON 파싱 ────────────────────────────────────────────────

    private SummaryResult parseSummary(String raw) throws Exception {
        String json = extractJson(raw);
        JSONObject obj = new JSONObject(json);

        List<String> summary = toStringList(obj.getJSONArray("summary"));
        List<String> keywords = toStringList(obj.getJSONArray("keywords"));

        if (summary.size() < 3 || summary.size() > 5) {
            throw new Exception("summary 필드는 3~5개여야 함");
        }
        if (keywords.size() != 3) {
            throw new Exception("keywords 필드는 3개여야 함");
        }
        return new SummaryResult(summary, keywords);
    }

    private List<QuizItem> parseQuizzes(String raw) throws Exception {
        String json = extractJson(raw);
        JSONArray arr = new JSONArray(json);
        List<QuizItem> quizzes = new ArrayList<>();

        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.getJSONObject(i);
            String question = obj.getString("question");
            List<String> options = toStringList(obj.getJSONArray("options"));
            int answerIndex = obj.getInt("answerIndex");
            String explanation = obj.getString("explanation");

            if (question.isEmpty() || options.size() != 4
                    || answerIndex < 0 || answerIndex > 3
                    || explanation.isEmpty()) {
                Log.w(TAG, "퀴즈 항목 검증 실패, 건너뜀: index=" + i);
                continue;
            }
            quizzes.add(new QuizItem(question, options, answerIndex, explanation));
        }

        if (quizzes.size() != 3) throw new Exception("유효한 퀴즈는 3개여야 함");
        return quizzes;
    }

    // AI 응답에 설명 문장이 섞일 경우 JSON 블록만 추출
    private String extractJson(String text) {
        int objStart = text.indexOf('{');
        int arrStart = text.indexOf('[');

        if (objStart == -1 && arrStart == -1) return text;
        if (objStart == -1) return text.substring(arrStart);
        if (arrStart == -1) return text.substring(objStart);
        return text.substring(Math.min(objStart, arrStart));
    }

    private List<String> toStringList(JSONArray arr) throws Exception {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) {
            list.add(arr.getString(i));
        }
        return list;
    }

    // ─── 헬퍼 ────────────────────────────────────────────────────

    private static String normalizeBaseUrl(String url) {
        if (url == null) return "";
        String normalized = url.trim();
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    // 백그라운드 스레드에서 호출. 로그인 안 됐거나 토큰 취득 실패·타임아웃 시 null 반환.
    private String fetchIdToken() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return null;
        try {
            String token = Tasks.await(user.getIdToken(false), 10, TimeUnit.SECONDS).getToken();
            return (token != null && !token.isEmpty()) ? token : null;
        } catch (Exception e) {
            Log.e(TAG, "ID 토큰 취득 실패", e);
            return null;
        }
    }

    private String extractErrorMessage(Response response, String fallback) {
        try {
            if (response.body() == null) return fallback;
            JSONObject json = new JSONObject(response.body().string());
            String msg = json.optString("error", "");
            return msg.isEmpty() ? fallback : msg;
        } catch (Exception e) {
            Log.w(TAG, "서버 오류 body 파싱 실패", e);
            return fallback;
        }
    }

    private boolean isJsonResponse(Response response) {
        String contentType = response.header("Content-Type", "");
        return contentType.toLowerCase(java.util.Locale.ROOT).contains("application/json");
    }

    private String invalidServerResponseMessage() {
        return "AI 서버 응답이 올바르지 않습니다. local.properties의 ai.base.url과 Functions 배포 상태를 확인해주세요.";
    }

    private void failOnMain(SummaryCallback callback, String message) {
        mainHandler.post(() -> callback.onFailure(message));
    }

    private void failOnMain(QuizCallback callback, String message) {
        mainHandler.post(() -> callback.onFailure(message));
    }
}
