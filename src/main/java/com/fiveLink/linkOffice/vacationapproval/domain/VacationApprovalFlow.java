package com.fiveLink.linkOffice.vacationapproval.domain;

import java.time.LocalDateTime;

import org.hibernate.annotations.UpdateTimestamp;

import com.fiveLink.linkOffice.member.domain.Member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="fl_vacation_approval_flow")
@NoArgsConstructor(access=AccessLevel.PROTECTED)
@AllArgsConstructor(access=AccessLevel.PROTECTED)
@Getter
@Setter
@Builder
public class VacationApprovalFlow {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="vacation_approval_flow_no")
	private Long vacationApprovalFlowNo;
	
    @ManyToOne
    @JoinColumn(name = "vacation_approval_no", referencedColumnName = "vacation_approval_no")
    private VacationApproval vacationApproval;
    
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="member_no", referencedColumnName = "member_no")
	private Member member;
	
	@Column(name="vacation_approval_flow_role")
	private Long vacationApprovalFlowRole;
	
	@Column(name="vacation_approval_flow_order")
	private Long vacationApprovalFlowOrder;
	
	@Column(name="vacation_approval_flow_status")
	private Long vacationApprovalFlowStatus;
	
	@Column(name="vacation_approval_flow_reject_reason")
	private String vacationApprovalFlowRejectReason;
	
	@Column(name="vacation_approval_flow_complete_date")
	@UpdateTimestamp
	private LocalDateTime vacationApprovalFlowCompleteDate;
	
	public VacationApprovalFlowDto toDto() {
		return VacationApprovalFlowDto.builder()
				.vacation_approval_flow_no(vacationApprovalFlowNo)
				.vacation_approval_no(vacationApproval.getVacationApprovalNo())
				.member_no(member.getMemberNo())
				.member_name(member.getMemberName())
				.member_position(member.getPosition().getPositionName())
				.vacation_approval_flow_role(vacationApprovalFlowRole)
				.vacation_approval_flow_order(vacationApprovalFlowOrder)
				.vacation_approval_flow_status(vacationApprovalFlowStatus)
				.vacation_approval_flow_reject_reason(vacationApprovalFlowRejectReason)
				.vacation_approval_flow_complete_date(vacationApprovalFlowCompleteDate)
				.build();
	}
	
	
}
