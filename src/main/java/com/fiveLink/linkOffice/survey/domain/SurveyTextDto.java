package com.fiveLink.linkOffice.survey.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class SurveyTextDto {
	private Long survey_text_no;
	private Long survey_question_no;
	private Long survey_participant_no;
	private String survey_text_answer;
	private String survey_position_name;
	 // 추가: 여러 질문과 답변을 처리할 수 있도록 배열 필드 추가
    private String[] survey_question_nos; 
    private String[] survey_text_answers;  
    
	public SurveyText toEntity() {
		return SurveyText.builder()
				.surveyTextNo(survey_text_no)
				.surveyTextAnswer(survey_text_answer)
		        .build();			
	}
	
	public SurveyTextDto toDto(SurveyText surveyText) {
		return SurveyTextDto.builder()
				.survey_text_no(surveyText.getSurveyTextNo())
				.survey_text_answer(surveyText.getSurveyTextAnswer())
				.build();
	}
}
