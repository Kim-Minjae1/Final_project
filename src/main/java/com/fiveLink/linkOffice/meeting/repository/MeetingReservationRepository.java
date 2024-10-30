package com.fiveLink.linkOffice.meeting.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fiveLink.linkOffice.meeting.domain.MeetingReservation;

@Repository
public interface MeetingReservationRepository extends JpaRepository<MeetingReservation, Long> {
	@Query("SELECT mr, mem, pos.positionName, dept.departmentName " +
		       "FROM MeetingReservation mr " +
		       "JOIN Meeting m ON mr.meetingNo = m.meetingNo " +
		       "JOIN Member mem ON mr.memberNo = mem.memberNo " +
		       "JOIN Position pos ON mem.positionNo = pos.positionNo " +    
		       "JOIN Department dept ON mem.departmentNo = dept.departmentNo " +  
		       "WHERE mr.meetingReservationDate = :date " +
		       "AND mr.meetingReservationStatus = :status " +
		       "ORDER BY mr.meetingReservationStartTime ASC")
    List<Object[]> findReservations(@Param("date") String date, @Param("status") Long status);
  
    @Query("SELECT mr FROM MeetingReservation mr " +
    	       "JOIN Meeting m ON mr.meetingNo = m.meetingNo " +
    	       "JOIN Member mem ON mr.memberNo = mem.memberNo " +
    	       "WHERE (:memberNo IS NULL OR mr.memberNo = :memberNo) " +
    	       "AND (:meetingNo IS NULL OR mr.meetingNo = :meetingNo) " +
    	       "AND (:searchText IS NULL OR mr.meetingReservationPurpose LIKE %:searchText%) " +
    	       "AND (:startDate IS NULL OR mr.meetingReservationDate >= :startDate) " +
    	       "AND (:endDate IS NULL OR mr.meetingReservationDate <= :endDate) " +
    	       "AND mr.meetingReservationStatus = 0 " +
    	       "ORDER BY " +
    	       "CASE WHEN :sortBy = 'latest' THEN mr.meetingReservationDate END DESC, " +
    	       "CASE WHEN :sortBy = 'oldest' THEN mr.meetingReservationDate END ASC")
    Page<MeetingReservation> searchReservations(@Param("memberNo") Long memberNo, @Param("meetingNo") Long meetingNo, @Param("searchText") String searchText, @Param("startDate") String startDate, 
    											@Param("endDate") String endDate, @Param("sortBy") String sortBy, Pageable pageable);
    
    // 관리자 - 전체 예약 목록
    @Query("SELECT mr FROM MeetingReservation mr " +
 	       "JOIN Meeting m ON mr.meetingNo = m.meetingNo " +
 	       "JOIN Member mem ON mr.memberNo = mem.memberNo " + 
 	       "WHERE (:meetingNo IS NULL OR mr.meetingNo = :meetingNo) " +
 	       "AND (:searchText IS NULL OR mem.memberName LIKE %:searchText%) " +
 	       "AND (:startDate IS NULL OR mr.meetingReservationDate >= :startDate) " +
 	       "AND (:endDate IS NULL OR mr.meetingReservationDate <= :endDate) " +
 	       "AND mr.meetingReservationStatus = 0 " +
 	       "ORDER BY " +
 	       "CASE WHEN :sortBy = 'latest' THEN mr.meetingReservationDate END DESC, " +
 	       "CASE WHEN :sortBy = 'oldest' THEN mr.meetingReservationDate END ASC")
    Page<MeetingReservation> allReservations(@Param("meetingNo") Long meetingNo, @Param("searchText") String searchText, @Param("startDate") String startDate, 
 											@Param("endDate") String endDate, @Param("sortBy") String sortBy, Pageable pageable);
    
    @Query("SELECT mr FROM MeetingReservation mr WHERE mr.meetingReservationStatus = 0")
    List<MeetingReservation> findMeetingReservationsByStatusZero();
    
    List<MeetingReservation> findAllByMeetingReservationStatusOrderByMeetingReservationStartTimeAsc(Long status);
} 