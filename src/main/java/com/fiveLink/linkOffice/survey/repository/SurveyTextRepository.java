package com.fiveLink.linkOffice.survey.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fiveLink.linkOffice.survey.domain.SurveyParticipant;
import com.fiveLink.linkOffice.survey.domain.SurveyQuestion;
import com.fiveLink.linkOffice.survey.domain.SurveyText;

@Repository
public interface SurveyTextRepository extends JpaRepository<SurveyText, Long> {

	 @Query("SELECT t FROM SurveyText t WHERE t.surveyQuestion.surveyQuestionNo = :surveyQuestionNo")
	    List<SurveyText> findByQuestionNo(@Param("surveyQuestionNo") Long surveyQuestionNo);

	 @Query("SELECT m.memberName, p.positionName, t.surveyTextAnswer " +
		       "FROM SurveyText t " +
		       "JOIN t.surveyParticipant sp " +
		       "JOIN sp.member m " +
		       "JOIN m.position p " +
		       "WHERE t.surveyQuestion.surveyQuestionNo = :surveyQuestionNo")
		List<Object[]> findTextAnswersWithParticipant(@Param("surveyQuestionNo") Long surveyQuestionNo);
		
	List<SurveyText> findAllBySurveyParticipantAndSurveyQuestion(SurveyParticipant surveyParticipant, SurveyQuestion surveyQuestion);
		
	
		
}
