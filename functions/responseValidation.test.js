const assert = require("node:assert/strict");
const test = require("node:test");

const {
  isValidQuiz,
  isValidSummaryResult,
} = require("./responseValidation");

test("accepts a valid summary response", () => {
  assert.equal(isValidSummaryResult({
    summary: ["요약 1", "요약 2", "요약 3"],
    keywords: ["키워드 1", "키워드 2", "키워드 3"],
  }), true);
});

test("rejects blank summary items", () => {
  assert.equal(isValidSummaryResult({
    summary: ["요약 1", " ", "요약 3"],
    keywords: ["키워드 1", "키워드 2", "키워드 3"],
  }), false);
});

test("accepts a valid four-choice quiz", () => {
  assert.equal(isValidQuiz({
    question: "질문",
    options: ["A", "B", "C", "D"],
    answerIndex: 1,
    explanation: "해설",
  }), true);
});

test("rejects a fractional answer index", () => {
  assert.equal(isValidQuiz({
    question: "질문",
    options: ["A", "B", "C", "D"],
    answerIndex: 1.5,
    explanation: "해설",
  }), false);
});
