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
public class ScheduleCategoryDto {
    private Long schedule_category_no;
    private String schedule_category_name;
    private String schedule_category_color;
    private Long schedule_category_admin;
    private LocalDateTime schedule_category_create_date;
    private LocalDateTime schedule_category_update_date;
    private Long schedule_category_status;

    public ScheduleCategory toEntity() {
        return ScheduleCategory.builder()
                .scheduleCategoryNo(schedule_category_no)
                .scheduleCategoryName(schedule_category_name)
                .scheduleCategoryColor(schedule_category_color)
                .scheduleCategoryAdmin(schedule_category_admin)
                .scheduleCategoryCreateDate(schedule_category_create_date)
                .scheduleCategoryUpdateDate(schedule_category_update_date)
                .scheduleCategoryStatus(schedule_category_status)
                .build();
    }

    public static ScheduleCategoryDto toDto(ScheduleCategory scheduleCategory) {
        return ScheduleCategoryDto.builder()
                .schedule_category_no(scheduleCategory.getScheduleCategoryNo())
                .schedule_category_name(scheduleCategory.getScheduleCategoryName())
                .schedule_category_color(scheduleCategory.getScheduleCategoryColor())
                .schedule_category_admin(scheduleCategory.getScheduleCategoryAdmin())
                .schedule_category_create_date(scheduleCategory.getScheduleCategoryCreateDate())
                .schedule_category_update_date(scheduleCategory.getScheduleCategoryUpdateDate())
                .schedule_category_status(scheduleCategory.getScheduleCategoryStatus())
                .build();
    }
}