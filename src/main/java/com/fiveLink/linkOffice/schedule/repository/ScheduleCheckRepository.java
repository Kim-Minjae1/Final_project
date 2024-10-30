package com.fiveLink.linkOffice.schedule.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fiveLink.linkOffice.schedule.domain.ScheduleCheck;
import com.fiveLink.linkOffice.schedule.domain.ScheduleCheckDto; 

@Repository
public interface ScheduleCheckRepository extends JpaRepository<ScheduleCheck, Long>{

	List<ScheduleCheck> findByMemberNoAndScheduleCheckStatus(Long memberNo, Long scheduleCheckStatus); 

	ScheduleCheck findByMemberNoAndDepartmentNoAndScheduleCheckStatus(Long member_no, Long department_no, Long scheduleCheckStatus);

}
