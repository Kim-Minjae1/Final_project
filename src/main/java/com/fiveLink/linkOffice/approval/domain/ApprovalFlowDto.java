package com.fiveLink.linkOffice.approval.domain;

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
public class ApprovalFlowDto {

		private Long approval_flow_no;
		private Long approval_no;
		private Long member_no;
		private Long approval_flow_role;
		private Long approval_flow_order;
		private Long approval_flow_status;
		private String approval_flow_reject_reason;
		private LocalDateTime approval_flow_complete_date;
		
		private String member_name;
		private String format_approval_flow_complete_date;
		private String digital_name;
		private String member_position;
		
		public ApprovalFlow toEntity(Approval approval, Member member) {
			return ApprovalFlow.builder()
					.approvalFlowNo(approval_no)
					.approval(approval)
					.member(member)
					.approvalFlowRole(approval_flow_role)
					.approvalFlowOrder(approval_flow_order)
					.approvalFlowStatus(approval_flow_status)
					.approvalFlowRejectReason(approval_flow_reject_reason)
					.approvalFlowCompleteDate(approval_flow_complete_date)
					.build();
		}
}
