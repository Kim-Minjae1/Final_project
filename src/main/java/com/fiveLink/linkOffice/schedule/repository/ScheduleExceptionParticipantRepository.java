package com.fiveLink.linkOffice.schedule.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fiveLink.linkOffice.schedule.domain.ScheduleExceptionParticipant;
import com.fiveLink.linkOffice.schedule.domain.ScheduleParticipant;

@Repository
public interface ScheduleExceptionParticipantRepository extends JpaRepository<ScheduleExceptionParticipant, Long>{
	@Query("SELECT sp FROM ScheduleExceptionParticipant sp WHERE sp.scheduleExceptionNo = :scheduleExceptionNo AND sp.scheduleExceptionParticipantStatus = 0")
    List<ScheduleExceptionParticipant> findExceptionParticipantsByScheduleNo(@Param("scheduleExceptionNo") Long scheduleExceptionNo);

	@Query("SELECT sp FROM ScheduleExceptionParticipant sp WHERE sp.scheduleExceptionNo = :scheduleExceptionNo AND sp.scheduleExceptionParticipantStatus = 0 AND sp.memberNo NOT IN (:memberNo)")
	List<ScheduleExceptionParticipant> findExceptionParticipantsByScheduleNo(@Param("scheduleExceptionNo") Long scheduleExceptionNo, @Param("memberNo") Long memberNo);
}
	