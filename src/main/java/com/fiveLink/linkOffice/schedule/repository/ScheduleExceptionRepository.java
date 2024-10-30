package com.fiveLink.linkOffice.schedule.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fiveLink.linkOffice.schedule.domain.Schedule;
import com.fiveLink.linkOffice.schedule.domain.ScheduleException; 
@Repository
public interface ScheduleExceptionRepository extends JpaRepository<ScheduleException, Long>{
	List<ScheduleException> findByScheduleNo(Long scheduleNo);
	 
	List<ScheduleException> findByScheduleExceptionStatus(Long scheduleExceptionStatus); 
}
