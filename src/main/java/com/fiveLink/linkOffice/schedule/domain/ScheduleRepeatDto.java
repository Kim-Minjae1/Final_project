package com.fiveLink.linkOffice.schedule.domain;
 
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
public class ScheduleRepeatDto {

    private Long schedule_repeat_no;
    private Long schedule_no;
    private Long schedule_repeat_type;
    private Long schedule_repeat_day;
    private Long schedule_repeat_week;
    private Long schedule_repeat_date;
    private Long schedule_repeat_month;
    private String schedule_repeat_end_date;

    public ScheduleRepeat toEntity() {
        return ScheduleRepeat.builder()
                .scheduleRepeatNo(schedule_repeat_no)
                .scheduleNo(schedule_no)
                .scheduleRepeatType(schedule_repeat_type)
                .scheduleRepeatDay(schedule_repeat_day)
                .scheduleRepeatWeek(schedule_repeat_week)
                .scheduleRepeatDate(schedule_repeat_date)
                .scheduleRepeatMonth(schedule_repeat_month)
                .scheduleRepeatEndDate(schedule_repeat_end_date)
                .build();
    }

    public static ScheduleRepeatDto toDto(ScheduleRepeat scheduleRepeat) {
        return ScheduleRepeatDto.builder()
                .schedule_repeat_no(scheduleRepeat.getScheduleRepeatNo())
                .schedule_no(scheduleRepeat.getScheduleNo())
                .schedule_repeat_type(scheduleRepeat.getScheduleRepeatType())
                .schedule_repeat_day(scheduleRepeat.getScheduleRepeatDay())
                .schedule_repeat_week(scheduleRepeat.getScheduleRepeatWeek())
                .schedule_repeat_date(scheduleRepeat.getScheduleRepeatDate())
                .schedule_repeat_month(scheduleRepeat.getScheduleRepeatMonth())
                .schedule_repeat_end_date(scheduleRepeat.getScheduleRepeatEndDate())
                .build();
    }
}