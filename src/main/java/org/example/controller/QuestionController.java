package org.example.controller;

import org.example.model.Question;
import org.example.model.SubjectInfo;
import org.example.service.QuestionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping("/subjects")
    public List<SubjectInfo> getSubjects() {
        return questionService.getSubjects();
    }

    @GetMapping("/questions/{subject}")
    public List<Question> getQuestions(@PathVariable String subject, @RequestParam(required = false) String grade) {
        return questionService.getQuestions(subject, grade);
    }
}
