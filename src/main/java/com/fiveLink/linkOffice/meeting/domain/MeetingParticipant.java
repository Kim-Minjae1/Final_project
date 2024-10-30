package com.fiveLink.linkOffice.meeting.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "fl_meeting_participant")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Builder
public class MeetingParticipant {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "meeting_participant_no")
    private Long meetingParticipantNo;
	
	@Column(name = "meeting_reservation_no")
    private Long meetingReservationNo;
	
	@Column(name = "member_no")
    private Long memberNo;
	
	@Column(name = "meeting_participant_status")
    private Long meetingParticipantStatus;
}


