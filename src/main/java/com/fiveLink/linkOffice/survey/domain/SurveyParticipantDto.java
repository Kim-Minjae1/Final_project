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
public class SurveyParticipantDto {
	private Long survey_participant_no;
	private Long survey_no;
	private Long member_no;
	private String member_name;
	private Integer survey_participant_status;
	private Long survey_option_no;  // 객관식 답변 번호
    private String survey_text_answer;  // 주관식 답변
	
	
	private int search_type = 1;
	private String search_text;
	
	public SurveyParticipant toEntity() {
		return SurveyParticipant.builder()
		        .surveyParticipantNo(survey_participant_no)
		        .surveyParticipantStatus(survey_participant_status)
		        .build();			
	}
	
	public SurveyParticipantDto toDto(SurveyParticipant surveyParticipant) {
		return SurveyParticipantDto.builder()
				.survey_participant_no(surveyParticipant.getSurveyParticipantNo())
				.survey_participant_status(surveyParticipant.getSurveyParticipantStatus())
				.build();
	}
}
