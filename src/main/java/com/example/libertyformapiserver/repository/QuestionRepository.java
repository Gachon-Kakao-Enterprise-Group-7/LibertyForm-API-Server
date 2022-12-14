package com.example.libertyformapiserver.repository;

import com.example.libertyformapiserver.config.status.BaseStatus;
import com.example.libertyformapiserver.domain.Question;
import com.example.libertyformapiserver.domain.Survey;
import lombok.extern.java.Log;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findQuestionsBySurveyIdAndStatus(long surveyId, BaseStatus status);
    Optional<Question> findQuestionBySurveyAndNumber(Survey survey, int number);
}
