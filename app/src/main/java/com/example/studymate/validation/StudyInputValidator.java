package com.example.studymate.validation;

public final class StudyInputValidator {
    public static final int MIN_CONTENT_LENGTH = 30;
    public static final int MAX_CONTENT_LENGTH = 5000;

    private StudyInputValidator() {
    }

    public static ValidationResult validate(String title, String content) {
        String normalizedTitle = title == null ? "" : title.trim();
        String normalizedContent = content == null ? "" : content.trim();

        if (normalizedTitle.isEmpty()) {
            return ValidationResult.invalid("제목을 입력해주세요.");
        }
        if (normalizedContent.length() < MIN_CONTENT_LENGTH) {
            return ValidationResult.invalid(
                    "학습 내용은 30자 이상 입력해주세요. 현재 "
                            + normalizedContent.length()
                            + "자입니다."
            );
        }
        if (normalizedContent.length() > MAX_CONTENT_LENGTH) {
            return ValidationResult.invalid(
                    "학습 내용은 5000자 이하로 입력해주세요. 현재 "
                            + normalizedContent.length()
                            + "자입니다."
            );
        }
        return ValidationResult.valid();
    }

    public static final class ValidationResult {
        private final boolean valid;
        private final String message;

        private ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public static ValidationResult valid() {
            return new ValidationResult(true, "");
        }

        public static ValidationResult invalid(String message) {
            return new ValidationResult(false, message);
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }
    }
}
