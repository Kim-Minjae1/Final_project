package com.fiveLink.linkOffice.vacationapproval.domain;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fiveLink.linkOffice.member.domain.Member;
import com.fiveLink.linkOffice.vacation.domain.VacationType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "fl_vacation_approval")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Builder
public class VacationApproval {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "vacation_approval_no")
	private Long vacationApprovalNo;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_no")
	private Member member;

	@Column(name = "vacation_approval_title")
	private String vacationApprovalTitle;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "vacation_type_no")
	private VacationType vacationType;

	@Column(name = "vacation_approval_start_date")
	private String vacationApprovalStartDate;

	@Column(name = "vacation_approval_end_date")
	private String vacationApprovalEndDate;

	@Column(name = "vacation_approval_total_days")
	private String vacationApprovalTotalDays;

	@Column(name = "vacation_approval_content")
	private String vacationApprovalContent;

	@Column(name = "vacation_approval_status")
	private Long vacationApprovalStatus;

	@Column(name = "vacation_approval_cancel_reason")
	private String vacationApprovalCancelReason;

	@Column(name = "vacation_approval_create_date")
	@CreationTimestamp
	private LocalDateTime vacationApprovalCreateDate;

	@Column(name = "vacation_approval_update_date")
	@UpdateTimestamp
	private LocalDateTime vacationApprovalUpdateDate;

	@OneToOne(mappedBy = "vacationApproval")
	private VacationApprovalFile vacationApprovalFile;

	@OneToMany(mappedBy = "vacationApproval") 
	private List<VacationApprovalFlow> vacationApprovalFlows;
	 
	public VacationApprovalDto toDto() {
		return VacationApprovalDto.builder()
				.vacation_approval_no(vacationApprovalNo)
				.member_no(member.getMemberNo())
				.member_name(member.getMemberName())
				.member_position(member.getPosition().getPositionName())
				.vacation_approval_title(vacationApprovalTitle)
				.vacation_type_no(vacationType.getVacationTypeNo())
				.vacation_type_name(vacationType.getVacationTypeName())
				.vacation_approval_start_date(vacationApprovalStartDate)
				.vacation_approval_end_date(vacationApprovalEndDate)
				.vacation_approval_total_days(vacationApprovalTotalDays)
				.vacation_approval_content(vacationApprovalContent).vacation_approval_status(vacationApprovalStatus)
				.vacation_approval_cancel_reason(vacationApprovalCancelReason)
				.vacation_approval_create_date(vacationApprovalCreateDate)
				.vacation_approval_update_date(vacationApprovalUpdateDate)
				.build();
	}
}
