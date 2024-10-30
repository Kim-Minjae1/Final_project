package com.fiveLink.linkOffice.schedule.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "fl_schedule_repeat")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Builder
public class ScheduleRepeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_repeat_no")
    private Long scheduleRepeatNo;

    @Column(name = "schedule_no", nullable = false)
    private Long scheduleNo;

    @Column(name = "schedule_repeat_type", nullable = false)
    private Long scheduleRepeatType;

    @Column(name = "schedule_repeat_day")
    private Long scheduleRepeatDay;

    @Column(name = "schedule_repeat_week")
    private Long scheduleRepeatWeek;

    @Column(name = "schedule_repeat_date")
    private Long scheduleRepeatDate;

    @Column(name = "schedule_repeat_month")
    private Long scheduleRepeatMonth;

    @Column(name = "schedule_repeat_end_date")
    private String scheduleRepeatEndDate;
}