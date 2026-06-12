package com.example.studymate.validation;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class StudyInputValidatorTest {
    @Test
    public void rejectsBlankTitle() {
        StudyInputValidator.ValidationResult result =
                StudyInputValidator.validate("   ", repeat("학습", 20));

        assertFalse(result.isValid());
        assertEquals("제목을 입력해주세요.", result.getMessage());
    }

    @Test
    public void rejectsContentShorterThanThirtyCharacters() {
        StudyInputValidator.ValidationResult result =
                StudyInputValidator.validate("제목", repeat("가", 29));

        assertFalse(result.isValid());
        assertTrue(result.getMessage().contains("현재 29자"));
    }

    @Test
    public void acceptsBoundaryLengths() {
        assertTrue(StudyInputValidator.validate(
                "제목",
                repeat("가", StudyInputValidator.MIN_CONTENT_LENGTH)
        ).isValid());
        assertTrue(StudyInputValidator.validate(
                "제목",
                repeat("가", StudyInputValidator.MAX_CONTENT_LENGTH)
        ).isValid());
    }

    @Test
    public void rejectsContentLongerThanFiveThousandCharacters() {
        StudyInputValidator.ValidationResult result =
                StudyInputValidator.validate("제목", repeat("가", 5001));

        assertFalse(result.isValid());
        assertTrue(result.getMessage().contains("현재 5001자"));
    }

    private String repeat(String value, int count) {
        StringBuilder builder = new StringBuilder(value.length() * count);
        for (int i = 0; i < count; i++) {
            builder.append(value);
        }
        return builder.toString();
    }
}
