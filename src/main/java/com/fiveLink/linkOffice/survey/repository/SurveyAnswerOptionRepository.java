package com.fiveLink.linkOffice.survey.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fiveLink.linkOffice.survey.domain.SurveyAnswerOption;
import com.fiveLink.linkOffice.survey.domain.SurveyParticipant;
import com.fiveLink.linkOffice.survey.domain.SurveyQuestion;

@Repository
public interface SurveyAnswerOptionRepository extends JpaRepository<SurveyAnswerOption, Long> {
	List<SurveyAnswerOption> findAllBySurveyParticipantAndSurveyOption_SurveyQuestion(SurveyParticipant surveyParticipant, SurveyQuestion surveyQuestion);

	 // 여러 옵션 번호를 기준으로 답변 조회
    List<SurveyAnswerOption> findAllBySurveyOption_SurveyOptionNoIn(List<Long> surveyOptionNos);
}