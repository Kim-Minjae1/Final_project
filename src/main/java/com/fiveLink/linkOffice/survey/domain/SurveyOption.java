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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "SurveyOption")
@Table(name="fl_survey_option")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Builder
public class SurveyOption {
	@Id
	@Column(name="survey_option_no")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long surveyOptionNo;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_question_no")
    private SurveyQuestion surveyQuestion;
	
	@Column(name="survey_option_answer")
	private String surveyOptionAnswer;
	
	// [김민재] 설문 선택형 답변
	@OneToMany(mappedBy = "surveyOption", fetch = FetchType.LAZY)
	private List<SurveyAnswerOption> surveyAnswerOption;
}
