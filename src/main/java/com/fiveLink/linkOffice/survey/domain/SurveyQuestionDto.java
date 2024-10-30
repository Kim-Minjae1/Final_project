package com.fiveLink.linkOffice.survey.domain;

import java.util.List;

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
public class SurveyQuestionDto {
	
	private Long survey_question_no;
	private Long survey_no;
	private String survey_question_text;
	private Integer survey_question_type;
	private Integer survey_question_essential;
	private List<Long> survey_option_no;
	private List<String> survey_option_answer;
    private List<Long> survey_text_no;
    private List<String> survey_text_answer;
    private List<String> options;
    private List<Long> selected_option_no;
    
	
	public SurveyQuestion toEntity() {
		return SurveyQuestion.builder()
				.surveyQuestionNo(survey_question_no)
		        .surveyQuestionText(survey_question_text)
		        .surveyQuestionType(survey_question_type)
		        .surveyQuestionEssential(survey_question_essential)
		        .build();			
	}
	
	public SurveyQuestionDto toDto(SurveyQuestion surveyQuestion) {
		return SurveyQuestionDto.builder()
				.survey_question_no(surveyQuestion.getSurveyQuestionNo())
				.survey_question_text(surveyQuestion.getSurveyQuestionText())
				.survey_question_type(surveyQuestion.getSurveyQuestionType())
				.survey_question_essential(surveyQuestion.getSurveyQuestionEssential())
				.build();
	}
}
