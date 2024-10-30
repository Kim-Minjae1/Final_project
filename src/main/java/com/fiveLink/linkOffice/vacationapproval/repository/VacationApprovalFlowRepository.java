package com.fiveLink.linkOffice.vacationapproval.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fiveLink.linkOffice.vacationapproval.domain.VacationApproval;
import com.fiveLink.linkOffice.vacationapproval.domain.VacationApprovalFile;
import com.fiveLink.linkOffice.vacationapproval.domain.VacationApprovalFlow;

@Repository
public interface VacationApprovalFlowRepository extends JpaRepository<VacationApprovalFlow, Long>{
	
	List<VacationApprovalFlow> findByVacationApproval(VacationApproval vacationApproval);
	
	List<VacationApprovalFlow> findByVacationApprovalVacationApprovalNo(Long vacation_approval_no);
	
	// 결재 내역함 조회
	@Query("SELECT v FROM VacationApprovalFlow v WHERE v.member.memberNo = :memberNo AND v.vacationApprovalFlowRole IN (1, 2) AND v.vacationApprovalFlowStatus <> 0")
	List<VacationApprovalFlow> findByMemberMemberNoAndRole(@Param("memberNo") Long memberNo);
	
	//결재 참조함 조회
	@Query("SELECT v FROM VacationApprovalFlow v WHERE v.member.memberNo = :memberNo AND v.vacationApprovalFlowRole IN (0)")
	List<VacationApprovalFlow> findByMemberMemberNoAndRoleReferens(@Param("memberNo") Long memberNo);
	
	void deleteByVacationApproval(VacationApproval vacationApproval);
}
