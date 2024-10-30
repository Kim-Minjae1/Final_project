package com.fiveLink.linkOffice.approval.domain;

import java.time.LocalDateTime;

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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="fl_approval_flow")
@NoArgsConstructor(access=AccessLevel.PROTECTED)
@AllArgsConstructor(access=AccessLevel.PROTECTED)
@Getter
@Setter
@Builder
public class ApprovalFlow {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "approval_flow_no")
	private Long approvalFlowNo;
	
    @OneToOne
    @JoinColumn(name="approval_no", referencedColumnName = "approval_no")
    private Approval approval;
    
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="member_no", referencedColumnName = "member_no")
	private Member member;
	
	@Column(name="approval_flow_role")
	private Long approvalFlowRole;
	
	@Column(name="approval_flow_order")
	private Long approvalFlowOrder;
	
	@Column(name="approval_flow_status")
	private Long approvalFlowStatus;
	
	@Column(name="approval_flow_reject_reason")
	private String approvalFlowRejectReason;
	
	@Column(name="approval_flow_complete_date")
	@UpdateTimestamp
	private LocalDateTime approvalFlowCompleteDate;
	
	public ApprovalFlowDto toDto() {
		return ApprovalFlowDto.builder()
				.approval_flow_no(approvalFlowNo)
				.approval_no(approval.getApprovalNo())
				.member_no(member.getMemberNo())
				.member_name(member.getMemberName())
				.member_position(member.getPosition().getPositionName())
				.approval_flow_role(approvalFlowRole)
				.approval_flow_order(approvalFlowOrder)
				.approval_flow_status(approvalFlowStatus)
				.approval_flow_reject_reason(approvalFlowRejectReason)
				.approval_flow_complete_date(approvalFlowCompleteDate)
				.build();
				
	}
}
