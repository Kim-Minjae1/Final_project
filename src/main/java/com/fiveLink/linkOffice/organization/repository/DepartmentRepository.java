package com.fiveLink.linkOffice.organization.repository;

import com.fiveLink.linkOffice.member.domain.Member;
import com.fiveLink.linkOffice.organization.domain.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    List<Department> findByDepartmentHigh(Long departmentHigh);

    List<Department> findByDepartmentHighAndDepartmentStatus(Long departmentHigh, Long departmentStatus);
    
    List<Department> findAllByDepartmentStatusOrderByDepartmentHighAscDepartmentNameAsc(Long departmentStatus);
    
    // 부서명 수정 중복 확인
    boolean existsByDepartmentNameAndDepartmentStatus(String departmentName, Long departmentStatus);   
    
    boolean existsByDepartmentNameAndDepartmentStatusAndDepartmentNoNot(String departmentName, Long departmentStatus, Long departmentId);
    
    @Query("SELECT d FROM Department d WHERE d.departmentHigh = :departmentNo AND d.departmentStatus = 0")
    List<Department> findSubDepartmentsByDepartmentNo(@Param("departmentNo") Long departmentNo);

    // [전주영] 사원 등록 (부서명조회)
    @Query(value = "SELECT d1 FROM Department d1 LEFT JOIN Department d2 ON d1.departmentNo = d2.departmentHigh " +
            "WHERE d2.departmentHigh IS NULL AND d1.departmentStatus = 0 "
            + "ORDER BY d1.departmentName")
    List<Department> findDepartmentsWithoutSubDepartments();
    
    @Query("SELECT d FROM Department d WHERE d.departmentHigh = :departmentNo ORDER BY d.departmentName ASC")
    List<Department> findSubDepartmentsByDepartmentName(@Param("departmentNo") Long departmentNo);
    
    // [박혜선] 폴더 생성 (부서번호 조회)
    Department findByDepartmentNo(Long departmentNo);
    
    // [서혜원] 일정 부서 조회
    @Query("SELECT d FROM Department d " +
            "WHERE d.departmentStatus = 0 " +
            "AND (d.departmentHigh != 0 " +
            "OR (d.departmentHigh = 0 " +
            "AND d.departmentNo NOT IN (SELECT d2.departmentHigh FROM Department d2 WHERE d2.departmentHigh != 0))) " +
            "ORDER BY d.departmentHigh ASC, d.departmentName ASC")
     List<Department> findAllByCustomCondition();
     
}
