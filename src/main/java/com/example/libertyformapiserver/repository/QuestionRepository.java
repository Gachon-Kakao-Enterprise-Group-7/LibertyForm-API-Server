package com.example.libertyformapiserver.repository;

import com.example.libertyformapiserver.domain.Question;
import lombok.extern.java.Log;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findQuestionsBySurveyId(long surveyId);
}
