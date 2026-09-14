package org.example.model;

import java.util.List;

public record Question(
        String id,
        String question,
        List<String> options,
        String correctAnswer,
        String explanation,
        String grade
) {
}
