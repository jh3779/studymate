const { onRequest } = require("firebase-functions/v2/https");
const { defineSecret } = require("firebase-functions/params");
const express = require("express");
const OpenAI = require("openai");
const admin = require("firebase-admin");
const { createVerifyAuth } = require("./authMiddleware");

if (!admin.apps.length) admin.initializeApp();

const openaiApiKey = defineSecret("OPENAI_API_KEY");

const MAX_TEXT_LENGTH = 5000;

// 지연 초기화: Secret이 바인딩된 후 첫 요청 시 단일 인스턴스 생성
let _openai;
function getOpenAI() {
  if (!_openai) _openai = new OpenAI({ apiKey: openaiApiKey.value() });
  return _openai;
}

const app = express();
app.use(express.json());

const verifyAuth = createVerifyAuth(admin);
app.use(verifyAuth);

// ─── 프롬프트 ────────────────────────────────────────────────────────────────

function buildSummaryPrompt(text) {
  return `너는 학습 내용을 요약해주는 AI 학습 도우미다.

아래 학습 내용을 바탕으로 다음 형식에 맞게 결과를 생성해라.

조건:
- 핵심 요약은 3~5줄로 작성
- 어려운 표현은 쉽게 풀어서 작성
- 중요한 키워드 3개를 추출
- 결과는 JSON 형식으로 반환

입력 내용:
"""
${text}
"""

출력 형식:
{
  "summary": ["요약 문장 1", "요약 문장 2", "요약 문장 3"],
  "keywords": ["키워드1", "키워드2", "키워드3"]
}`;
}

// OpenAI의 response_format: json_object 모드는 최상위 JSON 객체만 허용한다.
// 따라서 ai_prompt_spec.md의 bare array 형식 대신 { "quizzes": [...] }로 래핑한다.
// 서버에서 raw.quizzes를 추출한 뒤 배열 그대로 클라이언트에 반환하므로
// AiService.java의 new JSONArray(raw) 파싱은 정상 동작한다.
function buildQuizPrompt(text) {
  return `너는 시험 대비용 객관식 문제를 만들어주는 AI 학습 도우미다.

아래 학습 내용을 기반으로 객관식 문제 3개를 생성해라.

조건:
- 문제는 4지선다형
- 정답은 1개만 존재
- 각 문제마다 해설 포함
- 너무 쉬운 문제만 만들지 말 것
- 결과는 JSON 형식으로 반환

입력 내용:
"""
${text}
"""

출력 형식:
{
  "quizzes": [
    {
      "question": "문제 내용",
      "options": ["보기1", "보기2", "보기3", "보기4"],
      "answerIndex": 0,
      "explanation": "정답 해설"
    }
  ]
}`;
}

// ─── /summary ────────────────────────────────────────────────────────────────

app.post("/summary", async (req, res) => {
  const { text } = req.body;

  if (!text || typeof text !== "string" || text.trim().length === 0) {
    return res.status(400).json({ error: "text 필드가 필요합니다." });
  }
  if (text.length > MAX_TEXT_LENGTH) {
    return res.status(400).json({ error: `입력 내용은 ${MAX_TEXT_LENGTH}자를 초과할 수 없습니다.` });
  }

  try {
    const completion = await getOpenAI().chat.completions.create({
      model: "gpt-4o-mini",
      messages: [{ role: "user", content: buildSummaryPrompt(text) }],
      response_format: { type: "json_object" },
    });

    const result = JSON.parse(completion.choices[0].message.content);

    if (!Array.isArray(result.summary) || result.summary.length === 0) {
      return res.status(500).json({ error: "summary 응답이 올바르지 않습니다." });
    }
    if (!Array.isArray(result.keywords)) {
      return res.status(500).json({ error: "keywords 응답이 올바르지 않습니다." });
    }

    return res.status(200).json(result);
  } catch (err) {
    const httpStatus = err?.status >= 400 && err?.status < 600 ? err.status : 500;
    console.error(`summary 오류 [${httpStatus}]:`, err.message ?? err);
    return res.status(httpStatus).json({ error: "요약 생성에 실패했습니다." });
  }
});

// ─── /quiz ───────────────────────────────────────────────────────────────────

app.post("/quiz", async (req, res) => {
  const { text } = req.body;

  if (!text || typeof text !== "string" || text.trim().length === 0) {
    return res.status(400).json({ error: "text 필드가 필요합니다." });
  }
  if (text.length > MAX_TEXT_LENGTH) {
    return res.status(400).json({ error: `입력 내용은 ${MAX_TEXT_LENGTH}자를 초과할 수 없습니다.` });
  }

  try {
    const completion = await getOpenAI().chat.completions.create({
      model: "gpt-4o-mini",
      messages: [{ role: "user", content: buildQuizPrompt(text) }],
      response_format: { type: "json_object" },
    });

    const raw = JSON.parse(completion.choices[0].message.content);
    const quizzes = raw.quizzes;

    if (!Array.isArray(quizzes) || quizzes.length === 0) {
      return res.status(500).json({ error: "퀴즈 응답이 올바르지 않습니다." });
    }

    const valid = quizzes.filter(
      (q) =>
        q.question && q.question.length > 0 &&
        Array.isArray(q.options) && q.options.length === 4 &&
        typeof q.answerIndex === "number" && q.answerIndex >= 0 && q.answerIndex <= 3 &&
        q.explanation && q.explanation.length > 0
    );

    if (valid.length === 0) {
      return res.status(500).json({ error: "유효한 퀴즈가 없습니다." });
    }

    // AiService.java는 JSON 배열을 기대함
    return res.status(200).json(valid);
  } catch (err) {
    const httpStatus = err?.status >= 400 && err?.status < 600 ? err.status : 500;
    console.error(`quiz 오류 [${httpStatus}]:`, err.message ?? err);
    return res.status(httpStatus).json({ error: "퀴즈 생성에 실패했습니다." });
  }
});

// ─── Functions export ─────────────────────────────────────────────────────────

exports.api = onRequest(
  {
    secrets: [openaiApiKey],
    timeoutSeconds: 120,
  },
  app
);
