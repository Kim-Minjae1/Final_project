package com.fiveLink.linkOffice.vacationapproval.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fiveLink.linkOffice.vacationapproval.domain.VacationApproval;


@Repository
public interface VacationApprovalRepository extends JpaRepository<VacationApproval, Long>{

	// 사원의 휴가신청함
	Page<VacationApproval> findAllByMemberMemberNo(Long memberNo, Pageable pageable);
	
	// 전체 조회
	@Query("SELECT va FROM VacationApproval va " +
			"WHERE va.member.memberNo = :memberNo " +
			"AND ((:searchText = '진행중' OR :searchText = '진행' AND va.vacationApprovalStatus = 0) " +
			"     OR (:searchText = '완료' AND va.vacationApprovalStatus = 1) " +
			"     OR (:searchText = '반려' AND va.vacationApprovalStatus = 2) " +
			"     OR (:searchText = '취소' AND va.vacationApprovalStatus = 3) " + 
			"     OR (va.vacationApprovalTitle LIKE %:searchText%))")
	Page<VacationApproval> findByMemberMemberNoAndVacationApprovalTitleAndApprovalStatus(@Param("memberNo") Long memberNo, @Param("searchText") String searchText, Pageable pageable);

	// 제목으로 검색
	 Page<VacationApproval> findByMemberMemberNoAndVacationApprovalTitleContaining(Long memberNo, String searchText, Pageable pageable);

	 // 상태로 검색
	 @Query("SELECT va FROM VacationApproval va " +
	           "WHERE va.member.memberNo = :memberNo " +
	           "AND ((:searchText = '진행중' OR :searchText = '진행' AND va.vacationApprovalStatus = 0) " +
	           "     OR (:searchText = '완료' AND va.vacationApprovalStatus = 1) " +
	           "     OR (:searchText = '반려' AND va.vacationApprovalStatus = 2) " +
	           "     OR (:searchText = '취소' AND va.vacationApprovalStatus = 3))")
	 Page<VacationApproval> findByMemberNoAndApprovalStatus(@Param("memberNo") Long memberNo, @Param("searchText") String searchText, Pageable pageable);
	 
	 
	 // 사원 휴가 신청 상세 조회 
	 VacationApproval findByVacationApprovalNo(Long VacatioApprovalNo);
	 
	// [박혜선] 사원 휴가 신청 조회(근태 조회)
	 List<VacationApproval> findByMemberMemberNo(Long memberNo);
	 
	 // [서혜원] 사원 일정 휴가 조회 
	 @Query(value = "SELECT va.member_no, va.vacation_type_no, va.vacation_approval_start_date, va.vacation_approval_end_date, " +
             "vt.vacation_type_name, m.department_no, va.vacation_approval_no " +  
             "FROM fl_vacation_approval va " +
             "JOIN fl_vacation_type vt ON va.vacation_type_no = vt.vacation_type_no " +
             "JOIN fl_member m ON va.member_no = m.member_no " +  
             "WHERE va.vacation_approval_status = :status", nativeQuery = true)
List<Object[]> findApprovedVacationSchedules(@Param("status") int status);



}