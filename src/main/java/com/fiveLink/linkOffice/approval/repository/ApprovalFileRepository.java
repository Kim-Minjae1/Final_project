package com.fiveLink.linkOffice.approval.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fiveLink.linkOffice.approval.domain.Approval;
import com.fiveLink.linkOffice.approval.domain.ApprovalFile;
import com.fiveLink.linkOffice.approval.domain.ApprovalFlow;
import com.fiveLink.linkOffice.vacationapproval.domain.VacationApproval;

@Repository
public interface ApprovalFileRepository extends JpaRepository<ApprovalFile, Long>{
	
	List<ApprovalFile> findByApproval(Approval approval);
	
	boolean existsByApproval(Approval approval);
}
