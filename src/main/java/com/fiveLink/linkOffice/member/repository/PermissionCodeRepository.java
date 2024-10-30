package com.fiveLink.linkOffice.member.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fiveLink.linkOffice.member.domain.PermissionCode;

@Repository
public interface PermissionCodeRepository extends JpaRepository<PermissionCode, Long>{
	@Query("SELECT pc.permissionCodeName FROM PermissionCode pc "+
			"JOIN MenuPermission mp ON pc.permissionCodeNo = mp.permissionCodeNo "+
			"JOIN MemberPermission bp ON mp.menuPermissionNo = bp.menuPermissionNo "+
			"WHERE bp.memberNo = :memberNo "+
			"AND bp.memberPermissionStatus = 0")
	List<String> findPermissionsByMemberNo(@Param("memberNo") Long memberNo);
	
	// [서혜원] 권한 관리 - 기능별 권한자
	@Query("SELECT m.memberNo, m.memberName, d.departmentName, p.positionName, mp.memberPermissionCreateDate " +
		       "FROM MemberPermission mp " +
		       "JOIN Member m ON mp.memberNo = m.memberNo " +
		       "JOIN Department d ON m.departmentNo = d.departmentNo " +
		       "JOIN Position p ON m.positionNo = p.positionNo " +
		       "WHERE mp.menuPermissionNo = :menuPermissionNo " +
		       "AND m.memberStatus = 0 " +
		       "AND mp.memberPermissionStatus = 0 " +   
		       "ORDER BY mp.memberPermissionCreateDate DESC")
	List<Object[]> findMembersByMenuNoWithDetails(@Param("menuPermissionNo") Long menuPermissionNo);


}
