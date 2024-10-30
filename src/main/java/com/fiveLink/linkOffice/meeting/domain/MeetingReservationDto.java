package com.fiveLink.linkOffice.meeting.domain;

import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class MeetingReservationDto {

    private Long meeting_reservation_no;
    private Long meeting_no;
    private Long member_no;
    private String meeting_reservation_date;
    private String meeting_reservation_start_time;
    private String meeting_reservation_end_time;
    private String meeting_reservation_purpose;
    private LocalDateTime meeting_reservation_create_date;
    private LocalDateTime meeting_reservation_update_date;
    private Long meeting_reservation_status;
    
    private String meeting_name; 
    private String member_name;
    private String position_name; 
    private String department_name;
    
    private Long participant_count;

    public MeetingReservation toEntity() {
        return MeetingReservation.builder()
                .meetingReservationNo(meeting_reservation_no)
                .meetingNo(meeting_no)
                .memberNo(member_no)
                .meetingReservationDate(meeting_reservation_date)
                .meetingReservationStartTime(meeting_reservation_start_time)
                .meetingReservationEndTime(meeting_reservation_end_time)
                .meetingReservationPurpose(meeting_reservation_purpose)
                .meetingReservationCreateDate(meeting_reservation_create_date)
                .meetingReservationUpdateDate(meeting_reservation_update_date)
                .meetingReservationStatus(meeting_reservation_status)
                .build();
    }

    public static MeetingReservationDto toDto(MeetingReservation meetingReservation, String meetingName, String memberName) {
        return MeetingReservationDto.builder()
                .meeting_reservation_no(meetingReservation.getMeetingReservationNo())
                .meeting_no(meetingReservation.getMeetingNo())
                .member_no(meetingReservation.getMemberNo())
                .meeting_reservation_date(meetingReservation.getMeetingReservationDate())
                .meeting_reservation_start_time(meetingReservation.getMeetingReservationStartTime())
                .meeting_reservation_end_time(meetingReservation.getMeetingReservationEndTime())
                .meeting_reservation_purpose(meetingReservation.getMeetingReservationPurpose())
                .meeting_reservation_create_date(meetingReservation.getMeetingReservationCreateDate())
                .meeting_reservation_update_date(meetingReservation.getMeetingReservationUpdateDate())
                .meeting_reservation_status(meetingReservation.getMeetingReservationStatus())
                .meeting_name(meetingName) 
                .member_name(memberName)  
                .build();
    }
}
