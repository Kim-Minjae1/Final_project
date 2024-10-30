package com.fiveLink.linkOffice.vacationapproval.domain;

import java.time.LocalDateTime;

import com.fiveLink.linkOffice.member.domain.Member;

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
public class VacationApprovalFlowDto {

	private Long vacation_approval_flow_no;
	private Long vacation_approval_no;
	private Long member_no;
	private Long vacation_approval_flow_role;
	private Long vacation_approval_flow_order;
	private Long vacation_approval_flow_status;
	private String vacation_approval_flow_reject_reason;
	private LocalDateTime vacation_approval_flow_complete_date;
	
	
	private String member_name;
	private String format_vacation_approval_flow_complete_date;
	private String digital_name;
	private String member_position;
	
	public VacationApprovalFlow toEntity(VacationApproval vacationApproval, Member member) {
		return VacationApprovalFlow.builder()
				.vacationApprovalFlowNo(vacation_approval_flow_no)
				.vacationApproval(vacationApproval)
				.member(member)
				.vacationApprovalFlowRole(vacation_approval_flow_role)
				.vacationApprovalFlowOrder(vacation_approval_flow_order)
				.vacationApprovalFlowStatus(vacation_approval_flow_status)
				.vacationApprovalFlowRejectReason(vacation_approval_flow_reject_reason)
				.vacationApprovalFlowCompleteDate(vacation_approval_flow_complete_date)
				.build();
	}
}
