package com.fiveLink.linkOffice.meeting.domain;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class MeetingParticipantDto {
	private Long meeting_participant_no;
	private Long meeting_reservation_no;
	private Long member_no;
	private Long meeting_participant_status;
	
	private String memberName;
	private String positionName;
	private String departmentName;
	
	public MeetingParticipant toEntity() {
        return MeetingParticipant.builder()
                .meetingParticipantNo(meeting_participant_no)
                .meetingReservationNo(meeting_reservation_no)
                .memberNo(member_no)
                .meetingParticipantStatus(meeting_participant_status)
                .build();
    }
	
	public static MeetingParticipantDto toDto(MeetingParticipant meetingParticipant) {
        return MeetingParticipantDto.builder()
                .meeting_participant_no(meetingParticipant.getMeetingReservationNo())
                .meeting_reservation_no(meetingParticipant.getMeetingReservationNo())
                .member_no(meetingParticipant.getMemberNo())
                .meeting_participant_status(meetingParticipant.getMeetingParticipantStatus())
                .build();
    }
}
