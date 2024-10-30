package com.fiveLink.linkOffice.schedule.domain;

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
public class ScheduleCheckDto {
	private Long schedule_check_no;
    private Long department_no;
    private Long member_no;
    private Long schedule_check_status; 

    public ScheduleCheck toEntity() {
        return ScheduleCheck.builder()
                .scheduleCheckNo(schedule_check_no)
                .departmentNo(department_no)
                .memberNo(member_no)
                .scheduleCheckStatus(schedule_check_status) 
                .build();
    }

    public static ScheduleCheckDto toDto(ScheduleCheck scheduleCheck) {
        return ScheduleCheckDto.builder()
                .schedule_check_no(scheduleCheck.getScheduleCheckNo())
                .department_no(scheduleCheck.getDepartmentNo())
                .member_no(scheduleCheck.getMemberNo())
                .schedule_check_status(scheduleCheck.getScheduleCheckStatus())
                .build();
    }
}
