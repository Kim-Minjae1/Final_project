package com.fiveLink.linkOffice.meeting.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fiveLink.linkOffice.meeting.domain.MeetingParticipant;

@Repository
public interface MeetingParticipantRepository extends JpaRepository<MeetingParticipant, Long> {
	@Query("SELECT COUNT(mp) FROM MeetingParticipant mp WHERE mp.meetingReservationNo = :reservationNo AND mp.meetingParticipantStatus = :status")
    long countByMeetingReservationNoAndStatus(@Param("reservationNo") Long reservationNo, @Param("status") Long status);
	
	@Query("SELECT mp FROM MeetingParticipant mp WHERE mp.meetingReservationNo = :reservationNo AND mp.meetingParticipantStatus = 0")
    List<MeetingParticipant> findParticipantsByReservationNo(@Param("reservationNo") Long reservationNo); 
	
	@Query("SELECT mp FROM MeetingParticipant mp WHERE mp.meetingReservationNo = :meetingReservationNo AND mp.meetingParticipantStatus = 0 AND mp.memberNo NOT IN (:memberNo)")
    List<MeetingParticipant> findParticipantsByMeetingReservationNoExcludingMember(@Param("meetingReservationNo") Long meetingReservationNo, @Param("memberNo") Long memberNo);
	
	@Query("SELECT mp FROM MeetingParticipant mp WHERE mp.meetingReservationNo = :reservationNo AND mp.meetingParticipantStatus = 0 AND mp.memberNo NOT IN (:memberNo)")
	List<MeetingParticipant> findParticipantsByReservationNoExcludingMemberNo(@Param("reservationNo") Long reservationNo, @Param("memberNo") Long memberNo);
}

