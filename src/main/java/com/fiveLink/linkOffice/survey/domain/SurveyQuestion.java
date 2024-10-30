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


@Entity(name = "SurveyQuestion")
@Table(name="fl_survey_question")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Builder
public class SurveyQuestion {
	@Id
	@Column(name="survey_question_no")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long surveyQuestionNo;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "survey_no", nullable = false)
	private Survey survey;
	
	@Column(name="survey_question_text")
	private String surveyQuestionText;
	
	@Column(name="survey_question_type")
	private Integer surveyQuestionType;

	@Column(name="survey_question_essential")
	private Integer surveyQuestionEssential;
	
	// [김민재] 설문 선택형
	@OneToMany(mappedBy = "surveyQuestion", fetch = FetchType.LAZY)
	private List<SurveyOption> surveyOption;
	
	// [김민재] 설문 텍스트형
	@OneToMany(mappedBy = "surveyQuestion", fetch = FetchType.LAZY)
	private List<SurveyText> surveyText;
}
