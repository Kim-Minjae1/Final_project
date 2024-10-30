package com.fiveLink.linkOffice.member.domain;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fiveLink.linkOffice.approval.domain.Approval;
import com.fiveLink.linkOffice.approval.domain.ApprovalFlow;
import com.fiveLink.linkOffice.inventory.domain.Inventory;
import com.fiveLink.linkOffice.notice.domain.Notice;
import com.fiveLink.linkOffice.organization.domain.Department;
import com.fiveLink.linkOffice.organization.domain.Position;
import com.fiveLink.linkOffice.survey.domain.Survey;
import com.fiveLink.linkOffice.survey.domain.SurveyParticipant;
import com.fiveLink.linkOffice.vacationapproval.domain.VacationApproval;
import com.fiveLink.linkOffice.vacationapproval.domain.VacationApprovalFlow;

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

@Entity
@Table(name="fl_member")
@NoArgsConstructor(access=AccessLevel.PROTECTED)
@AllArgsConstructor(access=AccessLevel.PROTECTED)
@Getter
@Setter
@Builder
public class Member {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="member_no")
	private Long memberNo;
	
	@Column(name="member_number")
	private String memberNumber;

	@Column(name="member_pw")
	private String memberPw;
	
	@Column(name="member_name")
	private String memberName;
	
	@Column(name="member_national")
	private String memberNational;
	
	@Column(name="member_internal")
	private String memberInternal;
	
	@Column(name="member_mobile")
	private String memberMobile;
	
	@Column(name="department_no")
	private Long departmentNo;
	
	@Column(name="position_no")
	private Long positionNo;

    @ManyToOne(fetch = FetchType.LAZY )
    @JoinColumn(name = "department_no", insertable = false, updatable =false)
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_no", insertable = false, updatable =false)
    private Position position;
	
	@Column(name="member_address")
	private String memberAddress;
	
	@Column(name="member_hire_date")
	private String memberHireDate;
	
	@Column(name="member_end_date")
	private LocalDateTime memberEndDate;
	
	@Column(name="member_create_date")
	@CreationTimestamp
	private LocalDateTime memberCreateDate;
	
	@Column(name="member_update_date")
	@UpdateTimestamp
	private LocalDateTime memberUpdateDate;
	
	@Column(name="member_ori_profile_img")
	private String memberOriProfileImg;
	
	@Column(name="member_new_profile_img")
	private String memberNewProfileImg;
	
	@Column(name="member_ori_digital_img")
	private String memberOriDigitalImg;
	
	@Column(name="member_new_digital_img")
	private String memberNewDigitalImg;
	
	//insertable = false -> insert 할 때 제외
	@Column(name="member_status", insertable = false, updatable = true)
	private Long memberStatus;

	@Column(name="member_additional", insertable = false, updatable = true)
	private Long memberAdditional;
	
	// [김민재] 비품관리 관리자 확인
	@OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
	private List<Inventory> inventory;

	// [김채영] 1년 미만 멤버 확인
	@Column(name="member_one_under")
	private int memberOneUnder;

	//[김채영] 휴가 지급 개수
	@Column(name="member_vacation_count")
	private double memberVacationCount;

	//[김채영] 휴가 지급 날짜
	@Column(name="member_vacation_date")
	private String memberVacationDate;
	
	// [전주영] 휴가 결재
	@OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
	private List<VacationApproval> vacationApprovals;

	@OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
	private List<VacationApprovalFlow> vacationApprovalFlows;
	
	// [전주영] 전자 결재
	@OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
	private List<Approval> approval;
	
	@OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
	private List<ApprovalFlow> approvalFlow;
	
	// [김민재] 공지사항 관리자 확인
	@OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
	private List<Notice> notice;
	
	// [김민재] 설문 작성자 확인
	@OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
	private List<Survey> survey;
	
	// [김민재] 설문 참여자 확인
	@OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
	private List<SurveyParticipant> surveyParticipant;
}