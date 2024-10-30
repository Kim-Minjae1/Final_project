package com.fiveLink.linkOffice.approval.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fiveLink.linkOffice.approval.domain.ApprovalForm;

@Repository
public interface ApprovalFormRepository extends JpaRepository<ApprovalForm, Long>{
	// 전체 양식 (상태 1 제외 조회)
	Page<ApprovalForm> findAllByApprovalFormStatusNot(Pageable pageable, Long approvalFormStatus);

    @Query("SELECT af FROM ApprovalForm af WHERE af.approvalFormTitle LIKE %:searchText% AND af.approvalFormStatus <> :status")
    Page<ApprovalForm> findByaprovalFormTitleContaining(@Param("searchText") String searchText, Pageable pageable, @Param("status") Long approvalFormStatus);
    
    List<ApprovalForm> findAllByApprovalFormStatusNot(Long approvalFormStatus);
    
    ApprovalForm findByApprovalFormNo(Long formNo);
}
