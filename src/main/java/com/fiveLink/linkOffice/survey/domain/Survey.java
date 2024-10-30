package com.fiveLink.linkOffice.survey.domain;


import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import com.fiveLink.linkOffice.member.domain.Member;

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

@Entity(name = "Survey")
@Table(name="fl_survey")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Builder
public class Survey {
	@Id
	@Column(name="survey_no")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long surveyNo;
	
	@Column(name="survey_title")
	private String surveyTitle;
	
	@Column(name="survey_description")
	private String surveyDescription;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_no")
    private Member member;
	
	@Column(name="survey_start_date")
	private String surveyStartDate;
	
	@Column(name="survey_end_date")
	private String surveyEndDate;
	
	@Column(name="survey_create_date")
	@CreationTimestamp
	private LocalDateTime surveyCreateDate;
	
	@Column(name="survey_status")
	private Integer surveyStatus;
	
	// [김민재] 설문 참여자 확인
	@OneToMany(mappedBy = "survey", fetch = FetchType.LAZY)
	private List<SurveyParticipant> surveyParticipant;
	
	// [김민재] 설문 질문
	@OneToMany(mappedBy = "survey", fetch = FetchType.LAZY)
	private List<SurveyQuestion> surveyQuestion;
}
