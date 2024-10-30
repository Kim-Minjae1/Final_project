package com.fiveLink.linkOffice.survey.domain;

import java.util.List;

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

@Entity(name = "SurveyText")
@Table(name="fl_survey_text")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Builder
public class SurveyText {
	@Id
	@Column(name="survey_text_no")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long surveyTextNo;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_question_no")
    private SurveyQuestion surveyQuestion;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_participant_no")
    private SurveyParticipant surveyParticipant;
	
	@Column(name="survey_text_answer")
	private String surveyTextAnswer;
}
