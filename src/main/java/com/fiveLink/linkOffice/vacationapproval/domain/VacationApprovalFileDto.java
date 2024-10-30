package com.fiveLink.linkOffice.vacationapproval.domain;

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
public class VacationApprovalFileDto {
	
	private Long vacation_approval_file_no;
	private Long vacation_approval_no;
	private String vacation_approval_file_ori_name;
	private String vacation_approval_file_new_name;
	private Long vacation_approval_file_size;
	private LocalDateTime vacation_approval_file_upload_date;
	
	public VacationApprovalFile toEntity(VacationApproval vacationApproval) {
		return VacationApprovalFile.builder()
				.vacationApprovalFileNo(vacation_approval_file_no)
				.vacationApproval(vacationApproval)
				.vacationApprovalFileOriName(vacation_approval_file_ori_name)
				.vacationApprovalFileNewName(vacation_approval_file_new_name)
				.vacationApprovalFileSize(vacation_approval_file_size)
				.vacationApprovalFileUploadDate(vacation_approval_file_upload_date)
				.build();
	}
}
