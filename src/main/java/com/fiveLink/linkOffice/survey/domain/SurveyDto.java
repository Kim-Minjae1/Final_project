package com.fiveLink.linkOffice.survey.domain;

import java.time.LocalDateTime;
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
public class SurveyDto {
	
	private Long survey_no;
	private String survey_title;
	private String survey_description;
	private String survey_start_date;
	private String survey_end_date;
	private LocalDateTime survey_create_date;
	private Long member_no;
	private String member_name;
	private Integer survey_status;
	private Integer survey_participant_status;
	private List<Long> participantMemberNos;
	private List<SurveyQuestionDto> questions;
	private String position_name;
	
	private int search_type = 1;
	private String search_text;
	

    public Survey toEntity() {
        return Survey.builder()
                .surveyNo(survey_no)
                .surveyTitle(survey_title)
                .surveyDescription(survey_description)
                .surveyStartDate(survey_start_date)
                .surveyEndDate(survey_end_date)
                .surveyCreateDate(survey_create_date != null ? survey_create_date : LocalDateTime.now())
                .surveyStatus(survey_status != null ? survey_status : 0)
                .build();            
    }

    public SurveyDto toDto(Survey survey) {
        return SurveyDto.builder()
                .survey_no(survey.getSurveyNo())
                .survey_title(survey.getSurveyTitle())
                .survey_description(survey.getSurveyDescription())
                .survey_start_date(survey.getSurveyStartDate())
                .survey_end_date(survey.getSurveyEndDate())
                .survey_create_date(survey.getSurveyCreateDate())
                .survey_status(survey.getSurveyStatus())
                .build();
    }
	
}
