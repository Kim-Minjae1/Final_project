package com.fiveLink.linkOffice.schedule.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fiveLink.linkOffice.schedule.domain.ScheduleExceptionParticipant;
import com.fiveLink.linkOffice.schedule.domain.ScheduleParticipant;

@Repository
public interface ScheduleParticipantRepository extends JpaRepository<ScheduleParticipant, Long> {
	@Query("SELECT sp FROM ScheduleParticipant sp WHERE sp.scheduleNo = :scheduleNo AND sp.scheduleParticipantStatus = 0")
    List<ScheduleParticipant> findParticipantsByScheduleNo(@Param("scheduleNo") Long scheduleNo); 
	
	@Query("SELECT sp FROM ScheduleParticipant sp WHERE sp.scheduleNo = :scheduleNo AND sp.scheduleParticipantStatus = 0 AND sp.memberNo NOT IN (:memberNo)")
    List<ScheduleParticipant> findParticipantsByScheduleNo(@Param("scheduleNo") Long scheduleNo, @Param("memberNo") Long memberNo); 
}
