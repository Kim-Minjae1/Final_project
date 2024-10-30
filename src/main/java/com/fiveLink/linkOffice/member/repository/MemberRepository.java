package com.fiveLink.linkOffice.member.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.fiveLink.linkOffice.member.domain.Member;


@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
	//[전주영] 로그인
    Member findByMemberNumber(String memberNumber); 
    // [전주영] 전자결재이미지 수정
    Member findByMemberNo(Long memberNo);
    
    // [전주영] 권한 조회 
    @Query("SELECT m, p.positionName, d.departmentName " +
            "FROM Member m " +
            "JOIN m.position p " +
            "JOIN m.department d " +
            "WHERE m.memberNumber = :memberNumber")
    List<Object[]> findMemberNumber(@Param("memberNumber") String memberNumber);
    
    // [전주영] mypage 정보 조회
    @Query("SELECT m, p.positionName, d.departmentName " +
            "FROM Member m " +
            "JOIN m.position p " +
            "JOIN m.department d " +
            "WHERE m.memberNo = :memberNo")
     List<Object[]> findMemberWithDepartmentAndPosition(@Param("memberNo") Long memberNo); 

    
    // [서혜원] 부서 등록 
     List<Member> findByDepartmentNoAndMemberStatus(Long departmentNo, Long memberStatus);

    // [서혜원] 본인 포함 전체 사원 조직도
    @Query("SELECT m FROM Member m WHERE m.positionNo = :positionNo")
    List<Member> findByPositionNo(Long positionNo); 
    
    // [서혜원] 부서 소속 사원 여부
    @Query("SELECT COUNT(m) > 0 FROM Member m WHERE m.departmentNo = :departmentNo")
    boolean existsByDepartmentNo(@Param("departmentNo") Long departmentNo);

    // [서혜원] 하위 부서 소속 사원 여부
    long countByDepartmentNoAndMemberStatus(Long departmentNo, Long memberStatus);
    
    // [서혜원] 직위 소속 사원 여부
    long countByPositionNo(Long positionNo);
     
    // [서혜원] 조직도
    List<Member> findAllByMemberStatusOrderByPosition_PositionLevelAsc(Long status);
      
    // [전주영] 전체 사원 조회 (관리자 빼고, 입사일 최신순) - 관리자 사원 목록 조회
    @Query("SELECT m, p.positionName, d.departmentName " +
            "FROM Member m " +
            "LEFT JOIN Position p ON m.positionNo = p.positionNo " +
            "LEFT JOIN Department d ON m.departmentNo = d.departmentNo " +
            "WHERE m.memberNo != 1")
     Page<Object[]> findAllMembersWithDetails(Pageable pageable); 
     
     // 검색어(전체 : 사번, 사원명, 부서명, 직위명, 상태)
     @Query("SELECT m, p.positionName, d.departmentName " +
    	       "FROM Member m " +
    	       "LEFT JOIN Position p ON m.positionNo = p.positionNo " +
    	       "LEFT JOIN Department d ON m.departmentNo = d.departmentNo " +
    	       "WHERE m.memberNo != 1 " +
    	       "AND (m.memberNumber LIKE %:searchText% " +
    	       "     OR m.memberName LIKE %:searchText% " +
    	       "     OR d.departmentName LIKE %:searchText% " +
    	       "     OR p.positionName LIKE %:searchText% " +
    	       "     OR CASE " +
    	       "         WHEN :searchText = '재직' THEN m.memberStatus = 0 " +
    	       "         WHEN :searchText = '퇴사' THEN m.memberStatus = 1 " +
    	       "         ELSE false " +
    	       "       END)")
    	Page<Object[]> findMembersByNumberMemberNameDepartmentNamePositionNameStatus(@Param("searchText") String searchText, Pageable pageable);

    // 검색어(조건, 사번)
        @Query("SELECT m, p.positionName, d.departmentName " +
       	       "FROM Member m " +
       	       "LEFT JOIN Position p ON m.positionNo = p.positionNo " +
       	       "LEFT JOIN Department d ON m.departmentNo = d.departmentNo " +
       	       "WHERE m.memberNo != 1 AND m.memberNumber LIKE %:searchText%")
       	Page<Object[]> findMembersByNumber(@Param("searchText") String searchText, Pageable pageable);

     // 검색어(조건, 사원명)
        @Query("SELECT m, p.positionName, d.departmentName " +
       	       "FROM Member m " +
       	       "LEFT JOIN Position p ON m.positionNo = p.positionNo " +
       	       "LEFT JOIN Department d ON m.departmentNo = d.departmentNo " +
       	       "WHERE m.memberNo != 1 AND m.memberName LIKE %:searchText%")
       	Page<Object[]> findMembersByMemberName(@Param("searchText") String searchText, Pageable pageable);
     
     // 검색어(조건, 부서명)
     @Query("SELECT m, p.positionName, d.departmentName " +
    	       "FROM Member m " +
    	       "LEFT JOIN Position p ON m.positionNo = p.positionNo " +
    	       "LEFT JOIN Department d ON m.departmentNo = d.departmentNo " +
    	       "WHERE m.memberNo != 1 AND d.departmentName LIKE %:searchText%")
    	Page<Object[]> findMembersByDepartmentName(@Param("searchText") String searchText, Pageable pageable);
    // 검색어(조건, 직위명)
      @Query("SELECT m, p.positionName, d.departmentName " +
             "FROM Member m " +
             "LEFT JOIN Position p ON m.positionNo = p.positionNo " +
             "LEFT JOIN Department d ON m.departmentNo = d.departmentNo " +
             "WHERE m.memberNo != 1 AND p.positionName LIKE %:searchText%")
      Page<Object[]> findMembersByPositionName(@Param("searchText") String searchText, Pageable pageable);
     // 검색어(조건,상태 )
      @Query("SELECT m, p.positionName, d.departmentName FROM Member m " +
    	       "LEFT JOIN Position p ON m.positionNo = p.positionNo " +
    	       "LEFT JOIN Department d ON m.departmentNo = d.departmentNo " +
    	       "WHERE m.memberNo != 1 " +
    	       "AND ((:searchText = '재직' AND m.memberStatus = 0) " +
    	       "     OR (:searchText = '퇴사' AND m.memberStatus = 1))")
    	Page<Object[]> findMembersByMemberStatus(@Param("searchText") String searchText, Pageable pageable);
   
      
     // [전주영] 전체 사원 조회 (관리자 빼고, 직위순) - 사용자 사원 목록 조회
     @Query("SELECT m, p.positionName, d.departmentName " +
             "FROM Member m " +
             "LEFT JOIN Position p ON m.positionNo = p.positionNo " +
             "LEFT JOIN Department d ON m.departmentNo = d.departmentNo " +
             "WHERE m.memberNo != 1 AND m.memberStatus = 0")
      Page<Object[]> findAllMemberStatusOrderByPosition(Pageable pageable);
      
      // 검색어(전체 : 사번, 사원명, 부서명, 직위명)
      @Query("SELECT m, p.positionName, d.departmentName " +
    	       "FROM Member m " +
    	       "LEFT JOIN Position p ON m.positionNo = p.positionNo " +
    	       "LEFT JOIN Department d ON m.departmentNo = d.departmentNo " +
    	       "WHERE m.memberNo != 1 AND m.memberStatus = 0 " +
    	       "AND (m.memberNumber LIKE %:searchText% " +
    	       "     OR m.memberName LIKE %:searchText% " +
    	       "     OR d.departmentName LIKE %:searchText% " +
    	       "     OR p.positionName LIKE %:searchText%)")
    	Page<Object[]> findAllMemberStatusByNumberNameDepartmentNamePositionName(@Param("searchText") String searchText, Pageable pageable);

      // 검색어(조건, 사번)
      @Query("SELECT m, p.positionName, d.departmentName " +
              "FROM Member m " +
              "LEFT JOIN Position p ON m.positionNo = p.positionNo " +
              "LEFT JOIN Department d ON m.departmentNo = d.departmentNo " +
              "WHERE m.memberNo != 1 AND m.memberStatus = 0 AND m.memberNumber LIKE %:searchText%")
       Page<Object[]> findAllMemberStatusByMemberNumber(@Param("searchText") String searchText,Pageable pageable); 
       
       // 검색어(조건, 사원명)
       @Query("SELECT m, p.positionName, d.departmentName " +
               "FROM Member m " +
               "LEFT JOIN Position p ON m.positionNo = p.positionNo " +
               "LEFT JOIN Department d ON m.departmentNo = d.departmentNo " +
               "WHERE m.memberNo != 1 AND m.memberStatus = 0 AND m.memberName LIKE %:searchText%")
        Page<Object[]> findAllMemberStatusByMemberName(@Param("searchText") String searchText,Pageable pageable); 

      
      // 검색어(조건, 부서명)
      @Query("SELECT m, p.positionName, d.departmentName " +
              "FROM Member m " +
              "LEFT JOIN Position p ON m.positionNo = p.positionNo " +
              "LEFT JOIN Department d ON m.departmentNo = d.departmentNo " +
              "WHERE m.memberNo != 1 AND m.memberStatus = 0 AND d.departmentName LIKE %:searchText%")
       Page<Object[]> findAllMemberStatusByDepartmentName(@Param("searchText") String searchText,Pageable pageable); 
       // 검색어(조건, 직위명) 
       @Query("SELECT m, p.positionName, d.departmentName " +
               "FROM Member m " +
               "LEFT JOIN Position p ON m.positionNo = p.positionNo " +
               "LEFT JOIN Department d ON m.departmentNo = d.departmentNo " +
               "WHERE m.memberNo != 1 AND m.memberStatus = 0 AND p.positionName LIKE %:searchText%")
        Page<Object[]> findAllMemberStatusByPositionName(@Param("searchText") String searchText,Pageable pageable); 
 
      // [전주영] 사원 조회 
      @Query("SELECT m, p.positionName, d.departmentName " +
    		  "FROM Member m " +
    		  "LEFT JOIN Position p ON m.positionNo = p.positionNo " +
    		  "LEFT JOIN Department d ON m.departmentNo = d.departmentNo " +
    		  "WHERE m.memberNo != 1"
    		  + "ORDER BY m.memberHireDate DESC")
      List<Object[]> findAllMembers(); 
      
      // [서혜원] 권한자 등록
      @Transactional
      @Modifying
      @Query("UPDATE Member m SET m.memberAdditional = :status WHERE m.memberNo = :memberNo")
      void updateMemberAdditionalStatus(@Param("memberNo") Long memberNo, @Param("status") long status);
      
      // [김민재] 사원 이름 조회 
      Member findBymemberNo(Long noticeWriter);
      // [김민재] 사원 번호 조회
      Member findByMemberName(String memberName);  
      
      // [서혜원] 본인 제외 사원 조직도
      List<Member> findAllByMemberStatusAndMemberNumberNotOrderByPosition_PositionLevelAsc(Long status, String memberNumber);


}

