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
public class ScheduleDto {
	private Long schedule_no;
    private Long member_no;
    private String schedule_title;
    private String schedule_comment;
    private String schedule_start_date;
    private String schedule_end_date;
    private String schedule_start_time;
    private String schedule_end_time;
    private Long schedule_allday;
    private Long schedule_category_no;
    private Long schedule_type;
    private Long department_no;
    private Long meeting_no;
    private Long schedule_repeat;
    private LocalDateTime schedule_create_date;
    private LocalDateTime schedule_update_date;
    private Long schedule_status; 
    
    private String member_name; 
    private String department_name; 
    private String position_name; 
    
    public Schedule toEntity() {
        return Schedule.builder()
                .scheduleNo(schedule_no)
                .memberNo(member_no)
                .scheduleTitle(schedule_title)
                .scheduleComment(schedule_comment)
                .scheduleStartDate(schedule_start_date)
                .scheduleEndDate(schedule_end_date)
                .scheduleStartTime(schedule_start_time)
                .scheduleEndTime(schedule_end_time)
                .scheduleAllday(schedule_allday)
                .scheduleCategoryNo(schedule_category_no)
                .scheduleType(schedule_type)
                .departmentNo(department_no)
                .meetingNo(meeting_no)
                .scheduleRepeat(schedule_repeat)
                .scheduleCreateDate(schedule_create_date)
                .scheduleUpdateDate(schedule_update_date)
                .scheduleStatus(schedule_status)
                .build();
    }

    public static ScheduleDto toDto(Schedule schedule) {
        return ScheduleDto.builder()
                .schedule_no(schedule.getScheduleNo())
                .member_no(schedule.getMemberNo())
                .schedule_title(schedule.getScheduleTitle())
                .schedule_comment(schedule.getScheduleComment())
                .schedule_start_date(schedule.getScheduleStartDate())
                .schedule_end_date(schedule.getScheduleEndDate())
                .schedule_start_time(schedule.getScheduleStartTime())
                .schedule_end_time(schedule.getScheduleEndTime())
                .schedule_allday(schedule.getScheduleAllday())
                .schedule_category_no(schedule.getScheduleCategoryNo())
                .schedule_type(schedule.getScheduleType())
                .department_no(schedule.getDepartmentNo())
                .meeting_no(schedule.getMeetingNo())
                .schedule_repeat(schedule.getScheduleRepeat())
                .schedule_create_date(schedule.getScheduleCreateDate())
                .schedule_update_date(schedule.getScheduleUpdateDate())
                .schedule_status(schedule.getScheduleStatus())
                .build();
    }
}