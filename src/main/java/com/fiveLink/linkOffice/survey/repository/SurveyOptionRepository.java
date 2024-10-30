package com.fiveLink.linkOffice.survey.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.fiveLink.linkOffice.survey.domain.SurveyOption;
import java.util.List;

@Repository
public interface SurveyOptionRepository extends JpaRepository<SurveyOption, Long> {

    @Query("SELECT o FROM SurveyOption o WHERE o.surveyQuestion.surveyQuestionNo = :surveyQuestionNo")
    List<SurveyOption> findByQuestionNo(@Param("surveyQuestionNo") Long surveyQuestionNo);
    
    @Query("SELECT so.surveyQuestion.surveyQuestionNo, so.surveyOptionAnswer, COUNT(sao.surveyAnswerOptionNo) AS answerCount " +
    	       "FROM SurveyOption so " +
    	       "LEFT JOIN SurveyAnswerOption sao ON so.surveyOptionNo = sao.surveyOption.surveyOptionNo " +
    	       "WHERE so.surveyQuestion.surveyQuestionNo IN (" +
    	       "    SELECT sq.surveyQuestionNo " +
    	       "    FROM SurveyQuestion sq " +
    	       "    WHERE sq.survey.surveyNo = :surveyNo) " +
    	       "GROUP BY so.surveyQuestion.surveyQuestionNo, so.surveyOptionAnswer")
    List<Object[]> countAnswersByOptionWithAnswer(@Param("surveyNo") Long surveyNo);
}