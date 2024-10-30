package com.fiveLink.linkOffice.survey.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param; 
import org.springframework.stereotype.Repository;
import com.fiveLink.linkOffice.survey.domain.SurveyQuestion;
import java.util.List;

@Repository
public interface SurveyQuestionRepository extends JpaRepository<SurveyQuestion, Long> {

    @Query("SELECT q FROM SurveyQuestion q WHERE q.survey.surveyNo = :surveyNo")
    List<SurveyQuestion> findBySurveyNo(@Param("surveyNo") Long surveyNo); 
    
    
}