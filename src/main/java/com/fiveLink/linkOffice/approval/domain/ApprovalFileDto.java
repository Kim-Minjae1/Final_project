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
public class ApprovalFileDto {
	
	private Long approval_file_no;
	private Long approval_no;
	private String approval_file_ori_name;
	private String approval_file_new_name;
	private Long approval_file_size;
	private LocalDateTime approval_file_upload_date;

	public ApprovalFile toEntity(Approval approval) {
		return ApprovalFile.builder()
				.approvalFileNo(approval_file_no)
				.approval(approval)
				.approvalFileOriName(approval_file_ori_name)
				.approvalFileNewName(approval_file_new_name)
				.approvalFileSize(approval_file_size)
				.approvalFileUploadDate(approval_file_upload_date)
				.build();
	}

}