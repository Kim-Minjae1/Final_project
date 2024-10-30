package com.fiveLink.linkOffice.schedule.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fiveLink.linkOffice.meeting.domain.Meeting;
import com.fiveLink.linkOffice.organization.domain.Department;
import com.fiveLink.linkOffice.schedule.domain.ScheduleCategory;

@Repository
public interface ScheduleCategoryRepository extends JpaRepository<ScheduleCategory, Long>{
    List<ScheduleCategory> findAllByScheduleCategoryStatusOrderByScheduleCategoryNameAsc(Long scheduleCategoryStatus);

    // 등록 중복 확인
    boolean existsByScheduleCategoryNameAndScheduleCategoryStatus(String scheduleCategoryName, Long scheduleCategoryStatus);
    
    boolean existsByScheduleCategoryColorAndScheduleCategoryStatus(String scheduleCategoryColor, Long scheduleCategoryStatus);
    
    // 수정 중복 확인
    boolean existsByScheduleCategoryNameAndScheduleCategoryStatusAndScheduleCategoryNoNot(String scheduleCategoryName, Long status, Long categoryNo);

    boolean existsByScheduleCategoryColorAndScheduleCategoryStatusAndScheduleCategoryNoNot(String scheduleCategoryColor, Long status, Long categoryNo);

    // 관리자 - 일정 등록 카테고리
    List<ScheduleCategory> findByScheduleCategoryStatusOrderByScheduleCategoryNameAsc(Long scheduleCategoryStatus);
    
    // 사원 - 일정 카테고리
    List<ScheduleCategory> findByScheduleCategoryStatusAndScheduleCategoryAdminOrderByScheduleCategoryNameAsc(Long scheduleCategoryStatus, Long scheduleCategoryAdmin);

}
