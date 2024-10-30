package com.fiveLink.linkOffice.approval.domain;

import java.time.LocalDateTime;

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
public class ApprovalFormDto {

	private Long approval_form_no;
	private String approval_form_title;
	private String approval_form_content;
	private LocalDateTime approval_form_create_date;
	private Long approval_form_status;
	
	private String format_create_date;
	private String search_text;
	
	
	public ApprovalForm toEntity() {
		return ApprovalForm.builder()
				.approvalFormNo(approval_form_no)
				.approvalFormTitle(approval_form_title)
				.approvalFormContent(approval_form_content)
				.approvalFormCreateDate(approval_form_create_date)
				.approvalFormStatus(approval_form_status)
				.build();
	}
	
}
