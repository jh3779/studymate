package com.example.studymate.service;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class AiResponseParserTest {
    @Test
    public void parsesSummaryJsonWithLeadingText() throws Exception {
        String raw = "응답:\n{"
                + "\"summary\":[\"요약1\",\"요약2\",\"요약3\"],"
                + "\"keywords\":[\"키1\",\"키2\",\"키3\"]"
                + "}";

        AiResponseParser.SummaryData result =
                AiResponseParser.parseSummary(raw);

        assertEquals(List.of("요약1", "요약2", "요약3"), result.summary);
        assertEquals(List.of("키1", "키2", "키3"), result.keywords);
    }

    @Test
    public void rejectsSummaryWithWrongKeywordCount() {
        String raw = "{"
                + "\"summary\":[\"요약1\",\"요약2\",\"요약3\"],"
                + "\"keywords\":[\"키1\",\"키2\"]"
                + "}";

        assertThrows(
                Exception.class,
                () -> AiResponseParser.parseSummary(raw)
        );
    }

    @Test
    public void parsesExactlyThreeFourChoiceQuizzes() throws Exception {
        String quiz = "{"
                + "\"question\":\"질문\","
                + "\"options\":[\"1\",\"2\",\"3\",\"4\"],"
                + "\"answerIndex\":1,"
                + "\"explanation\":\"해설\""
                + "}";
        List<AiResponseParser.QuizData> result =
                AiResponseParser.parseQuizzes("[" + quiz + "," + quiz + "," + quiz + "]");

        assertEquals(3, result.size());
        assertEquals(4, result.get(0).options.size());
        assertEquals(1, result.get(0).answerIndex);
    }

    @Test
    public void rejectsQuizWithInvalidAnswerIndex() {
        String quiz = "{"
                + "\"question\":\"질문\","
                + "\"options\":[\"1\",\"2\",\"3\",\"4\"],"
                + "\"answerIndex\":4,"
                + "\"explanation\":\"해설\""
                + "}";

        assertThrows(
                Exception.class,
                () -> AiResponseParser.parseQuizzes(
                        "[" + quiz + "," + quiz + "," + quiz + "]"
                )
        );
    }

    @Test
    public void rejectsQuizWithFractionalAnswerIndex() {
        String quiz = "{"
                + "\"question\":\"질문\","
                + "\"options\":[\"1\",\"2\",\"3\",\"4\"],"
                + "\"answerIndex\":1.5,"
                + "\"explanation\":\"해설\""
                + "}";

        assertThrows(
                Exception.class,
                () -> AiResponseParser.parseQuizzes(
                        "[" + quiz + "," + quiz + "," + quiz + "]"
                )
        );
    }
}
