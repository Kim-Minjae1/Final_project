package com.fiveLink.linkOffice.schedule.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fiveLink.linkOffice.schedule.domain.Schedule;
import com.fiveLink.linkOffice.schedule.domain.ScheduleRepeat; 

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
	long countByScheduleCategoryNoAndScheduleStatus(Long scheduleCategoryNo, Long scheduleStatus);
	
	@Query("SELECT s FROM Schedule s WHERE s.scheduleType = :scheduleType AND s.scheduleStatus = :scheduleStatus")
	List<Schedule> findByScheduleTypeAndScheduleStatus(@Param("scheduleType") Long scheduleType, @Param("scheduleStatus") Long scheduleStatus);
	 
	@Query("SELECT s FROM Schedule s WHERE s.scheduleType = :scheduleType AND s.scheduleStatus = :scheduleStatus AND s.memberNo = :memberNo")
	List<Schedule> findByScheduleTypeAndScheduleStatusAndMemberNo(@Param("scheduleType") Long scheduleType, @Param("scheduleStatus") Long scheduleStatus, @Param("memberNo") Long memberNo);
 
}
