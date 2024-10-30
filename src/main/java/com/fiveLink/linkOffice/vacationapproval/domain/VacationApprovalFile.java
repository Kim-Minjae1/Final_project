package com.fiveLink.linkOffice.vacationapproval.domain;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="fl_vacation_approval_file")
@NoArgsConstructor(access=AccessLevel.PROTECTED)
@AllArgsConstructor(access=AccessLevel.PROTECTED)
@Getter
@Setter
@Builder
public class VacationApprovalFile {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="vacation_approval_file_no")
	private Long vacationApprovalFileNo;
	
    @OneToOne
    @JoinColumn(name="vacation_approval_no", referencedColumnName = "vacation_approval_no")
    private VacationApproval vacationApproval;
	
	@Column(name="vacation_approval_file_ori_name")
	private String vacationApprovalFileOriName;
	
	@Column(name="vacation_approval_file_new_name")
	private String vacationApprovalFileNewName;
	
	@Column(name="vacation_approval_file_size")
	private Long vacationApprovalFileSize;
	
	@Column(name="vacation_approval_file_upload_date")
	@CreationTimestamp
	private LocalDateTime vacationApprovalFileUploadDate;

	  public VacationApprovalFileDto toDto() {
	        return VacationApprovalFileDto.builder()
	                .vacation_approval_file_no(vacationApprovalFileNo)
	                .vacation_approval_no(vacationApproval.getVacationApprovalNo())
	                .vacation_approval_file_ori_name(vacationApprovalFileOriName)
	                .vacation_approval_file_new_name(vacationApprovalFileNewName)
	                .vacation_approval_file_size(vacationApprovalFileSize)
	                .vacation_approval_file_upload_date(vacationApprovalFileUploadDate)
	                .build();
	    }
}
