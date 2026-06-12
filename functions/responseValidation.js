function isNonEmptyString(value) {
  return typeof value === "string" && value.trim().length > 0;
}

function isValidSummaryResult(result) {
  return Boolean(
    result &&
    Array.isArray(result.summary) &&
    result.summary.length >= 3 &&
    result.summary.length <= 5 &&
    result.summary.every(isNonEmptyString) &&
    Array.isArray(result.keywords) &&
    result.keywords.length === 3 &&
    result.keywords.every(isNonEmptyString)
  );
}

function isValidQuiz(quiz) {
  return Boolean(
    quiz &&
    isNonEmptyString(quiz.question) &&
    Array.isArray(quiz.options) &&
    quiz.options.length === 4 &&
    quiz.options.every(isNonEmptyString) &&
    Number.isInteger(quiz.answerIndex) &&
    quiz.answerIndex >= 0 &&
    quiz.answerIndex <= 3 &&
    isNonEmptyString(quiz.explanation)
  );
}

module.exports = {
  isValidQuiz,
  isValidSummaryResult,
};
