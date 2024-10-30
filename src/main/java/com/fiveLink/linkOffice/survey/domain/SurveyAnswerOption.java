package com.fiveLink.linkOffice.survey.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "SurveyAnswerOption")
@Table(name="fl_survey_answer_option")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Builder
public class SurveyAnswerOption {
	@Id
	@Column(name="survey_answer_option_no")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long surveyAnswerOptionNo;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_participant_no")
    private SurveyParticipant surveyParticipant;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_option_no")
    private SurveyOption surveyOption;
}
