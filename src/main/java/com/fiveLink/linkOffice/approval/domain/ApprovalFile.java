package com.fiveLink.linkOffice.approval.domain;

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
@Table(name="fl_approval_file")
@NoArgsConstructor(access=AccessLevel.PROTECTED)
@AllArgsConstructor(access=AccessLevel.PROTECTED)
@Getter
@Setter
@Builder
public class ApprovalFile {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "approval_file_no")
	private Long approvalFileNo;
	
    @OneToOne
    @JoinColumn(name="approval_no", referencedColumnName = "approval_no")
    private Approval approval;
    
	@Column(name="approval_file_ori_name")
	private String approvalFileOriName;
	
	@Column(name="approval_file_new_name")
	private String approvalFileNewName;
	
	@Column(name="approval_file_size")
	private Long approvalFileSize;
	
	@Column(name="approval_file_upload_date")
	@CreationTimestamp
	private LocalDateTime approvalFileUploadDate;
    
    public ApprovalFileDto toDto() {
    	return ApprovalFileDto.builder()
    			.approval_file_no(approvalFileNo)
    			.approval_no(approval.getApprovalNo())
    			.approval_file_ori_name(approvalFileOriName)
    			.approval_file_new_name(approvalFileNewName)
    			.approval_file_size(approvalFileSize)
    			.approval_file_upload_date(approvalFileUploadDate)
    			.build();
    }
}
