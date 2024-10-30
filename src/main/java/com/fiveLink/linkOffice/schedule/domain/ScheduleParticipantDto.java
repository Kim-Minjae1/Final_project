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
public class ScheduleParticipantDto {

    private Long schedule_participant_no;
    private Long schedule_no;
    private Long member_no;
    private Long schedule_participant_status;
 
	private String memberName;
	private String positionName;
	private String departmentName;
	
    public ScheduleParticipant toEntity() {
        return ScheduleParticipant.builder()
                .scheduleParticipantNo(schedule_participant_no)
                .scheduleNo(schedule_no)
                .memberNo(member_no)
                .scheduleParticipantStatus(schedule_participant_status)
                .build();
    }

    public static ScheduleParticipantDto toDto(ScheduleParticipant scheduleParticipant) {
        return ScheduleParticipantDto.builder()
                .schedule_participant_no(scheduleParticipant.getScheduleParticipantNo())
                .schedule_no(scheduleParticipant.getScheduleNo())
                .member_no(scheduleParticipant.getMemberNo())
                .schedule_participant_status(scheduleParticipant.getScheduleParticipantStatus())
                .build();
    }
}