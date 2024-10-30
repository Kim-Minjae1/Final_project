package com.fiveLink.linkOffice.permission.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fiveLink.linkOffice.member.domain.Member;
import com.fiveLink.linkOffice.member.domain.MemberPermission;

@Repository
public interface MemberPermissionRepository extends JpaRepository<MemberPermission, Long> {

	@Query("SELECT mp.memberNo FROM MemberPermission mp WHERE mp.menuPermissionNo = :menuPermissionNo AND mp.memberPermissionStatus = 0")
	List<Long> findMemberNosByMenuPermissionNo(@Param("menuPermissionNo") Long menuPermissionNo);

	@Query("SELECT m, mp.memberPermissionCreateDate FROM Member m JOIN MemberPermission mp ON m.memberNo = mp.memberNo WHERE mp.menuPermissionNo IN :menuPermissionNos AND mp.memberPermissionStatus = 0")
	List<Object[]> findMembersByMenuPermissionNos(@Param("menuPermissionNos") List<Long> menuPermissionNos);

	@Query("SELECT mp FROM MemberPermission mp WHERE mp.memberNo = :memberNo")
	MemberPermission findByMemberNo(@Param("memberNo") Long memberNo); 
	 
	List<MemberPermission> findByMemberNoInAndMenuPermissionNo(List<Long> memberNos, Long menuPermissionNo);
	
	@Query("SELECT COUNT(mp) > 0 FROM MemberPermission mp WHERE mp.memberNo = :memberNo AND mp.menuPermissionNo BETWEEN :startNo AND :endNo AND mp.memberPermissionStatus = 0")
	boolean existsByMemberNoAndMenuPermissionNoRange(@Param("memberNo") Long memberNo, @Param("startNo") Long startNo, @Param("endNo") Long endNo); 
	
	boolean existsByMemberNoAndMemberPermissionStatus(Long memberNo, int status);
}
