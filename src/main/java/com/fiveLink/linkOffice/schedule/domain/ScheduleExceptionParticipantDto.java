package com.fiveLink.linkOffice.schedule.domain;

import groovy.transform.ToString;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter; 

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class ScheduleExceptionParticipantDto {

    private Long schedule_exception_participant_no;
    private Long schedule_exception_no;
    private Long member_no;
    private Long schedule_exception_participant_status;
 
	private String memberName;
	private String positionName;
	private String departmentName;
	
    public ScheduleExceptionParticipant toEntity() {
        return ScheduleExceptionParticipant.builder()
                .scheduleExceptionParticipantNo(schedule_exception_participant_no)
                .scheduleExceptionNo(schedule_exception_no)
                .memberNo(member_no)
                .scheduleExceptionParticipantStatus(schedule_exception_participant_status)
                .build();
    }

    public static ScheduleExceptionParticipantDto toDto(ScheduleExceptionParticipant scheduleExceptionParticipant) {
        return ScheduleExceptionParticipantDto.builder()
                .schedule_exception_participant_no(scheduleExceptionParticipant.getScheduleExceptionParticipantNo())
                .schedule_exception_no(scheduleExceptionParticipant.getScheduleExceptionNo())
                .member_no(scheduleExceptionParticipant.getMemberNo())
                .schedule_exception_participant_status(scheduleExceptionParticipant.getScheduleExceptionParticipantStatus())
                .build();
    }
}