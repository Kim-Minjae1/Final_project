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
public class SurveyOptionDto {
	
	private Long survey_option_no;
	private Long survey_question_no;
	private String survey_question_text;
	private String survey_option_answer;
	
	public SurveyOption toEntity() {
		return SurveyOption.builder()
				.surveyOptionNo(survey_option_no)
		        .surveyOptionAnswer(survey_option_answer)
		        .build();			
	}
	
	public SurveyOptionDto toDto(SurveyOption surveyOption) {
		return SurveyOptionDto.builder()
				.survey_option_no(surveyOption.getSurveyOptionNo())
				.survey_question_text(surveyOption.getSurveyOptionAnswer())
				.build();
	}
}
