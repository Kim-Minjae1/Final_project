package com.fiveLink.linkOffice.schedule.domain;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

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
@Table(name = "fl_schedule_exception")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED) 
@Setter
@Getter
@Builder
public class ScheduleException {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_exception_no")
    private Long scheduleExceptionNo;

    @Column(name = "schedule_no")
    private Long scheduleNo; 
    
    @Column(name = "member_no")
    private Long memberNo; 

    @Column(name = "schedule_exception_date")
    private String scheduleExceptionDate;

    @Column(name = "schedule_exception_title")
    private String scheduleExceptionTitle;

    @Column(name = "schedule_exception_comment")
    private String scheduleExceptionComment;

    @Column(name = "schedule_exception_start_date")
    private String scheduleExceptionStartDate;

    @Column(name = "schedule_exception_end_date")
    private String scheduleExceptionEndDate;
    
    @Column(name = "schedule_exception_start_time")
    private String scheduleExceptionStartTime;

    @Column(name = "schedule_exception_end_time")
    private String scheduleExceptionEndTime;
    
    @Column(name = "schedule_exception_allday")
    private Long scheduleExceptionAllday;

    @Column(name = "schedule_category_no")
    private Long scheduleCategoryNo;

    @Column(name = "schedule_exception_type")
    private Long scheduleExceptionType; 
    
    @Column(name = "department_no")
    private Long departmentNo; 
    
    @CreationTimestamp
    @Column(name = "schedule_exception_create_date")
    private LocalDateTime scheduleExceptionCreateDate;
    
    @Column(name = "schedule_exception_status")
    private Long scheduleExceptionStatus;
}