package com.fiveLink.linkOffice.approval.domain;

import java.time.LocalDateTime;
import java.util.List;

import com.fiveLink.linkOffice.member.domain.Member;
import com.fiveLink.linkOffice.vacationapproval.domain.VacationApprovalFileDto;
import com.fiveLink.linkOffice.vacationapproval.domain.VacationApprovalFlowDto;

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
public class ApprovalDto {
	
	private Long approval_no;
	private Long member_no;
	private String approval_title;
	private String approval_content;
	private Long approval_status;
	private String approval_cancel_reason;
	private LocalDateTime approval_create_date;
	private LocalDateTime approval_update_date;
	
	private String member_name;
	
	private int search_type = 1;
	private String search_text;
	
	private String format_approval_create_date;
	private String digitalname;
	private String member_position;
	
	private Long approval_flow_role;
	private String approvalType;
	
	
	private List<ApprovalFileDto> files;
	private List<ApprovalFlowDto> flows;
	
	public Approval toEntity(Member member) {
			return Approval.builder()
					.approvalNo(approval_no)
					.member(member)
					.approvalTitle(approval_title)
					.approvalContent(approval_content)
					.approvalStatus(approval_status)
					.approvalCancelReason(approval_cancel_reason)
					.approvalCreateDate(approval_create_date)
					.approvalUpdateDate(approval_update_date)
					.build();
	}
}
