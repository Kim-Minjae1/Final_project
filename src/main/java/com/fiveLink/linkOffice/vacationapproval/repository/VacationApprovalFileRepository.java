package com.fiveLink.linkOffice.vacationapproval.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fiveLink.linkOffice.vacationapproval.domain.VacationApproval;
import com.fiveLink.linkOffice.vacationapproval.domain.VacationApprovalFile;

@Repository
public interface VacationApprovalFileRepository extends JpaRepository<VacationApprovalFile, Long>{
	
	List<VacationApprovalFile> findByVacationApproval(VacationApproval vacationApproval);
	
	boolean existsByVacationApproval(VacationApproval vacationApproval);
	
}
