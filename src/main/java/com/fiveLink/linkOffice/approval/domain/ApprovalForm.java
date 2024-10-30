package com.fiveLink.linkOffice.approval.domain;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="fl_approval_form")
@NoArgsConstructor(access=AccessLevel.PROTECTED)
@AllArgsConstructor(access=AccessLevel.PROTECTED)
@Getter
@Setter
@Builder
public class ApprovalForm {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="approval_form_no")
	private Long approvalFormNo;
	
	@Column(name="approval_form_title")
	private String approvalFormTitle;
	
	@Column(name="approval_form_content")
	private String approvalFormContent;
	
	@Column(name="approval_form_create_date")
	@CreationTimestamp
	private LocalDateTime approvalFormCreateDate;
	
	@Column(name="approval_form_update_date")
	@UpdateTimestamp
	private LocalDateTime approvalFormUpdateDate;
	
	@Column(name="approval_form_status")
	private Long approvalFormStatus;
	
	public ApprovalFormDto toDto() {
		return ApprovalFormDto.builder()
				.approval_form_no(approvalFormNo)
				.approval_form_title(approvalFormTitle)
				.approval_form_content(approvalFormContent)
				.approval_form_create_date(approvalFormCreateDate)
				.approval_form_status(approvalFormStatus)
				.build();
	}
}
