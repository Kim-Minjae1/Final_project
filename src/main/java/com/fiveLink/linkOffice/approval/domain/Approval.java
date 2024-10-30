package com.fiveLink.linkOffice.approval.domain;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="fl_approval")
@NoArgsConstructor(access=AccessLevel.PROTECTED)
@AllArgsConstructor(access=AccessLevel.PROTECTED)
@Getter
@Setter
@Builder
public class Approval {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="approval_no")
	private Long approvalNo;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_no")
	private Member member;
	
	@Column(name="approval_title")
	private String approvalTitle;
	
	@Column(name="approval_content")
	private String approvalContent;
	
	@Column(name="approval_status")
	private Long approvalStatus;
	
	@Column(name="approval_cancel_reason")
	private String approvalCancelReason;
	
	@Column(name="approval_create_date")
	@CreationTimestamp
	private LocalDateTime approvalCreateDate;
	
	@Column(name="approval_update_date")
	@UpdateTimestamp
	private LocalDateTime approvalUpdateDate;
	
	@OneToOne(mappedBy = "approval")
	private ApprovalFile approvalFile;
	
	@OneToMany(mappedBy = "approval") 
	private List<ApprovalFlow> approvalFlow;
	
	public ApprovalDto toDto() {
		return ApprovalDto.builder()
				.approval_no(approvalNo)
				.member_no(member.getMemberNo())
				.member_name(member.getMemberName())
			    .member_position(member.getPosition().getPositionName())
				.approval_title(approvalTitle)
				.approval_content(approvalContent)
				.approval_status(approvalStatus)
				.approval_cancel_reason(approvalCancelReason)
				.approval_create_date(approvalCreateDate)
				.approval_update_date(approvalUpdateDate)
				.build();
	}
}
