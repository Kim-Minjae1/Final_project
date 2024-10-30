package com.fiveLink.linkOffice.schedule.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fiveLink.linkOffice.schedule.domain.ScheduleRepeat;

@Repository
public interface ScheduleRepeatRepository extends JpaRepository<ScheduleRepeat, Long>{
	 List<ScheduleRepeat> findByScheduleNo(long scheduleNo);
	 
	 @Query("SELECT sr FROM ScheduleRepeat sr")
	 List<ScheduleRepeat> findAllScheduleRepeats(); 
	 
	 @Query("SELECT sr FROM ScheduleRepeat sr WHERE sr.scheduleNo = :scheduleNo")
	 ScheduleRepeat getByScheduleNo(@Param("scheduleNo") Long scheduleNo);
}
