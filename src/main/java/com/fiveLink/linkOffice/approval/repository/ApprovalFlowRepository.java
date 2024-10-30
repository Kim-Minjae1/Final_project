package com.fiveLink.linkOffice.approval.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fiveLink.linkOffice.approval.domain.Approval;
import com.fiveLink.linkOffice.approval.domain.ApprovalFlow;
import com.fiveLink.linkOffice.vacationapproval.domain.VacationApproval;
import com.fiveLink.linkOffice.vacationapproval.domain.VacationApprovalFlow;

@Repository
public interface ApprovalFlowRepository extends JpaRepository<ApprovalFlow, Long> {
	
	List<ApprovalFlow> findByApproval(Approval approval);
	
	List<ApprovalFlow> findByApprovalApprovalNo(Long approval_no);
	
	void deleteByApproval(Approval approval);
	
	//결재 참조함 조회
	@Query("SELECT v FROM ApprovalFlow v WHERE v.member.memberNo = :memberNo AND v.approvalFlowRole IN (0)")
	List<ApprovalFlow> findByMemberMemberNoAndRoleReferens(@Param("memberNo") Long memberNo);
}
